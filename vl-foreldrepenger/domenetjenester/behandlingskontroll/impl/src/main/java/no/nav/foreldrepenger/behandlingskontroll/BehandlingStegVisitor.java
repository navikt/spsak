package no.nav.foreldrepenger.behandlingskontroll;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.StegTransisjon;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

/**
 * Visitor for å traversere behandlingssteg.
 * <p>
 * Thread-safety: Bør opprettes for hver traversering.
 */
class BehandlingStegVisitor {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(BehandlingStegVisitor.class);

    private final BehandlingRepository behandlingRepository;
    private final BehandlingskontrollTjeneste kontrollTjeneste;
    private final BehandlingskontrollKontekst kontekst;
    private final BehandlingModell behandlingModell;
    private final BehandlingStegKonfigurasjon behandlingStegKonfigurasjon;

    private final BehandlingskontrollEventPubliserer eventPubliserer;

    private final Behandling behandling;

    private Optional<BehandlingStegTilstand> sistBehandlingStegTilstand;

    private final InternalManipulerBehandlingImpl manipulerInternBehandling;

    private final AksjonspunktRepository aksjonspunktRepository;


    BehandlingStegVisitor(BehandlingRepositoryProvider repositoryProvider, Behandling behandling,
                          BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                          BehandlingModell behandlingModell, BehandlingskontrollKontekst kontekst,
                          BehandlingskontrollEventPubliserer eventPubliserer) {

        Objects.requireNonNull(behandling, "behandling");
        Objects.requireNonNull(kontekst, "kontekst");
        this.behandling = behandling;
        this.behandlingModell = behandlingModell;
        this.sistBehandlingStegTilstand = behandling.getBehandlingStegTilstand();
        this.kontrollTjeneste = behandlingskontrollTjeneste;
        this.kontekst = kontekst;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

        this.manipulerInternBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);

