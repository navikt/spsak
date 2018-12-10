package no.nav.foreldrepenger.behandlingskontroll;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellImpl.TriFunction;
import no.nav.foreldrepenger.behandlingskontroll.ModifiserbarBehandlingModell.ModifiserbarBehandlingStegType;
import no.nav.foreldrepenger.behandlingskontroll.ModifiserbarBehandlingModell.ModifiserbarVurderingspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingskontrollTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.StegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BehandlingskontrollTjenesteImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private final class BehandlingskontrollEventPublisererForTest extends BehandlingskontrollEventPubliserer {
        private List<BehandlingEvent> events = new ArrayList<>();

        @Override
        protected void doFireEvent(BehandlingEvent event) {
            events.add(event);
        }
    }

    static class BehandlingModellForTest {
        BehandlingType behandlingType = BehandlingType.FØRSTEGANGSSØKNAD;
        FagsakYtelseType fagsakYtelseType = FagsakYtelseType.FORELDREPENGER;

        // random liste av aksjonspunkt og steg i en definert rekkefølge for å kunne sette opp modellen
        AksjonspunktDefinisjon a2_0 = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        AksjonspunktDefinisjon a2_1 = AksjonspunktDefinisjon.AVKLAR_FORTSATT_MEDLEMSKAP;
        AksjonspunktDefinisjon a3_0 = AksjonspunktDefinisjon.VURDER_ARBEIDSFORHOLD;
        AksjonspunktDefinisjon a3_1 = AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
        AksjonspunktDefinisjon a4_0 = AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE;
        AksjonspunktDefinisjon a4_1 = AksjonspunktDefinisjon.AVKLAR_TILLEGGSOPPLYSNINGER;
        AksjonspunktDefinisjon a5_0 = AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT;
        AksjonspunktDefinisjon a5_1 = AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD;

        DummySteg steg1 = new DummySteg();
        DummySteg steg2 = new DummySteg();
        DummySteg steg3 = new DummySteg();
        DummySteg steg4 = new DummySteg();
        DummySteg steg5 = new DummySteg();

        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, fagsakYtelseType, steg1, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, fagsakYtelseType, steg2, ap(a2_0), ap(a2_1)),
            new TestStegKonfig(STEG_4, behandlingType, fagsakYtelseType, steg4, ap(a4_0), ap(a4_1)),
            new TestStegKonfig(STEG_5, behandlingType, fagsakYtelseType, steg5, ap(a5_0), ap(a5_1)));

        BehandlingModellImpl modell = setupModell(behandlingType, fagsakYtelseType, modellData);
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private BehandlingModellForTest behandlingModellForTest = new BehandlingModellForTest();

    private static final BehandlingStegType STEG_1 = BehandlingStegType.INNHENT_REGISTEROPP;
    private static final BehandlingStegType STEG_2 = BehandlingStegType.KONTROLLER_FAKTA;
    private static final BehandlingStegType STEG_4 = BehandlingStegType.FATTE_VEDTAK;
    private static final BehandlingStegType STEG_5 = BehandlingStegType.IVERKSETT_VEDTAK;

    private BehandlingskontrollTjeneste kontrollTjeneste;

    private Behandling behandling;

    private BehandlingskontrollKontekst kontekst;

    private BehandlingskontrollEventPublisererForTest eventPubliserer = new BehandlingskontrollEventPublisererForTest();

    @Inject
    private KodeverkRepository kodeverkRepository;

    @Inject
    private InternalManipulerBehandling manipulerInternBehandling;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private BehandlingskontrollRepository behandlingskontrollRepository;

    @Before
    public void setup() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        behandling = scenario.lagre(repositoryProvider);

        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, STEG_2);

        initBehandlingskontrollTjeneste(this.behandlingModellForTest.modell);

        kontekst = Mockito.mock(BehandlingskontrollKontekst.class);
        Mockito.when(kontekst.getBehandlingId()).thenReturn(behandling.getId());
        Mockito.when(kontekst.getFagsakId()).thenReturn(behandling.getFagsakId());
    }

    @Test
    public void skal_rykke_tilbake_til_inngang_vurderingspunkt_av_steg() {

        String steg2InngangAksjonspunkt = this.behandlingModellForTest.a2_0.getKode();

        kontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, Arrays.asList(steg2InngangAksjonspunkt), false);

        assertThat(getBehandlingSteg()).isEqualTo(STEG_2);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        assertThat(getBehandlingStegTilstand()).isNotNull();
        assertThat(getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(STEG_2, BehandlingStegStatus.TILBAKEFØRT,
            BehandlingStegStatus.INNGANG);

    }

    @Test
    public void skal_rykke_tilbake_til_utgang_vurderingspunkt_av_steg() {

        String steg2UtgangAksjonspunkt = this.behandlingModellForTest.a2_1.getKode();

        kontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, Arrays.asList(steg2UtgangAksjonspunkt), false);

        assertThat(getBehandlingSteg()).isEqualTo(STEG_2);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.UTGANG);
        assertThat(getBehandlingStegTilstand()).isNotNull();

        assertThat(getBehandlingStegTilstandSteg2()).isPresent();
        assertThat(getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(STEG_2, BehandlingStegStatus.TILBAKEFØRT,
            BehandlingStegStatus.UTGANG);

    }

    @Test
    public void skal_rykke_tilbake_til_start_av_tidligere_steg_ved_tilbakeføring() {

        kontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, STEG_2);

        assertThat(getBehandlingSteg()).isEqualTo(STEG_2);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        assertThat(getBehandlingStegTilstand()).isNotNull();

        assertThat(getBehandlingStegTilstandSteg2()).isPresent();
        assertThat(getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(STEG_2, BehandlingStegStatus.TILBAKEFØRT,
            BehandlingStegStatus.INNGANG);

    }

    @Test
    public void skal_tolerere_tilbakehopp_til_senere_steg_enn_inneværende() {

        kontrollTjeneste.behandlingTilbakeføringHvisTidligereBehandlingSteg(kontekst, STEG_4);

        assertThat(getBehandlingSteg()).isEqualTo(STEG_2);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(getBehandlingStegStatus()).isNull();
        assertThat(getBehandlingStegTilstand()).isNotNull();

        assertThat(getBehandlingStegTilstandSteg2()).isPresent();
        assertThat(getBehandlingStegTilstandSteg4()).isNotPresent();
        assertThat(getBehandlingStegTilstandHistorikk()).hasSize(1);
    }

    @Test
    public void skal_flytte_til__inngang_av_senere_steg_ved_framføring() {

        kontrollTjeneste.behandlingFramføringTilSenereBehandlingSteg(kontekst, STEG_5);

        assertThat(getBehandlingSteg()).isEqualTo(STEG_5);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.IVERKSETTER_VEDTAK);
        assertThat(getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        assertThat(getBehandlingStegTilstand()).isNotNull();

        assertThat(getBehandlingStegTilstandSteg5()).isPresent();
        assertThat(getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(STEG_2, BehandlingStegStatus.AVBRUTT);

        // NB: skipper STEP_4
        sjekkBehandlingStegTilstandHistorikk(STEG_4);

        sjekkBehandlingStegTilstandHistorikk(STEG_5, BehandlingStegStatus.INNGANG);

    }

    @Test(expected = IllegalStateException.class)
    public void skal_kaste_exception_dersom_ugyldig_tilbakeføring() {
        kontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, STEG_4);

    }

    @Test
    public void skal_rykke_tilbake_til_inngang_vurderingspunkt_av_samme_steg() {

        // Arrange
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, STEG_4, BehandlingStegStatus.UTGANG,
            BehandlingStegStatus.AVBRUTT);
        
        lagreBehandling(behandling);

        assertThat(getBehandlingSteg()).isEqualTo(STEG_4);
        assertThat(getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.UTGANG);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.FATTER_VEDTAK);

        String steg4InngangAksjonspunkt = this.behandlingModellForTest.a4_0.getKode();

        // Act
        kontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, Arrays.asList(steg4InngangAksjonspunkt), false);

        // Assert
        assertThat(getBehandlingSteg()).isEqualTo(STEG_4);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.FATTER_VEDTAK);
        assertThat(getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        assertThat(getBehandlingStegTilstand()).isNotNull();

        assertThat(getBehandlingStegTilstandHistorikk()).hasSize(3);

        sjekkBehandlingStegTilstandHistorikk(
            STEG_4, BehandlingStegStatus.TILBAKEFØRT, BehandlingStegStatus.INNGANG);

        assertThat(getBehandlingStegTilstandSteg4().get().getStatus()).isEqualTo(BehandlingStegStatus.INNGANG);

    }

    private Long lagreBehandling(Behandling behandling) {
        return repositoryProvider.getBehandlingRepository().lagre(behandling, repositoryProvider.getBehandlingLåsRepository().taLås(behandling.getId()));
    }

    @Test
    public void skal_ha_guard_mot_nøstet_behandlingskontroll_ved_prossesering_tilbakeføring_og_framføring() throws Exception {

        BehandlingModellRepository behandlingModellRepository = Mockito.mock(BehandlingModellRepository.class);
        Mockito.when(behandlingModellRepository.getModell(Mockito.any(), Mockito.any())).thenReturn(this.behandlingModellForTest.modell);
        this.kontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, behandlingModellRepository,
            eventPubliserer) {
            @Override
            protected BehandlingStegUtfall doProsesserBehandling(BehandlingskontrollKontekst kontekst, BehandlingModell modell,
                                                                 Behandling behandling, BehandlingStegType startFraBehandlingStegType) {

                kontrollTjeneste.prosesserBehandling(kontekst);
                return null;
            }
        };

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Støtter ikke nøstet prosessering");

        this.kontrollTjeneste.prosesserBehandling(kontekst);
    }

    @Test
    public void skal_returnere_true_når_aksjonspunktet_skal_løses_etter_angitt_steg() {
        assertThat(kontrollTjeneste.skalAksjonspunktReaktiveresIEllerEtterSteg(behandling, STEG_4, AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT)).isTrue();
    }

    @Test
    public void skal_returnere_true_når_aksjonspunktet_skal_løses_i_angitt_steg() {
        assertThat(kontrollTjeneste.skalAksjonspunktReaktiveresIEllerEtterSteg(behandling, STEG_4, AksjonspunktDefinisjon.AVKLAR_TILLEGGSOPPLYSNINGER))
            .isTrue();
    }

    @Test
    public void skal_returnere_false_når_aksjonspunktet_skal_løses_før_angitt_steg() {
        assertThat(kontrollTjeneste.skalAksjonspunktReaktiveresIEllerEtterSteg(behandling, STEG_4, AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS))
            .isFalse();
    }

    private void sjekkBehandlingStegTilstandHistorikk(BehandlingStegType stegType, BehandlingStegStatus... stegStatuser) {
        assertThat(
            getBehandlingStegTilstandHistorikk().stream()
                .filter(bst -> stegType == null || Objects.equals(bst.getStegType(), stegType))
                .map(bst -> bst.getStatus()))
                    .containsExactly(stegStatuser);
    }

    private static List<AksjonspunktDefinisjon> ap(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        return Arrays.asList(aksjonspunktDefinisjoner);
    }

    private static BehandlingModellImpl setupModell(BehandlingType behandlingType, FagsakYtelseType fagsakYtelseType, List<TestStegKonfig> resolve) {
        TriFunction<BehandlingStegType, BehandlingType, FagsakYtelseType, BehandlingSteg> finnSteg = DummySteg.map(resolve);

        BehandlingModellImpl modell = new BehandlingModellImpl(behandlingType, fagsakYtelseType, finnSteg);
        for (TestStegKonfig konfig : resolve) {
            BehandlingStegType stegType = konfig.getBehandlingStegType();

            // fake legg til behandlingSteg og vureringspunkter
            ModifiserbarBehandlingStegType modStegType = ModifiserbarBehandlingModell.fra(stegType);
            modell.leggTil(modStegType, behandlingType, fagsakYtelseType);

            ModifiserbarVurderingspunktDefinisjon modVurderingspunktInngang = ModifiserbarBehandlingModell.fra(modStegType,
                VurderingspunktDefinisjon.Type.INNGANG);
            modStegType.leggTilVurderingspunkt(modVurderingspunktInngang);
            modVurderingspunktInngang.leggTil(konfig.getInngangAksjonspunkter());

            ModifiserbarVurderingspunktDefinisjon modVurderingspunktUtgang = ModifiserbarBehandlingModell.fra(modStegType,
                VurderingspunktDefinisjon.Type.UTGANG);
            modStegType.leggTilVurderingspunkt(modVurderingspunktUtgang);
            modVurderingspunktUtgang.leggTil(konfig.getUtgangAksjonspunkter());

            modell.internFinnSteg(stegType).leggTilVurderingspunktInngang(Optional.of(modVurderingspunktInngang));
            modell.internFinnSteg(stegType).leggTilVurderingspunktUtgang(Optional.of(modVurderingspunktUtgang));

        }
        return modell;

    }

    private void initBehandlingskontrollTjeneste(BehandlingModellImpl modell) {
        BehandlingModellRepository behandlingModellRepository = Mockito.mock(BehandlingModellRepository.class);
        Mockito.when(behandlingModellRepository.getModell(Mockito.any(), Mockito.any())).thenReturn(modell);
        Mockito.when(behandlingModellRepository.getBehandlingStegKonfigurasjon()).thenReturn(BehandlingStegKonfigurasjon.lagDummy());
        Mockito.when(behandlingModellRepository.getKodeverkRepository()).thenReturn(kodeverkRepository);
        this.kontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, behandlingModellRepository, eventPubliserer);
    }

    private List<StegTilstand> getBehandlingStegTilstandHistorikk() {
        return behandlingskontrollRepository.getBehandlingStegTilstandHistorikk(behandling.getId())
                .stream()
                .map(StegTilstand::fra)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private Optional<StegTilstand> getBehandlingStegTilstand() {
        var tilstand = behandlingskontrollRepository.getBehandlingskontrollTilstand(behandling.getId());
        return StegTilstand.fra(tilstand);
    }

    private BehandlingStegStatus getBehandlingStegStatus() {
        return getBehandlingskontrollTilstand().getStegStatus();
    }

    private BehandlingStegType getBehandlingSteg() {
        return getBehandlingskontrollTilstand().getStegType();
    }

    private BehandlingskontrollTilstand getBehandlingskontrollTilstand() {
        return behandlingskontrollRepository.getBehandlingskontrollTilstand(behandling.getId());
    }

    private Optional<StegTilstand> getAktivtBehandlingSteg(BehandlingStegType stegType) {
        var tilstand = behandlingskontrollRepository.getBehandlingskontrollTilstand(behandling.getId());
        if (tilstand.erSteg(stegType)) {
            return StegTilstand.fra(tilstand);
        } else {
            return Optional.empty();
        }
    }

    private Optional<StegTilstand> getBehandlingStegTilstandSteg4() {
        return getAktivtBehandlingSteg(STEG_4);
    }

    private Optional<StegTilstand> getBehandlingStegTilstandSteg2() {
        return getAktivtBehandlingSteg(STEG_2);
    }

    private Optional<StegTilstand> getBehandlingStegTilstandSteg5() {
        return getAktivtBehandlingSteg(STEG_5);
    }

}
