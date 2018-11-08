package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjenesteOrkestrerer;
import no.nav.foreldrepenger.domene.kontrollerfakta.StartpunktTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.Endringskontroller;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterinnhentingHistorikkinnslagTjeneste;

/**
 * Denne klassen er en utvidelse av {@link BehandlingskontrollTjeneste} som håndterer oppdatering på åpen behandling.
 * <p>
 * Ikke endr denne klassen dersom du ikke har en komplett forståelse av hvordan denne protokollen fungerer.
 */
@Dependent
public class EndringskontrollerImpl implements Endringskontroller {
    private static final Logger LOGGER = LoggerFactory.getLogger(EndringskontrollerImpl.class);
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private StartpunktTjeneste startpunktTjeneste;
    private OppgaveTjeneste oppgaveTjeneste;
    private RegisterinnhentingHistorikkinnslagTjeneste historikkinnslagTjeneste;
    private KontrollerFaktaTjenesteOrkestrerer kontrollerFaktaTjenesteOrkestrerer;

    public EndringskontrollerImpl() {
        // For CDI proxy
    }

    @Inject
    public EndringskontrollerImpl(BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                  BehandlingRepositoryProvider provider,
                                  StartpunktTjeneste startpunktTjeneste,
                                  OppgaveTjeneste oppgaveTjeneste,
                                  RegisterinnhentingHistorikkinnslagTjeneste historikkinnslagTjeneste,
                                  KontrollerFaktaTjenesteOrkestrerer kontrollerFaktaTjenesteOrkestrerer) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.aksjonspunktRepository = provider.getAksjonspunktRepository();
        this.startpunktTjeneste = startpunktTjeneste;
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.kontrollerFaktaTjenesteOrkestrerer = kontrollerFaktaTjenesteOrkestrerer;
    }

    @Override
    public void gjenoppta(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
        behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, false);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
    }

    @Override
    public boolean settPåVent(Behandling behandling, AksjonspunktDefinisjon autopunkt, LocalDateTime fristTid, Venteårsak venteårsak) {
        if (erPåVent(behandling)) {
            return false;
        }
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, autopunkt,
            behandling.getAktivtBehandlingSteg(), fristTid, venteårsak);
        return true;
    }

    @Override
    public void taAvVent(Behandling behandling, AksjonspunktDefinisjon autopunkt) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.settAutopunktTilUtført(autopunkt, kontekst);
    }

    @Override
    public void spolTilSteg(Behandling behandling, BehandlingStegType behandlingStegType) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling.getId());
        behandlingskontrollTjeneste.behandlingTilbakeføringHvisTidligereBehandlingSteg(kontekst, behandlingStegType);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
    }

    @Override
    public void spolTilStartpunkt(Behandling behandling, EndringsresultatDiff endringsresultat) {
        avsluttOppgaverIGsak(behandling);

        StartpunktType startpunkt = startpunktTjeneste.utledStartpunktForDiffBehandlingsgrunnlag(behandling, endringsresultat);
        if (startpunkt.equals(StartpunktType.UDEFINERT)) {
            return; // Ingen detekterte endringer - ingen tilbakespoling
        }
        List<AksjonspunktResultat> aksjonspunktResultater = kontrollerFaktaTjenesteOrkestrerer.utledAksjonspunkterTilHøyreForStartpunkt(behandling, startpunkt);
        doSpolTilStartpunkt(behandling, startpunkt, aksjonspunktResultater);
    }

    private void doSpolTilStartpunkt(Behandling behandling, StartpunktType startpunktType, List<AksjonspunktResultat> aksjonspunktResultater) {
        // TODO (PK-49128): Tore, gi oss et API!!!
        BehandlingStegType fraSteg = behandling.getAktivtBehandlingSteg();
        BehandlingStegType tilSteg = startpunktType.getBehandlingSteg();

        reaktiverInaktiveAksjonspunkter(startpunktType, behandling);
        opprettAksjonspunkter(aksjonspunktResultater, startpunktType.getBehandlingSteg(), behandling);
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        boolean tilbakeføres = behandlingskontrollTjeneste.erStegPassert(behandling, tilSteg);
        if (tilbakeføres) {
            // Eventuelt ta behandling av vent
            behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
            behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, false);
            // Spol tilbake og fortsette behandling
            behandlingskontrollTjeneste.behandlingTilbakeføringHvisTidligereBehandlingSteg(kontekst, tilSteg);
            loggSpoleutfall(behandling, fraSteg, tilSteg, tilbakeføres);
            behandlingskontrollTjeneste.prosesserBehandling(kontekst);
        } else {
            loggSpoleutfall(behandling, fraSteg, tilSteg, tilbakeføres);
        }
    }

    private void loggSpoleutfall(Behandling behandling, BehandlingStegType førSteg, BehandlingStegType etterSteg, boolean tilbakeført) {
        if (tilbakeført && !førSteg.equals(etterSteg)) {
            historikkinnslagTjeneste.opprettHistorikkinnslagForTilbakespoling(behandling, førSteg, etterSteg);
            LOGGER.info("Behandling {} har mottatt en endring som medførte spoling tilbake. Før-steg {}, etter-steg {}", behandling.getId(),
                førSteg.getNavn(), etterSteg.getNavn());// NOSONAR //$NON-NLS-1$
        } else {
            LOGGER.info("Behandling {} har mottatt en endring som ikke medførte spoling tilbake. Før-steg {}, etter-steg {}", behandling.getId(),
                førSteg.getNavn(), etterSteg.getNavn());// NOSONAR //$NON-NLS-1$
        }
    }

    List<Aksjonspunkt> opprettAksjonspunkter(List<AksjonspunktResultat> apResultater, BehandlingStegType behandlingStegType, Behandling behandling) {
        if (!apResultater.isEmpty()) {
            List<Aksjonspunkt> funnetAksjonspunkter = new ArrayList<>();
            fjernGjensidigEkskluderendeAksjonspunkter(apResultater, behandling);
            funnetAksjonspunkter.addAll(leggTilNyeAksjonspunkterPåBehandling(behandlingStegType, apResultater, behandling));
            funnetAksjonspunkter.addAll(reåpneAvbrutteOgUtførteAksjonspunkter(apResultater, behandling));
            return funnetAksjonspunkter;
        } else {
            return new ArrayList<>();
        }
    }

    private List<Aksjonspunkt> leggTilNyeAksjonspunkterPåBehandling(BehandlingStegType behandlingStegType,
                                                                    List<AksjonspunktResultat> nyeDefinisjoner,
                                                                    Behandling behandling) {

        List<AksjonspunktDefinisjon> eksisterendeDefinisjoner = behandling.getAksjonspunkter().stream()
            .map(Aksjonspunkt::getAksjonspunktDefinisjon)
            .collect(toList());

        List<AksjonspunktResultat> nyeAksjonspunkt = nyeDefinisjoner.stream()
            .filter(apDefWrapper -> !eksisterendeDefinisjoner.contains(apDefWrapper.getAksjonspunktDefinisjon()))
            .collect(toList());

        return leggTilAksjonspunkt(behandlingStegType, behandling, nyeAksjonspunkt);
    }

    private List<Aksjonspunkt> leggTilAksjonspunkt(BehandlingStegType behandlingStegType, Behandling behandling,
                                                   List<AksjonspunktResultat> nyeAksjonspunkt) {

        List<Aksjonspunkt> aksjonspunkter = new ArrayList<>();
        nyeAksjonspunkt.forEach((AksjonspunktResultat apResultat) -> {

            Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, apResultat.getAksjonspunktDefinisjon(),
                behandlingStegType);
            apResultat.getAksjonspunktModifiserer().accept(aksjonspunkt);
            aksjonspunkter.add(aksjonspunkt);
        });
        return aksjonspunkter;
    }


    private void reaktiverInaktiveAksjonspunkter(StartpunktType startpunktType, Behandling behandling) {
        Set<String> apDefFraSteg = hentFremtidigeAksjonspunkterDefinisjonerFraOgMed(behandling, startpunktType.getBehandlingSteg());
        behandling.getAlleAksjonspunkterInklInaktive().stream()
            .filter(ap -> !ap.erAktivt())
            .filter(ap -> apDefFraSteg.contains(ap.getAksjonspunktDefinisjon().getKode()))
            .forEach(ap -> aksjonspunktRepository.reaktiver(ap));
    }

    List<Aksjonspunkt> reåpneAvbrutteOgUtførteAksjonspunkter(List<AksjonspunktResultat> nyeDefinisjoner,
                                                             Behandling behandling) {

        Map<AksjonspunktDefinisjon, AksjonspunktResultat> aksjonspunktResultatMap = nyeDefinisjoner.stream()
            .collect(toMap(AksjonspunktResultat::getAksjonspunktDefinisjon, Function.identity()));

        Set<Aksjonspunkt> skalReåpnes = behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.erUtført() || ap.erAvbrutt())
            .filter(ap -> aksjonspunktResultatMap.get(ap.getAksjonspunktDefinisjon()) != null)
            .collect(toSet());

        List<Aksjonspunkt> reåpnedeAksjonspunkter = new ArrayList<>();
        skalReåpnes.forEach((Aksjonspunkt ap) -> {
            aksjonspunktRepository.setReåpnet(ap);
            aksjonspunktResultatMap.get(ap.getAksjonspunktDefinisjon()).getAksjonspunktModifiserer().accept(ap);
            reåpnedeAksjonspunkter.add(ap);
        });

        return reåpnedeAksjonspunkter;
    }

    private void avsluttOppgaverIGsak(Behandling behandling) {
        boolean behandlingIFatteVedtak = BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus());
        if (behandlingIFatteVedtak) {
            oppgaveTjeneste.avslutt(behandling.getId(), OppgaveÅrsak.GODKJENNE_VEDTAK);
        }
    }

    private boolean erPåVent(Behandling behandling) {
        return !behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).isEmpty();
    }

    private Set<String> hentFremtidigeAksjonspunkterDefinisjonerFraOgMed(Behandling behandling, BehandlingStegType målsteg) {
        return behandlingskontrollTjeneste.finnAksjonspunktDefinisjonerFraOgMed(behandling, målsteg, true);
    }

    private void fjernGjensidigEkskluderendeAksjonspunkter(List<AksjonspunktResultat> nyeApResultater, Behandling behandling) {
        Set<AksjonspunktDefinisjon> nyeApDef = nyeApResultater.stream().map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(toSet());
        List<AksjonspunktDefinisjon> utelukkedeAksjonspunkter = behandling.getAksjonspunkter().stream()
            .flatMap(ap -> ap.getAksjonspunktDefinisjon().getUtelukkendeApdef().stream())
            .filter(utelukkendeApDef -> nyeApDef.contains(utelukkendeApDef))
            .collect(toList());
        // Dersom eksisterende aksjonspunkter på behandling er utelukket av de nye, så må de fjernes
        utelukkedeAksjonspunkter.forEach(utelukketApDef -> aksjonspunktRepository.fjernAksjonspunkt(behandling, utelukketApDef));
    }
}
