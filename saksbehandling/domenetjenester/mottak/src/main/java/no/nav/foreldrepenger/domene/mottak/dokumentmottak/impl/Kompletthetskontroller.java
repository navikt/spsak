package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_KOMPLETT_OPPDATERING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetModell;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.registerinnhenting.Endringskontroller;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;

/**
 * Denne klassen evaluerer hvilken effekt en ekstern hendelse (dokument, forretningshendelse) har på en åpen behandlings
 * kompletthet, og etterfølgende effekt på behandlingskontroll (gjennom {@link Endringskontroller})
 */
@Dependent
public class Kompletthetskontroller {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private DokumentmottakerFelles dokumentmottakerFelles;
    private EndringsresultatSjekker endringsresultatSjekker;
    private RegisterdataEndringshåndterer registerdataEndringshåndterer;
    private Endringskontroller endringskontroller;
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;
    private KompletthetModell kompletthetModell;

    public Kompletthetskontroller() {
        // For CDI proxy
    }

    @Inject
    public Kompletthetskontroller(BehandlingskontrollTjeneste behandlingskontrollTjeneste, // NOSONAR - ingen umiddelbar mulighet for å redusere denne til >= 7 parametere
                                  DokumentmottakerFelles dokumentmottakerFelles,
                                  EndringsresultatSjekker endringsresultatSjekker,
                                  RegisterdataEndringshåndterer registerdataEndringshåndterer,
                                  Endringskontroller endringskontroller,
                                  MottatteDokumentTjeneste mottatteDokumentTjeneste,
                                  KompletthetModell kompletthetModell) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.dokumentmottakerFelles = dokumentmottakerFelles;
        this.endringsresultatSjekker = endringsresultatSjekker;
        this.registerdataEndringshåndterer = registerdataEndringshåndterer;
        this.endringskontroller = endringskontroller;
        this.mottatteDokumentTjeneste = mottatteDokumentTjeneste;
        this.kompletthetModell = kompletthetModell;
    }

    void persisterDokumentOgVurderKompletthet(Behandling behandling, InngåendeSaksdokument mottattDokument) {
        // TODO (essv): Workaround mens vi venter på PKMANTIS-1646
        validerIngenVentingPåRegisteroppdateringer(behandling);

        // Ta snapshot av gjeldende grunnlag-id-er før oppdateringer
        EndringsresultatSnapshot grunnlagSnapshot = endringsresultatSjekker.opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(behandling);

        // Persister dokument (dvs. knytt dokument til behandlingen)
        mottatteDokumentTjeneste.persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());

        // Vurder kompletthet etter at dokument knyttet til behandling
        KompletthetResultat kompletthetResultat = kompletthetModell.vurderKompletthet(behandling);
        if (!kompletthetResultat.erOppfylt()) {
            settPåVent(behandling, kompletthetResultat);
        } else {
            if (kompletthetModell.erKompletthetssjekkPassert(behandling)) {
                spolKomplettBehandlingTilStartpunkt(behandling, grunnlagSnapshot);
            }
            endringskontroller.gjenoppta(behandling);
        }
    }

    private void validerIngenVentingPåRegisteroppdateringer(Behandling behandling) {
        boolean kanBehandlingEndresUnderVenting = behandling.getÅpneAksjonspunkter().stream()
            .anyMatch(ap -> ap.getAksjonspunktDefinisjon().equals(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER));
        if (kanBehandlingEndresUnderVenting) {
            throw new IllegalStateException("Kan ikke knytte dokument til behandling mens den venter på registeroppdateringer");
        }
    }

    private void settPåVent(Behandling behandling, KompletthetResultat kompletthetResultat) {
        if (kompletthetResultat.erFristUtløpt()) {
            // Tidsfrist for kompletthetssjekk er utløpt, skal derfor ikke settes på vent på nytt
            return;
        }
        // Settes på vent til behandlig er komplett
        boolean sattPåVent = endringskontroller.settPåVent(behandling, AUTO_VENT_KOMPLETT_OPPDATERING, kompletthetResultat.getVentefrist(), kompletthetResultat.getVenteårsak());
        if (sattPåVent) {
            dokumentmottakerFelles.opprettHistorikkinnslagForVenteFristRelaterteInnslag(behandling,
                HistorikkinnslagType.BEH_VENT, kompletthetResultat.getVentefrist(), kompletthetResultat.getVenteårsak());
        }
    }

    void persisterKøetDokumentOgVurderKompletthet(Behandling behandling, InngåendeSaksdokument mottattDokument, Optional<LocalDate> gjelderFra) {
        // Persister dokument (dvs. knytt dokument til behandlingen)
        mottatteDokumentTjeneste.persisterDokumentinnhold(behandling, mottattDokument, gjelderFra);
        vurderKompletthetForKøetBehandling(behandling);
    }

    public void oppdaterKompletthetForKøetBehandling(Behandling behandling) {
        vurderKompletthetForKøetBehandling(behandling);
    }

    void vurderKompletthetForKøetBehandling(Behandling behandling) {
        List<AksjonspunktDefinisjon> autoPunkter = kompletthetModell.rangerKompletthetsfunksjonerKnyttetTilAutopunkt(behandling);
        for (AksjonspunktDefinisjon autopunkt : autoPunkter) {
            KompletthetResultat kompletthetResultat = kompletthetModell.vurderKompletthet(behandling, autopunkt);
            if (!kompletthetResultat.erOppfylt()) {
                // Et av kompletthetskriteriene er ikke oppfylt, og evt. brev er sendt ut. Logger historikk og avbryter
                if (!kompletthetResultat.erFristUtløpt()) {
                    dokumentmottakerFelles.opprettHistorikkinnslagForVenteFristRelaterteInnslag(behandling,
                        HistorikkinnslagType.BEH_VENT, kompletthetResultat.getVentefrist(), kompletthetResultat.getVenteårsak());
                }
                return;
            }
        }
    }

    public void vurderNyForretningshendelse(Behandling behandling) {
        if (kompletthetModell.erKompletthetssjekkPassert(behandling)) {
            EndringsresultatSnapshot snapshot = endringsresultatSjekker.opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(behandling);
            registerdataEndringshåndterer.oppdaterRegisteropplysninger(behandling, snapshot);
        }
    }

    void spolKomplettBehandlingTilStartpunkt(Behandling behandling, EndringsresultatSnapshot grunnlagSnapshot) {
        // Behandling er komplett - nullstill venting (dvs. sett autopunkt for dette som utført)
        endringskontroller.taAvVent(behandling, AUTO_VENT_KOMPLETT_OPPDATERING);
        // Registerinnhenteren håndterer spoling til startpunkt dersom endringer detekteres
        registerdataEndringshåndterer.oppdaterRegisteropplysninger(behandling, grunnlagSnapshot);
    }

    void flyttTilbakeTilRegistreringPapirsøknad(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, false);
        behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, BehandlingStegType.REGISTRER_SØKNAD);
        dokumentmottakerFelles.opprettTaskForÅStarteBehandling(behandling);
    }

    boolean støtterBehandlingstypePapirsøknad(Behandling behandling) {
        return behandlingskontrollTjeneste.inneholderSteg(behandling, BehandlingStegType.REGISTRER_SØKNAD);
    }

}
