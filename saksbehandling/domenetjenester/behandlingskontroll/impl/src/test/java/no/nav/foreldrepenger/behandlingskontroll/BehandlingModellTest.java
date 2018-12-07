package no.nav.foreldrepenger.behandlingskontroll;

import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunktMedCallback;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.StegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@SuppressWarnings("resource")
@RunWith(CdiRunner.class)
public class BehandlingModellTest {

    private static final LocalDateTime FRIST_TID = LocalDateTime.now().plusWeeks(4);

    private final BehandlingType behandlingType = BehandlingType.FØRSTEGANGSSØKNAD;
    private final FagsakYtelseType fagsakYtelseType = FagsakYtelseType.FORELDREPENGER;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private static final BehandlingStegType STEG_1 = BehandlingStegType.INNHENT_REGISTEROPP;
    private static final BehandlingStegType STEG_2 = BehandlingStegType.KONTROLLER_FAKTA;
    private static final BehandlingStegType STEG_3 = BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR;
    private static final BehandlingStegType STEG_4 = BehandlingStegType.FATTE_VEDTAK;

    @Inject
    private BehandlingskontrollTjeneste kontrollTjeneste;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private BehandlingskontrollRepository behandlingskontrollRepository;

    @Inject
    private AksjonspunktRepository aksjonspunktRepository;

