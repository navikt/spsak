package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class AvsluttBehandlingImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Mock
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Mock
    private BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer;

    @Mock
    private ProsessTaskRepository prosessTaskRepository;

    private AvsluttBehandling avsluttBehandling;
    private Behandling behandling;

    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;

    private Fagsak fagsak;
    private KodeverkRepository kodeverkRepository;

    @Before
    public void setUp() {
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        behandling = lagBehandling(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        fagsak = behandling.getFagsak();

        when(repositoryProvider.getKodeverkRepository()).thenReturn(kodeverkRepository);

        avsluttBehandling = new AvsluttBehandlingImpl(repositoryProvider, behandlingskontrollTjeneste, behandlingVedtakEventPubliserer, prosessTaskRepository);

        when(behandlingskontrollTjeneste.initBehandlingskontroll(Mockito.anyLong())).thenAnswer(invocation -> {
            Long behId = invocation.getArgument(0);
            BehandlingLås lås = new BehandlingLås(behId) {
            };
            return new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);
        });
        when(behandlingskontrollTjeneste.initBehandlingskontroll(Mockito.any(Behandling.class)))
            .thenAnswer(invocation -> {
                Behandling beh = invocation.getArgument(0);
                BehandlingLås lås = new BehandlingLås(beh.getId()) {
                };
                return new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);
            });
    }

    @Test
    public void testAvsluttBehandlingUtenAndreBehandlingerISaken() {
        // Arrange
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer())).thenReturn(Collections.singletonList(behandling));

        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());

        // Assert
        verifiserIverksatt(behandling);
        verifiserKallTilProsesserBehandling(behandling);
    }

    private void verifiserIverksatt(Behandling behandling) {
        BehandlingVedtak vedtak = behandling.getBehandlingsresultat().getBehandlingVedtak();
        verify(vedtak).setIverksettingStatus(IverksettingStatus.IVERKSATT);
        verify(repositoryProvider.getBehandlingVedtakRepository()).lagre(Mockito.eq(vedtak), any(BehandlingLås.class));
    }

    @Test
    public void testAvsluttBehandlingMedAnnenBehandlingSomIkkeVenter() {
        // Arrange
        Behandling behandling2 = ScenarioMorSøkerEngangsstønad.forAdopsjon().lagMocked();
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer())).thenReturn(Arrays.asList(behandling, behandling2));
        avsluttBehandling = new AvsluttBehandlingImpl(repositoryProvider, behandlingskontrollTjeneste, behandlingVedtakEventPubliserer, null);

        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());

        verifiserIverksatt(behandling);
        verifiserKallTilProsesserBehandling(behandling);
        verify(prosessTaskRepository, never()).lagre(any(ProsessTaskData.class));
    }

    @Test
    public void testAvsluttBehandlingMedAnnenBehandlingSomVenter() {
        // Arrange
        Behandling annenBehandling = lagBehandling(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer())).thenReturn(Arrays.asList(behandling, annenBehandling));
        avsluttBehandling = new AvsluttBehandlingImpl(repositoryProvider, behandlingskontrollTjeneste, behandlingVedtakEventPubliserer, prosessTaskRepository);

        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());

        verifiserIverksatt(behandling);
        verifiserKallTilProsesserBehandling(behandling);
        verifiserKallTilFortsettBehandling(annenBehandling);
    }

    @Test
    public void testAvsluttBehandlingMedToAndreBehandlingerSomVenterEldsteFørst() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Behandling annenBehandling = lagBehandling(now.minusDays(2), now);
        Behandling tredjeBehandling = lagBehandling(now, now);
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer()))
            .thenReturn(Arrays.asList(behandling, annenBehandling, tredjeBehandling));
        avsluttBehandling = new AvsluttBehandlingImpl(repositoryProvider, behandlingskontrollTjeneste, behandlingVedtakEventPubliserer, prosessTaskRepository);

        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());

        verifiserIverksatt(behandling);
        verifiserKallTilProsesserBehandling(behandling);
        verifiserKallTilFortsettBehandling(annenBehandling);
        verifiserIkkeKallTilFortsettBehandling(tredjeBehandling);
    }

    private void verifiserKallTilProsesserBehandling(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        verify(behandlingskontrollTjeneste).prosesserBehandling(kontekst);
    }

    private void verifiserKallTilFortsettBehandling(Behandling behandling) {
        ArgumentCaptor<ProsessTaskData> prosessTaskCaptor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskRepository).lagre(prosessTaskCaptor.capture());
        List<ProsessTaskData> arguments = prosessTaskCaptor.getAllValues();

        assertThat(inneholderFortsettBehandlingTaskForBehandling(arguments, behandling)).isTrue();
    }

    private void verifiserIkkeKallTilFortsettBehandling(Behandling behandling) {
        ArgumentCaptor<ProsessTaskData> prosessTaskCaptor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskRepository).lagre(prosessTaskCaptor.capture());
        List<ProsessTaskData> arguments = prosessTaskCaptor.getAllValues();

        assertThat(inneholderFortsettBehandlingTaskForBehandling(arguments, behandling)).isFalse();
    }

    private boolean inneholderFortsettBehandlingTaskForBehandling(List<ProsessTaskData> arguments, Behandling behandling) {
        return arguments.stream()
            .anyMatch(argument -> argument.getTaskType().equals(FortsettBehandlingTaskProperties.TASKTYPE)
                && argument.getBehandlingId().equals(behandling.getId()));
    }

    @Test
    public void testAvsluttBehandlingMedToAndreBehandlingerSomVenterEldsteSist() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Behandling annenBehandling = lagBehandling(now, now);
        Behandling tredjeBehandling = lagBehandling(now.minusDays(1), now);
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer()))
            .thenReturn(Arrays.asList(behandling, annenBehandling, tredjeBehandling));

        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());

        verifiserIverksatt(behandling);
        verifiserKallTilProsesserBehandling(behandling);
        verifiserKallTilFortsettBehandling(tredjeBehandling);
        verifiserIkkeKallTilFortsettBehandling(annenBehandling);
    }

    private Behandling lagBehandling(LocalDateTime opprettet, LocalDateTime vedtaksdato) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        if (fagsak != null) {
            scenario.medFagsakId(fagsak.getId());
            scenario.medSaksnummer(fagsak.getSaksnummer());
        }
        if (repositoryProvider == null) {
            repositoryProvider = scenario.mockBehandlingRepositoryProvider();
            behandlingRepository = repositoryProvider.getBehandlingRepository();
        }
        Behandling behandling = scenario.lagMocked();
        Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .buildFor(behandling);

        if (vedtaksdato != null) {
            BehandlingVedtak vedtak = lagMockedBehandlingVedtak(opprettet, vedtaksdato, behandling);
            Whitebox.setInternalState(behandling.getBehandlingsresultat(), "behandlingVedtak", vedtak);
            when(repositoryProvider.getBehandlingVedtakRepository().hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));
            Whitebox.setInternalState(behandling, "avsluttetDato", vedtaksdato);
        }
        return behandling;
    }

    private BehandlingVedtak lagMockedBehandlingVedtak(LocalDateTime opprettet, LocalDateTime vedtaksdato, Behandling behandling) {
        BehandlingVedtak vedtak = Mockito.spy(BehandlingVedtak.builder()
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medBehandlingsresultat(behandling.getBehandlingsresultat())
            .medAnsvarligSaksbehandler("Severin Saksbehandler")
            .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
            .medVedtaksdato(vedtaksdato.toLocalDate()).build());
        Whitebox.setInternalState(vedtak, "opprettetTidspunkt", opprettet);
        return vedtak;
    }

}
