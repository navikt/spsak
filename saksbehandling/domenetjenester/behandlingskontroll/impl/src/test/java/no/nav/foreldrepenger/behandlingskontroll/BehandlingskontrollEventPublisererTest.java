package no.nav.foreldrepenger.behandlingskontroll;

import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BehandlingskontrollEventPublisererTest {
    private final BehandlingType behandlingType = BehandlingType.FØRSTEGANGSSØKNAD;
    private final FagsakYtelseType fagsakYtelseType = FagsakYtelseType.FORELDREPENGER;

    private static final BehandlingStegType STEG_1 = BehandlingStegType.INNHENT_REGISTEROPP;
    private static final BehandlingStegType STEG_2 = BehandlingStegType.KONTROLLER_FAKTA;
    private static final BehandlingStegType STEG_3 = BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR;
    private static final BehandlingStegType STEG_4 = BehandlingStegType.FATTE_VEDTAK;

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    BehandlingskontrollEventPubliserer eventPubliserer;

    @Inject
    BehandlingRepositoryProvider repositoryProvider;

    @Inject
    BehandlingModellRepository behandlingModellRepository;

    @Inject
    AksjonspunktRepository aksjonspunktRepository;

    @Inject
    HistorikkRepository historikkRepository;

    @Inject
    FagsakLåsRepository fagsakLåsRepository;

    // No Inject
    BehandlingskontrollTjenesteImpl kontrollTjeneste;

    @Before
    public void setup() {
        BehandlingModellImpl behandlingModell = byggModell();

        kontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, behandlingModellRepository, eventPubliserer) {
            @Override
            protected BehandlingModellImpl getModell(Behandling behandling) {
                return behandlingModell;
            }
        };

        TestEventObserver.startCapture();
    }

    @After
    public void after() {
        TestEventObserver.reset();
    }

    @Test
    public void skal_fyre_event_for_aksjonspunkt_funnet_ved_prosessering() throws Exception {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        BehandlingStegType stegType = BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR;

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT, stegType);
        kontrollTjeneste.aksjonspunkterFunnet(kontekst, stegType, Arrays.asList(aksjonspunkt));

        AksjonspunktDefinisjon[] ads = {AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT};
        TestEventObserver.containsExactly(ads);
    }

    @Test
    public void skal_fyre_event_for_behandlingskontroll_startet_stoppet_ved_prosessering() throws Exception {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = nyttScenario(STEG_1);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert

        BehandlingskontrollEvent startEvent = new BehandlingskontrollEvent.StartetEvent(null, null, null, STEG_1, null);
        BehandlingskontrollEvent stoppEvent = new BehandlingskontrollEvent.StoppetEvent(null, null, null, STEG_4,
            BehandlingStegStatus.INNGANG);
        TestEventObserver.containsExactly(startEvent, stoppEvent);

    }

    @Test
    public void skal_fyre_event_for_behandlingskontroll_behandlingsteg_status_endring_ved_prosessering() throws Exception {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = nyttScenario(STEG_1);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert

        BehandlingStegStatusEvent steg1StatusEvent0 = new BehandlingStegStatusEvent(kontekst, STEG_1, null,
            BehandlingStegStatus.STARTET);
        BehandlingStegStatusEvent steg1StatusEvent1 = new BehandlingStegStatusEvent(kontekst, STEG_1, BehandlingStegStatus.STARTET,
            BehandlingStegStatus.UTFØRT);
        BehandlingStegStatusEvent steg2StatusEvent0 = new BehandlingStegStatusEvent(kontekst, STEG_2, null,
            BehandlingStegStatus.STARTET);
        BehandlingStegStatusEvent steg2StatusEvent = new BehandlingStegStatusEvent(kontekst, STEG_2, BehandlingStegStatus.STARTET,
            BehandlingStegStatus.UTFØRT);
        BehandlingStegStatusEvent steg3StatusEvent0 = new BehandlingStegStatusEvent(kontekst, STEG_2, null,
            BehandlingStegStatus.STARTET);
        BehandlingStegStatusEvent steg3StatusEvent = new BehandlingStegStatusEvent(kontekst, STEG_3, BehandlingStegStatus.STARTET,
            BehandlingStegStatus.UTFØRT);
        BehandlingStegStatusEvent steg4StatusEvent = new BehandlingStegStatusEvent(kontekst, STEG_4, null,
            BehandlingStegStatus.INNGANG);
        TestEventObserver.containsExactly(steg1StatusEvent0, steg1StatusEvent1 //
            , steg2StatusEvent0, steg2StatusEvent//
            , steg3StatusEvent0, steg3StatusEvent//
            , steg4StatusEvent//
        );
    }

    @Test
    public void skal_fyre_event_for_behandlingskontroll_tilbakeføring_ved_prosessering() throws Exception {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = nyttScenario(STEG_3);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE, STEG_4);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert
        // TODO (essv): Vanskelig å overstyre SUT til å gjøre tilbakehopp i riktig retning, her gjøres det fremover.
        // Den trenger et åpent aksjonspunkt som ligger før startsteget
        BehandlingStegOvergangEvent tilbakeføring3_4 = nyOvergangEvent(kontekst, behandling, STEG_3, BehandlingStegStatus.UTFØRT, STEG_4, BehandlingStegStatus.UTFØRT);
        TestEventObserver.containsExactly(tilbakeføring3_4);
    }

    @Test
    public void skal_fyre_event_for_behandlingskontroll_behandlingsteg_overgang_ved_prosessering() throws Exception {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = nyttScenario(STEG_1);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert

        BehandlingStegOvergangEvent overgang1_2 = nyOvergangEvent(kontekst, behandling, STEG_1, BehandlingStegStatus.UTFØRT, STEG_2, BehandlingStegStatus.UTFØRT);
        BehandlingStegOvergangEvent overgang2_3 = nyOvergangEvent(kontekst, behandling, STEG_2, BehandlingStegStatus.UTFØRT, STEG_3, BehandlingStegStatus.UTFØRT);
        BehandlingStegOvergangEvent overgang3_4 = nyOvergangEvent(kontekst, behandling, STEG_3, BehandlingStegStatus.UTFØRT, STEG_4, BehandlingStegStatus.UTFØRT);
        TestEventObserver.containsExactly(overgang1_2, overgang2_3, overgang3_4);
    }

    protected ScenarioMorSøkerEngangsstønad nyttScenario(BehandlingStegType startSteg) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.medBehandlingStegStart(startSteg);
        return scenario;
    }

    private BehandlingStegOvergangEvent nyOvergangEvent(BehandlingskontrollKontekst kontekst, Behandling behandling,
                                                        BehandlingStegType steg1, BehandlingStegStatus steg1Status, BehandlingStegType steg2, BehandlingStegStatus steg2Status) {
        return new BehandlingStegOvergangEvent(kontekst, lagTilstand(behandling, steg1, steg1Status),
            lagTilstand(behandling, steg2, steg2Status));
    }

    private Optional<BehandlingStegTilstand> lagTilstand(Behandling behandling, BehandlingStegType stegType,
                                                         BehandlingStegStatus stegStatus) {
        return Optional.of(new BehandlingStegTilstand(behandling, stegType, stegStatus));
    }

    private BehandlingModellImpl byggModell() {
        // Arrange - noen utvalge, tilfeldige aksjonspunkter
        AksjonspunktDefinisjon a0_0 = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        AksjonspunktDefinisjon a0_1 = AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD;
        AksjonspunktDefinisjon a1_0 = AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT;
        AksjonspunktDefinisjon a1_1 = AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
        AksjonspunktDefinisjon a2_0 = AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE;
        AksjonspunktDefinisjon a2_1 = AksjonspunktDefinisjon.AVKLAR_TILLEGGSOPPLYSNINGER;

        DummySteg steg = new DummySteg();
        DummySteg steg0 = new DummySteg(opprettForAksjonspunkt(a2_0));
        DummySteg steg1 = new DummySteg();
        DummySteg steg2 = new DummySteg();

        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, steg, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, steg0, ap(a0_0), ap(a0_1)),
            new TestStegKonfig(STEG_3, behandlingType, fagsakYtelseType, steg1, ap(a1_0), ap(a1_1)),
            new TestStegKonfig(STEG_4, behandlingType, fagsakYtelseType, steg2, ap(a2_0), ap(a2_1))
        );

        return ModifiserbarBehandlingModell.setupModell(behandlingType, fagsakYtelseType, modellData);
    }

    private List<AksjonspunktDefinisjon> ap(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        return Arrays.asList(aksjonspunktDefinisjoner);
    }
}