    private final DummySteg nullSteg = new DummySteg();
    private final DummySteg aksjonspunktSteg = new DummySteg(opprettForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS));
    private final DummySteg aksjonspunktModifisererSteg = new DummySteg(opprettForAksjonspunktMedCallback(
        AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS, (ap) -> {
            aksjonspunktRepository.setFrist(ap, FRIST_TID, Venteårsak.AVV_DOK);
        }));

    @Test
    public void skal_finne_aksjonspunkter_som_ligger_etter_et_gitt_steg() {
        // Arrange - noen utvalge, tilfeldige aksjonspunkter
        AksjonspunktDefinisjon a0_0 = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        AksjonspunktDefinisjon a0_1 = AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
        AksjonspunktDefinisjon a1_0 = AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD;
        AksjonspunktDefinisjon a1_1 = AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT;
        AksjonspunktDefinisjon a2_0 = AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE;
        AksjonspunktDefinisjon a2_1 = AksjonspunktDefinisjon.AVKLAR_TILLEGGSOPPLYSNINGER;

        DummySteg steg = new DummySteg();
        DummySteg steg0 = new DummySteg();
        DummySteg steg1 = new DummySteg();
        DummySteg steg2 = new DummySteg();

        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, steg, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, steg0, ap(a0_0), ap(a0_1)),
            new TestStegKonfig(STEG_3, behandlingType, fagsakYtelseType, steg1, ap(a1_0), ap(a1_1)),
            new TestStegKonfig(STEG_4, behandlingType, fagsakYtelseType, steg2, ap(a2_0), ap(a2_1)));

        BehandlingModellImpl modell = setupModell(modellData);

        Set<String> ads = null;

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_1);

        assertThat(ads).

            containsOnly(a0_0.getKode(), a0_1.

                getKode(), a1_0.

                    getKode(),
                a1_1.

                    getKode(),
                a2_0.

                    getKode(),
                a2_1.

                    getKode());

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_2);

        assertThat(ads).

            containsOnly(a1_0.getKode(), a1_1.

                getKode(), a2_0.

                    getKode(),
                a2_1.

                    getKode());

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_3);

        assertThat(ads).

            containsOnly(a2_0.getKode(), a2_1.

                getKode());

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_4);

        assertThat(ads).

            isEmpty();

    }

    @Test
    public void skal_finne_aksjonspunkter_ved_inngang_eller_utgang_av_steg() {
        // Arrange - noen utvalge, tilfeldige aksjonspunkter
        AksjonspunktDefinisjon a0_0 = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        AksjonspunktDefinisjon a0_1 = AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
        AksjonspunktDefinisjon a1_0 = AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD;
        AksjonspunktDefinisjon a1_1 = AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT;

        DummySteg steg = new DummySteg();
        DummySteg steg0 = new DummySteg();
        DummySteg steg1 = new DummySteg();

        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, steg, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, steg0, ap(a0_0), ap(a0_1)),
            new TestStegKonfig(STEG_3, behandlingType, fagsakYtelseType, steg1, ap(a1_0), ap(a1_1)));

        BehandlingModellImpl modell = setupModell(modellData);

        Set<String> ads = null;

        ads = modell.finnAksjonspunktDefinisjonerInngang(STEG_1);
        assertThat(ads).isEmpty();

        ads = modell.finnAksjonspunktDefinisjonerInngang(STEG_2);
        assertThat(ads).containsOnly(a0_0.getKode());

        ads = modell.finnAksjonspunktDefinisjonerUtgang(STEG_3);
        assertThat(ads).containsOnly(a1_1.getKode());

    }

    @Test
    public void skal_stoppe_på_steg_2_når_får_aksjonspunkt() throws Exception {
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, aksjonspunktSteg, ap(), ap(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS)),
            new TestStegKonfig(STEG_3, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()));
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        BehandlingStegVisitorUtenLagring visitor = lagVisitor(scenario, modell);
        BehandlingStegUtfall siste = modell.prosesserFra(STEG_1, visitor);

        assertThat(siste.getStegType()).isEqualTo(STEG_2);
        assertThat(visitor.kjørteSteg).isEqualTo(Arrays.asList(STEG_1, STEG_2));
    }

    public List<AksjonspunktDefinisjon> ap(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        return Arrays.asList(aksjonspunktDefinisjoner);
    }

    @Test
    public void skal_kjøre_til_siste_når_ingen_gir_aksjonspunkt() throws Exception {
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()),
            new TestStegKonfig(STEG_3, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()));
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        BehandlingStegVisitorUtenLagring visitor = lagVisitor(scenario, modell);
        BehandlingStegUtfall siste = modell.prosesserFra(STEG_1, visitor);

        assertThat(siste).isNull();
        assertThat(visitor.kjørteSteg).isEqualTo(Arrays.asList(STEG_1, STEG_2, STEG_3));
    }

    @Test
    public void tilbakefører_til_tidligste_steg_med_åpent_aksjonspunkt() {
        AksjonspunktDefinisjon avklarFødsel = AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
        DummySteg tilbakeføringssteg = new DummySteg(true, opprettForAksjonspunkt(avklarFødsel));
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, nullSteg, ap(avklarFødsel), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()),
            new TestStegKonfig(STEG_3, behandlingType, fagsakYtelseType, tilbakeføringssteg, ap(), ap()));
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        BehandlingStegVisitorUtenLagring visitor = lagVisitor(scenario, modell);

        Behandling behandling = visitor.getBehandling();

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS,
            STEG_1);
        aksjonspunktRepository.setReåpnet(aksjonspunkt);

        BehandlingStegUtfall siste = modell.prosesserFra(STEG_3, visitor);
        assertThat(siste.getStegType()).isEqualTo(STEG_3);
        assertThat(getAktivtBehandlingSteg(behandling)).isEqualTo(STEG_1);
    }

    private BehandlingStegType getAktivtBehandlingSteg(Behandling behandling) {
        return behandlingskontrollRepository.getBehandlingskontrollTilstand(behandling.getId()).getStegType();
    }

    @Test
    public void finner_tidligste_steg_for_aksjonspunkter() {
        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, nullSteg, ap(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS), ap()),
            new TestStegKonfig(STEG_3, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()));

        BehandlingModellImpl modell = setupModell(modellData);
        Set<AksjonspunktDefinisjon> aksjonspunktDefinisjoner = new HashSet<>();
        aksjonspunktDefinisjoner.add(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS);
        BehandlingStegModell behandlingStegModell = modell.finnTidligsteStegFor(aksjonspunktDefinisjoner);
        assertThat(behandlingStegModell.getBehandlingStegType()).isEqualTo(STEG_1);
    }

    @Test
    public void skal_modifisere_aksjonspunktet_ved_å_kalle_funksjon_som_legger_til_frist() throws Exception {
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, aksjonspunktModifisererSteg, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, nullSteg, ap(), ap(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS)),
            new TestStegKonfig(STEG_3, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()));
        BehandlingModellImpl modell = setupModell(modellData);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(scenario, modell);

        // Act
        modell.prosesserFra(STEG_1, visitor);

        // Assert
        assertThat(visitor.getBehandling().getÅpneAksjonspunkter().size()).isEqualTo(1);
        assertThat(visitor.getBehandling().getÅpneAksjonspunkter().get(0).getFristTid()).isEqualTo(FRIST_TID);
    }

    @Test
    public void skal_reaktiveree_aksjonspunkt_som_steget_har_som_resultat() throws Exception {
        AksjonspunktDefinisjon apd = AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
        DummySteg stegSomOpretterAksjonspunkt = new DummySteg(true, opprettForAksjonspunkt(apd));
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, stegSomOpretterAksjonspunkt, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, nullSteg, ap(), ap(apd)),
            new TestStegKonfig(STEG_4, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()));
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        BehandlingStegVisitorUtenLagring visitor = lagVisitor(scenario, modell);

        Behandling behandling = visitor.getBehandling();

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, apd, STEG_2);
        aksjonspunktRepository.setTilUtført(aksjonspunkt, "ferdig");
        aksjonspunktRepository.deaktiver(aksjonspunkt);

        BehandlingStegUtfall siste = modell.prosesserFra(STEG_1, visitor);
        assertThat(siste.getStegType()).isEqualTo(STEG_1);
        assertThat(getAktivtBehandlingSteg(behandling)).isEqualTo(STEG_2);

        assertThat(behandling.getAksjonspunktFor(apd).erAktivt()).isTrue();
    }

    @Test
    public void skal_ikke_reaktivere_aksjonpunkt_som_ikke_er_fra_stegets_resultat() throws Exception {
        AksjonspunktDefinisjon apd = AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
        AksjonspunktDefinisjon apd2 = AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD;
        DummySteg stegSomOpretterAksjonspunkt = new DummySteg(true, opprettForAksjonspunkt(apd));
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, stegSomOpretterAksjonspunkt, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, nullSteg, ap(), ap(apd, apd2)),
            new TestStegKonfig(STEG_4, behandlingType, fagsakYtelseType, nullSteg, ap(), ap()));
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        BehandlingStegVisitorUtenLagring visitor = lagVisitor(scenario, modell);

        Behandling behandling = visitor.getBehandling();

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, apd, STEG_2);
        aksjonspunktRepository.setTilUtført(aksjonspunkt, "ferdig");
        aksjonspunktRepository.deaktiver(aksjonspunkt);

        Aksjonspunkt aksjonspunkt2 = aksjonspunktRepository.leggTilAksjonspunkt(behandling, apd2, STEG_2);
        aksjonspunktRepository.setTilUtført(aksjonspunkt2, "ferdig");
        aksjonspunktRepository.deaktiver(aksjonspunkt2);

        BehandlingStegUtfall siste = modell.prosesserFra(STEG_1, visitor);
        assertThat(siste.getStegType()).isEqualTo(STEG_1);
        assertThat(getAktivtBehandlingSteg(behandling)).isEqualTo(STEG_2);

        assertThat(behandling.getAksjonspunktFor(apd).erAktivt()).isTrue();
        Aksjonspunkt ap2 = behandling.getAlleAksjonspunkterInklInaktive().stream()
            .filter(a -> a.getAksjonspunktDefinisjon().equals(apd2))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Mangler aksjonspunkt med def " + apd2));
        assertThat(ap2.erAktivt()).isFalse();
    }

    private BehandlingModellImpl setupModell(List<TestStegKonfig> resolve) {
        return ModifiserbarBehandlingModell.setupModell(behandlingType, fagsakYtelseType, resolve);
    }

    private BehandlingStegVisitorUtenLagring lagVisitor(ScenarioMorSøkerEngangsstønad scenario, BehandlingModellImpl modell) {
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling);
        return new BehandlingStegVisitorUtenLagring(repositoryProvider, kontrollTjeneste, kontekst, modell,
            BehandlingskontrollEventPubliserer.NULL_EVENT_PUB, behandling);
    }

    static class BehandlingStegVisitorUtenLagring extends BehandlingStegVisitor implements BehandlingModellVisitor {
        List<BehandlingStegType> kjørteSteg = new ArrayList<>();

        BehandlingStegVisitorUtenLagring(BehandlingRepositoryProvider repositoryProvider,
                                         BehandlingskontrollTjeneste tjeneste, BehandlingskontrollKontekst kontekst, BehandlingModellImpl behandlingModell,
                                         BehandlingskontrollEventPubliserer eventPubliserer,
                                         Behandling behandling) {
            super(repositoryProvider, behandling, tjeneste, behandlingModell, kontekst, eventPubliserer, null);
        }

        @Override
        protected void settBehandlingStegSomGjeldende(StegTilstand stegTilstand) {
            super.settBehandlingStegSomGjeldende(stegTilstand);
            kjørteSteg.add(stegTilstand.getStegType());
        }

        @Override
        public BehandlingStegProsesseringResultat prosesser(BehandlingStegModell stegModell) {
            super.markerOvergangTilNyttSteg(stegModell.getBehandlingStegType());
            BehandlingStegProsesseringResultat resultat = super.prosesser(stegModell);
            return resultat;
        }
    }
}