        if (eventPubliserer != null) {
            this.eventPubliserer = eventPubliserer;
        } else {
            this.eventPubliserer = BehandlingskontrollEventPubliserer.NULL_EVENT_PUB;
        }
        behandlingStegKonfigurasjon = behandlingskontrollTjeneste.getBehandlingStegKonfigurasjon();

    }

    BehandlingStegProsesseringResultat prosesser(BehandlingStegModell stegModell) {
        BehandlingStegProsesseringResultat resultat = prosesserSteg(stegModell);
        return resultat;
    }


    Behandling getBehandling() {
        return behandling;
    }

    private BehandlingStegProsesseringResultat prosesserSteg(BehandlingStegModell stegModell) {
        BehandlingSteg steg = stegModell.getSteg();
        BehandlingStegType stegType = stegModell.getBehandlingStegType();

        // Sett riktig status for steget før det utføres
        BehandlingStegStatus førStegStatus = behandling.getBehandlingStegStatus();
        BehandlingStegStatus førsteStegStatus = utledStegStatusFørUtføring(stegModell);
        oppdaterBehandlingStegStatus(stegType, førStegStatus, førsteStegStatus);

        // Utfør steg hvis tillatt av stegets før-status. Utled stegets nye status.
        BehandlingStegProsesseringResultat stegResultat;
        List<Aksjonspunkt> funnetAksjonspunkter = new ArrayList<>();
        if (erIkkePåVent() && førsteStegStatus.kanUtføreSteg()) {
            BehandleStegResultat resultat = steg.utførSteg(kontekst);

            reaktiverInaktiveAksjonspunkter(resultat);

            funnetAksjonspunkter = opprettAksjonspunkter(resultat, stegType);
            BehandlingStegStatus nyStegStatus = håndterResultatAvSteg(stegModell, resultat);
            stegResultat = BehandlingStegProsesseringResultat.medMuligTransisjon(nyStegStatus, resultat.getTransisjon());
        } else if (BehandlingStegStatus.erVedUtgang(førsteStegStatus)) {
            BehandlingStegStatus nyStegStatus = utledUtgangStegStatus(stegModell.getBehandlingStegType());
            stegResultat = BehandlingStegProsesseringResultat.utenOverhopp(nyStegStatus);
        } else {
            stegResultat = BehandlingStegProsesseringResultat.utenOverhopp(førsteStegStatus);
        }

        avsluttSteg(stegType, førsteStegStatus, stegResultat, funnetAksjonspunkter);

        return stegResultat;
    }

    public void avsluttSteg(BehandlingStegType stegType, BehandlingStegStatus førsteStegStatus, BehandlingStegProsesseringResultat stegResultat,
                           List<Aksjonspunkt> funnetAksjonspunkter) {

        log.info("Avslutter steg={}, transisjon={} med aksjonspunkter={}", stegType, stegResultat, funnetAksjonspunkter.stream().map(a-> a.getAksjonspunktDefinisjon()).collect(Collectors.toList()));

        Optional<BehandlingStegTilstand> stegTilstandFør = behandling.getBehandlingStegTilstand();

        // Sett riktig status for steget etter at det er utført. Lagre eventuelle endringer fra steg på behandling
        guardAlleÅpneAksjonspunkterHarDefinertVurderingspunkt();
        oppdaterBehandlingStegStatus(stegType, førsteStegStatus, stegResultat.getNyStegStatus());
        lagreBehandling(behandling);

        // Publiser transisjonsevent
        StegTransisjon transisjon = behandlingModell.finnTransisjon(stegResultat.getTransisjon());
        BehandlingStegType tilSteg = finnFremoverhoppSteg(stegType, transisjon);
        eventPubliserer.fireEvent(new BehandlingTransisjonEvent(kontekst, stegResultat.getTransisjon(), stegTilstandFør, tilSteg, transisjon.erFremoverhopp()));

        // Publiser event om endring i stegets tilstand
        BehandlingStegTilstandEndringEvent behandlingStegTilstandEndringEvent = new BehandlingStegTilstandEndringEvent(kontekst, sistBehandlingStegTilstand);
        behandlingStegTilstandEndringEvent.setNyTilstand(behandling.getBehandlingStegTilstand());
        eventPubliserer.fireEvent(behandlingStegTilstandEndringEvent);

        // Publiser de funnede aksjonspunktene
        kontrollTjeneste.aksjonspunkterFunnet(kontekst, stegType, funnetAksjonspunkter);
        lagreBehandling(behandling);
    }

    private BehandlingStegType finnFremoverhoppSteg(BehandlingStegType stegType, StegTransisjon transisjon) {
        BehandlingStegType tilSteg = null;
        if (transisjon.erFremoverhopp()) {
            BehandlingStegModell fraStegModell = behandlingModell.finnSteg(stegType);
            BehandlingStegModell tilStegModell = transisjon.nesteSteg(fraStegModell);
            tilSteg = tilStegModell != null ? tilStegModell.getBehandlingStegType() : null;
        }
        return tilSteg;
    }

    void markerOvergangTilNyttSteg(BehandlingStegType stegType) {
        log.info("Markerer nytt steg som aktivt: {}", stegType);

        // Flytt aktivt steg til gjeldende steg hvis de ikke er like
        BehandlingStegStatus sluttStatusForAndreSteg = behandlingStegKonfigurasjon.getUtført();
        settBehandlingStegSomGjeldende(stegType, sluttStatusForAndreSteg);
        fyrEventBehandlingStegOvergang();
    }

    private boolean erIkkePåVent() {
        return !behandling.isBehandlingPåVent();
    }

    void fyrEventBehandlingStegOvergang() {
        Optional<BehandlingStegTilstand> behandlingStegTilstand = behandling.getBehandlingStegTilstand();
        BehandlingStegOvergangEvent event = BehandlingModellImpl.nyBehandlingStegOvergangEvent(behandlingModell, sistBehandlingStegTilstand,
            behandlingStegTilstand, kontekst, false);

        this.sistBehandlingStegTilstand = behandlingStegTilstand;

        eventPubliserer.fireEvent(event);
    }

    void fyrEventBehandlingStegTilbakeføring() {
        boolean erOverstyring = false;
        BehandlingStegOvergangEvent event = BehandlingModellImpl.nyBehandlingStegOvergangEvent(
            behandlingModell, sistBehandlingStegTilstand, behandling.getBehandlingStegTilstand(), kontekst, erOverstyring);

        eventPubliserer.fireEvent(event);
    }

    private void oppdaterBehandlingStegStatus(BehandlingStegType stegType, BehandlingStegStatus førsteStegStatus,
                                              BehandlingStegStatus nyStegStatus) {
        Optional<BehandlingStegTilstand> behandlingStegTilstand = behandling.getBehandlingStegTilstand(stegType);
        if (behandlingStegTilstand.isPresent()) {
            if (erForskjellig(førsteStegStatus, nyStegStatus)) {
                manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, stegType, nyStegStatus);
                lagreBehandling(behandling);
                eventPubliserer.fireEvent(kontekst, stegType, førsteStegStatus, nyStegStatus);
            }
        }
    }

    private static boolean erForskjellig(BehandlingStegStatus førsteStegStatus, BehandlingStegStatus nyStegStatus) {
        return !Objects.equals(nyStegStatus, førsteStegStatus);
    }

    protected BehandlingStegStatus utledStegStatusFørUtføring(BehandlingStegModell stegModell) {

        BehandlingStegStatus nåBehandlingStegStatus = behandling.getBehandlingStegStatus();

        BehandlingStegType stegType = stegModell.getBehandlingStegType();

        if (erForbiInngang(nåBehandlingStegStatus)) {
            // Hvis vi har kommet forbi INNGANG, så gå direkte videre til det gjeldende statusen
            return nåBehandlingStegStatus;
        } else {
            // Hvis status er null/UDEFINERT eller INNGANG så reberegner vi for å se om vi kan STARTE
            List<AksjonspunktDefinisjon> kriterier = stegType.getAksjonspunktDefinisjonerInngang();

            List<Aksjonspunkt> åpneAksjonspunkter = behandling.getÅpneAksjonspunkter(kriterier);

            BehandlingStegStatus nyStatus = !åpneAksjonspunkter.isEmpty() ? behandlingStegKonfigurasjon.getInngang()
                : behandlingStegKonfigurasjon.getStartet();

            return nyStatus;
        }
    }

    private boolean erForbiInngang(BehandlingStegStatus nåBehandlingStegStatus) {
        return nåBehandlingStegStatus != null && !Objects.equals(behandlingStegKonfigurasjon.getInngang(), nåBehandlingStegStatus);
    }

    /**
     * Returner ny status på pågående steg.
     */
    protected BehandlingStegStatus håndterResultatAvSteg(BehandlingStegModell stegModell, BehandleStegResultat resultat) {

        TransisjonIdentifikator transisjonIdentifikator = resultat.getTransisjon();
        if (transisjonIdentifikator == null) {
            throw new IllegalArgumentException("Utvikler-feil: mangler transisjon");
        }

        StegTransisjon transisjon = behandlingModell.finnTransisjon(transisjonIdentifikator);

        if (FellesTransisjoner.TILBAKEFØRT_TIL_AKSJONSPUNKT.getId().equals(transisjon.getId())) {
            // tilbakefør til tidligere steg basert på hvilke aksjonspunkter er åpne.
            BehandlingStegStatus behandlingStegStatus = håndterTilbakeføringTilTidligereSteg(behandling, stegModell.getBehandlingStegType());
            fyrEventBehandlingStegTilbakeføring();
            return behandlingStegStatus;
        }

        if (FellesTransisjoner.HENLAGT.getId().equals(transisjon.getId())) {
            return behandlingStegKonfigurasjon.getAvbrutt();
        }
        if (transisjon.erFremoverhopp()) {
            return behandlingStegKonfigurasjon.mapTilStatus(BehandlingStegResultat.FREMOVERFØRT);
        }
        if (FellesTransisjoner.UTFØRT.getId().equals(transisjon.getId())) {
            return utledUtgangStegStatus(stegModell.getBehandlingStegType());
        }
        if (FellesTransisjoner.STARTET.getId().equals(transisjon.getId())) {
            return behandlingStegKonfigurasjon.getStartet();
        }
        if (FellesTransisjoner.SETT_PÅ_VENT.getId().equals(transisjon.getId())) {
            return behandlingStegKonfigurasjon.getVenter();
        }
        throw new IllegalArgumentException("Utvikler-feil: ikke-håndtert transisjon " + transisjon.getId());
    }

    private BehandlingStegStatus utledUtgangStegStatus(BehandlingStegType behandlingStegType) {
        BehandlingStegStatus nyStegStatus;
        if (harÅpneAksjonspunkter(behandling, behandlingStegType)) {
            nyStegStatus = behandlingStegKonfigurasjon.getUtgang();
        } else {
            nyStegStatus = behandlingStegKonfigurasjon.getUtført();
        }
        return nyStegStatus;
    }

    private boolean harÅpneAksjonspunkter(Behandling behandling, BehandlingStegType behandlingStegType) {
        List<AksjonspunktDefinisjon> kriterier = behandlingStegType.getAksjonspunktDefinisjonerUtgang();

        List<Aksjonspunkt> utgangsAksjonspunkter = behandling.getÅpneAksjonspunkter().stream()
            .filter(a -> kriterier.contains(a.getAksjonspunktDefinisjon()))
            .collect(Collectors.toList());

        boolean åpneAksjonspunkter = utgangsAksjonspunkter
            .stream()
            .anyMatch((a) -> a.erÅpentAksjonspunkt());

        return åpneAksjonspunkter;
    }

    private BehandlingStegStatus håndterTilbakeføringTilTidligereSteg(Behandling behandling, BehandlingStegType inneværendeBehandlingStegType) {
        BehandlingStegStatus tilbakeførtStegStatus = behandlingStegKonfigurasjon.mapTilStatus(BehandlingStegResultat.TILBAKEFØRT);
        BehandlingStegStatus inneværendeBehandlingStegStatus = behandling.getBehandlingStegStatus();

        List<Aksjonspunkt> åpneAksjonspunkter = behandling.getÅpneAksjonspunkter();
        if (!åpneAksjonspunkter.isEmpty()) {
            List<String> aksjonspunkter = åpneAksjonspunkter.stream().map(a -> a.getAksjonspunktDefinisjon().getKode()).collect(Collectors.toList());
            BehandlingStegModell nesteBehandlingStegModell = behandlingModell.finnTidligsteStegForAksjonspunktDefinisjon(aksjonspunkter);
            Optional<BehandlingStegStatus> nesteStegStatus = behandlingModell.finnStegStatusFor(nesteBehandlingStegModell.getBehandlingStegType(), aksjonspunkter);

            // oppdater inneværende steg
            oppdaterBehandlingStegStatus(inneværendeBehandlingStegType, inneværendeBehandlingStegStatus, tilbakeførtStegStatus);

            // oppdater nytt steg
            BehandlingStegType nesteStegtype = nesteBehandlingStegModell.getBehandlingStegType();
            oppdaterBehandlingStegType(nesteStegtype, nesteStegStatus.isPresent() ? nesteStegStatus.get() : null, tilbakeførtStegStatus);
        }
        return tilbakeførtStegStatus;
    }


    private void reaktiverInaktiveAksjonspunkter(BehandleStegResultat stegResultat) {
        List<AksjonspunktDefinisjon> apFraSteg = stegResultat.getAksjonspunktListe();
        behandling.getAlleAksjonspunkterInklInaktive().stream()
            .filter(ap -> !ap.erAktivt())
            .filter(ap -> apFraSteg.contains(ap.getAksjonspunktDefinisjon()))
            .forEach(ap -> aksjonspunktRepository.reaktiver(ap));
    }

    /**
     * Lagrer nye aksjonspunkt, og gjenåpner dem hvis de alleerede står til avbrutt/utført
     */
    private List<Aksjonspunkt> opprettAksjonspunkter(BehandleStegResultat stegResultat, BehandlingStegType behandlingStegType) {
        List<AksjonspunktResultat> nyeApResultater = stegResultat.getAksjonspunktResultater();

        if (!nyeApResultater.isEmpty()) {
            List<Aksjonspunkt> funnetAksjonspunkter = new ArrayList<>();
            fjernGjensidigEkskluderendeAksjonspunkter(nyeApResultater);
            funnetAksjonspunkter.addAll(leggTilNyeAksjonspunkterPåBehandling(behandlingStegType, nyeApResultater, behandling));
            funnetAksjonspunkter.addAll(reåpneAvbrutteOgUtførteAksjonspunkter(nyeApResultater, behandling));
            return funnetAksjonspunkter;
        } else {
            return new ArrayList<>();
        }
    }

    private void fjernGjensidigEkskluderendeAksjonspunkter(List<AksjonspunktResultat> nyeApResultater) {
        Set<AksjonspunktDefinisjon> nyeApDef = nyeApResultater.stream().map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(toSet());
        List<AksjonspunktDefinisjon> utelukkedeAksjonspunkter = behandling.getAksjonspunkter().stream()
            .flatMap(ap -> ap.getAksjonspunktDefinisjon().getUtelukkendeApdef().stream())
            .filter(utelukkendeApDef -> nyeApDef.contains(utelukkendeApDef))
            .collect(toList());
        // Dersom eksisterende aksjonspunkter på behandling er utelukket av de nye, så må de fjernes
        utelukkedeAksjonspunkter.forEach(utelukketApDef -> aksjonspunktRepository.fjernAksjonspunkt(behandling, utelukketApDef));
    }

    private List<Aksjonspunkt> reåpneAvbrutteOgUtførteAksjonspunkter(List<AksjonspunktResultat> nyeDefinisjoner,
                                                                     Behandling behandling) {

        Map<AksjonspunktDefinisjon, AksjonspunktResultat> aksjonspunktResultatMap = nyeDefinisjoner.stream()
            .collect(Collectors.toMap(AksjonspunktResultat::getAksjonspunktDefinisjon, Function.identity()));

        Set<Aksjonspunkt> skalReåpnes = behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.erUtført() || ap.erAvbrutt())
            .filter(ap -> aksjonspunktResultatMap.get(ap.getAksjonspunktDefinisjon()) != null)
            .collect(Collectors.toSet());

        List<Aksjonspunkt> reåpnedeAksjonspunkter = new ArrayList<>();
        skalReåpnes.forEach((Aksjonspunkt ap) -> {
            aksjonspunktRepository.setReåpnet(ap);
            aksjonspunktResultatMap.get(ap.getAksjonspunktDefinisjon()).getAksjonspunktModifiserer().accept(ap);
            reåpnedeAksjonspunkter.add(ap);
        });

        return reåpnedeAksjonspunkter;
    }

    private List<Aksjonspunkt> leggTilNyeAksjonspunkterPåBehandling(BehandlingStegType behandlingStegType,
                                                                    List<AksjonspunktResultat> nyeDefinisjoner,
                                                                    Behandling behandling) {

        List<AksjonspunktDefinisjon> eksisterendeDefinisjoner = behandling.getAksjonspunkter().stream()
            .map(Aksjonspunkt::getAksjonspunktDefinisjon)
            .collect(Collectors.toList());

        List<AksjonspunktResultat> nyeAksjonspunkt = nyeDefinisjoner.stream()
            .filter(apDefWrapper -> !eksisterendeDefinisjoner.contains(apDefWrapper.getAksjonspunktDefinisjon()))
            .collect(Collectors.toList());

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

    void lagreBehandling(Behandling behandling) {
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    void oppdaterBehandlingStegType(BehandlingStegType nesteStegType, BehandlingStegStatus nesteStegStatus, BehandlingStegStatus sluttStegStatusVedOvergang) {
        Objects.requireNonNull(behandlingRepository, "behandlingRepository");

        BehandlingStegType nåBehandlingSteg = null;
        if (sistBehandlingStegTilstand.isPresent()) {
            nåBehandlingSteg = sistBehandlingStegTilstand.get().getBehandlingSteg();
        }

        if (!erSammeStegSomFør(nesteStegType, nåBehandlingSteg)) {

            // sett status for neste steg
            manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, nesteStegType, nesteStegStatus, sluttStegStatusVedOvergang);
        }
    }

    void settBehandlingStegSomGjeldende(BehandlingStegType nesteStegType, BehandlingStegStatus sluttStegStatusVedOvergang) {
        oppdaterBehandlingStegType(nesteStegType, null, sluttStegStatusVedOvergang);
    }

    private boolean erSammeStegSomFør(BehandlingStegType stegType, BehandlingStegType nåværendeBehandlingSteg) {
        return Objects.equals(nåværendeBehandlingSteg, stegType);
    }

    /**
     * TODO (FC: Trengs denne lenger? Aksjonspunkt har en not-null relasjon til Vurderingspunkt.
     * <p>
     * Verifiser at alle åpne aksjonspunkter har et definert vurderingspunkt i gjenværende steg hvor de må behandles.
     * Sikrer at ikke abstraktpunkt identifiseres ETTER at de skal være håndtert.
     */
    private void guardAlleÅpneAksjonspunkterHarDefinertVurderingspunkt() {
        BehandlingStegType aktivtBehandlingSteg = behandling.getAktivtBehandlingSteg();

        List<Aksjonspunkt> gjenværendeÅpneAksjonspunkt = new ArrayList<>(behandling.getÅpneAksjonspunkter());

        // TODO (FC): Denne bør håndteres med event ved overgang
        behandlingModell.hvertStegFraOgMed(aktivtBehandlingSteg)
            .forEach(bsm -> {
                filterVekkAksjonspunktHåndtertAvFremtidigVurderingspunkt(bsm, gjenværendeÅpneAksjonspunkt);
            });

        if (!gjenværendeÅpneAksjonspunkt.isEmpty()) {
            /*
             * TODO (FC): Lagre og sett behandling på vent i stedet for å kaste exception? Exception mest nyttig i test
             * og
             * utvikling, men i prod bør heller sette behandling til side hvis det skulle være så galt at
             * vurderingspunkt ikke er definert for et identifisert abstraktpunkt.
             */
            throw new IllegalStateException(
                "Utvikler-feil: Det er definert aksjonspunkt [" + //$NON-NLS-1$
                    Aksjonspunkt.getKoder(gjenværendeÅpneAksjonspunkt)
                    + "] som ikke er håndtert av noe steg" //$NON-NLS-1$
                    + (aktivtBehandlingSteg == null ? " i sekvensen " : " fra og med: " + aktivtBehandlingSteg)); //$NON-NLS-1$
        }
    }

    private void filterVekkAksjonspunktHåndtertAvFremtidigVurderingspunkt(BehandlingStegModell bsm, List<Aksjonspunkt> åpneAksjonspunkter) {
        BehandlingStegType stegType = bsm.getBehandlingStegType();
        List<AksjonspunktDefinisjon> inngangKriterier = stegType.getAksjonspunktDefinisjonerInngang();
        List<AksjonspunktDefinisjon> utgangKriterier = stegType.getAksjonspunktDefinisjonerUtgang();
        åpneAksjonspunkter.removeIf(elem -> {
            AksjonspunktDefinisjon elemAksDef = elem.getAksjonspunktDefinisjon();
            return elem.erÅpentAksjonspunkt() && (inngangKriterier.contains(elemAksDef) || utgangKriterier.contains(elemAksDef));
        });
    }

}
