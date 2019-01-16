package no.nav.foreldrepenger.web.app.tjenester.behandling.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandling.revurdering.fp.RevurderingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BehandlingsutredningApplikasjonTjenesteImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private BehandlingRepository behandlingRepository;

    @Mock
    private HistorikkTjenesteAdapter historikkApplikasjonTjenesteMock;

    @Mock
    private BehandlingModellRepository behandlingModellRepositoryMock;

    @Mock
    private RevurderingTjeneste revurderingTjenesteMock;

    @Mock
    private RevurderingTjenesteProvider revurderingTjenesteProviderMock;

    @Mock
    private SaksbehandlingDokumentmottakTjeneste saksbehandlingDokumentmottakTjenesteMock;

    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;

    private Long behandlingId;

    @Before
    public void setUp() {
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        Behandling behandling = ScenarioMorSøkerEngangsstønad
            .forDefaultAktør()
            .lagre(repositoryProvider, resultatRepositoryProvider);
        behandlingId = behandling.getId();

        BehandlingskontrollTjenesteImpl behandlingskontrollTjenesteImpl = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            behandlingModellRepositoryMock, null);

        when(revurderingTjenesteProviderMock.finnRevurderingTjenesteFor(any())).thenReturn(revurderingTjenesteMock);

        behandlingsutredningApplikasjonTjeneste = new BehandlingsutredningApplikasjonTjenesteImpl(
            Period.parse("P4W"),
            repositoryProvider,
            historikkApplikasjonTjenesteMock,
            behandlingskontrollTjenesteImpl,
            revurderingTjenesteProviderMock,
            saksbehandlingDokumentmottakTjenesteMock);
    }

    @Test
    public void skal_sette_behandling_pa_vent() {
        // Act
        behandlingsutredningApplikasjonTjeneste.settBehandlingPaVent(behandlingId, LocalDate.now(), Venteårsak.AVV_DOK);

        // Assert
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getÅpneAksjonspunkter()).hasSize(1);
        assertThat(behandling.getÅpneAksjonspunkter().get(0)).isExactlyInstanceOf(Aksjonspunkt.class);
    }

    @Test
    public void skal_oppdatere_ventefrist_og_arsakskode() {
        // Arrange
        LocalDate toUkerFrem = LocalDate.now().plusWeeks(2);

        // Act
        behandlingsutredningApplikasjonTjeneste.settBehandlingPaVent(behandlingId, LocalDate.now(), Venteårsak.AVV_DOK);
        behandlingsutredningApplikasjonTjeneste.endreBehandlingPaVent(behandlingId, toUkerFrem, Venteårsak.AVV_FODSEL);

        // Assert
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.getFristDatoBehandlingPåVent()).isEqualTo(toUkerFrem);
        assertThat(behandling.getVenteårsak()).isEqualTo(Venteårsak.AVV_FODSEL);
    }

    @Test(expected = Exception.class)
    public void skal_kaste_feil_når_oppdatering_av_ventefrist_av_behandling_som_ikke_er_på_vent() {
        // Arrange
        LocalDate toUkerFrem = LocalDate.now().plusWeeks(2);

        // Act
        behandlingsutredningApplikasjonTjeneste.endreBehandlingPaVent(behandlingId, toUkerFrem, Venteårsak.AVV_FODSEL);
    }

    @Test
    public void skal_sette_behandling_med_oppgave_pa_vent_og_opprette_task_avslutt_oppgave() {
        // Arrange
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        // Act
        behandlingsutredningApplikasjonTjeneste.settBehandlingPaVent(behandlingId, LocalDate.now(), Venteårsak.AVV_DOK);

        // Assert
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getÅpneAksjonspunkter()).hasSize(1);
        assertThat(behandling.getÅpneAksjonspunkter().get(0)).isExactlyInstanceOf(Aksjonspunkt.class);
    }

    @Test
    public void skal_bytte_behandlende_enhet() {
        // Arrange
        String enhetNavn = "OSLO";
        String enhetId = "22";
        String årsak = "Test begrunnelse";

        // Act
        behandlingsutredningApplikasjonTjeneste.byttBehandlendeEnhet(behandlingId, new OrganisasjonsEnhet(enhetId, enhetNavn), årsak, HistorikkAktør.SAKSBEHANDLER);

        // Assert
        verify(historikkApplikasjonTjenesteMock).lagInnslag(any(Historikkinnslag.class));
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.getBehandlendeOrganisasjonsEnhet().getEnhetId()).isEqualTo(enhetId);
        assertThat(behandling.getBehandlendeOrganisasjonsEnhet().getEnhetNavn()).isEqualTo(enhetNavn);
        assertThat(behandling.getBehandlendeEnhetÅrsak()).isEqualTo(årsak);
    }
}
