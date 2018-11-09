package no.nav.foreldrepenger.behandling.steg.registrersøknad;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIRSØKNAD_FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.REGISTRER_PAPIR_ENDRINGSØKNAD_FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VENT_PÅ_SØKNAD;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.registrersøknad.api.RegistrerSøknadSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.Kompletthetsjekker;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetsjekkerProvider;

@BehandlingStegRef(kode = "REGSØK")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@ApplicationScoped
public class RegistrerSøknadStegImpl implements RegistrerSøknadSteg {
    private BehandlingRepository behandlingRepository;
    private MottatteDokumentRepository mottatteDokumentRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private KompletthetsjekkerProvider kompletthetsjekkerProvider;

    RegistrerSøknadStegImpl() {
        // for CDI proxy
    }

    @Inject
    public RegistrerSøknadStegImpl(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                   BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                   KompletthetsjekkerProvider kompletthetsjekkerProvider) {

        this.mottatteDokumentRepository = behandlingRepositoryProvider.getMottatteDokumentRepository();
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.kompletthetsjekkerProvider = kompletthetsjekkerProvider;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        List<MottattDokument> mottatteDokumenter = mottatteDokumentRepository.hentMottatteDokument(kontekst.getBehandlingId());
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        FagsakYtelseType ytelseType = behandling.getFagsak().getYtelseType();

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);

        MottattDokument nyttDokument = mottatteDokumenter.stream()
            .max(Comparator.comparing(MottattDokument::getOpprettetTidspunkt)) // Sist mottatte dokument
            .orElse(null);
        if (nyttDokument == null) {
            // Behandlingen er startet uten noe dokument, f.eks. gjennom en forretningshendselse
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }

        //TODO IVER HER MÅ DET FYRES EVENT
        behandlingRepository.lagre(behandling, lås);

        Kompletthetsjekker kompletthetsjekker = kompletthetsjekkerProvider.finnKompletthetsjekkerFor(behandling);
        KompletthetResultat søknadMottatt = kompletthetsjekker.vurderSøknadMottatt(behandling);
        if (!søknadMottatt.erOppfylt()) {
            return evaluerSøknadMottattUoppfylt(behandling, søknadMottatt, VENT_PÅ_SØKNAD);
        }

        if (FagsakYtelseType.FORELDREPENGER.equals(ytelseType) && erUstrukturertForeldrepengerSøknad(nyttDokument)) {
            return BehandleStegResultat.utførtMedAksjonspunkter(singletonList(REGISTRER_PAPIRSØKNAD_FORELDREPENGER));
        }

        if (erUstrukturertEndringSøknad(nyttDokument)) {
            return BehandleStegResultat.utførtMedAksjonspunkter(singletonList(REGISTRER_PAPIR_ENDRINGSØKNAD_FORELDREPENGER));
        }

        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private boolean erUstrukturertEndringSøknad(MottattDokument dokument) {
        DokumentTypeId dokumentTypeId = dokument.getDokumentTypeId();
        return dokument.getPayloadXml() == null && DokumentTypeId.getEndringSøknadTyper().contains(dokumentTypeId);
    }

    private boolean erUstrukturertEngangsstønadSøknad(MottattDokument dokument) {
        DokumentTypeId dokumentTypeId = dokument.getDokumentTypeId();
        return dokument.getPayloadXml() == null && (DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON.equals(dokumentTypeId)
            || DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.equals(dokumentTypeId) || DokumentKategori.SØKNAD.equals(dokument.getDokumentKategori()));
    }

    private boolean erUstrukturertForeldrepengerSøknad(MottattDokument dokument) {
        DokumentTypeId dokumentTypeId = dokument.getDokumentTypeId();
        return dokument.getPayloadXml() == null &&
            (DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON.equals(dokumentTypeId)
            || DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.equals(dokumentTypeId) || DokumentKategori.SØKNAD.equals(dokument.getDokumentKategori()));
    }

    private boolean henleggBehandling(Behandling behandling) {
        return behandling.getAksjonspunkter().stream().anyMatch(a -> a.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.VENT_PÅ_SØKNAD) && a.getFristTid().isBefore(LocalDateTime.now()));
    }

    private BehandleStegResultat evaluerSøknadMottattUoppfylt(Behandling behandling, KompletthetResultat kompletthetResultat, AksjonspunktDefinisjon apDef) {
        if (henleggBehandling(behandling)) {
            return BehandleStegResultat.henlagtBehandling();
        }
        // TODO: Bestill brev (Venter på PK-50295)
        Aksjonspunkt aksjonspunkt = behandlingskontrollTjeneste.settBehandlingPåVent(behandling, apDef, BehandlingStegType.REGISTRER_SØKNAD, kompletthetResultat.getVentefrist(), Venteårsak.AVV_DOK);

        return BehandleStegResultat.utførtMedAksjonspunkter(singletonList(aksjonspunkt.getAksjonspunktDefinisjon()));
    }
}
