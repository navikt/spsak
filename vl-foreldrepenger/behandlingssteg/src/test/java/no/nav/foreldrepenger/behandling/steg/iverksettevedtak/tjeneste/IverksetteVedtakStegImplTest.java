package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.IverksetteVedtakHistorikkTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task.AvsluttBehandlingTask;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task.SendVedtaksbrevTask;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioInnsynEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioKlageEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.AvsluttOppgaveTaskProperties;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveVurderKonsekvensTask;
import no.nav.foreldrepenger.domene.vedtak.KanVedtaketIverksettesTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class IverksetteVedtakStegImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;

    private Behandling behandling;

    @Mock
    private OppgaveTjeneste oppgaveTjeneste;

    @Mock
    private IverksetteVedtakHistorikkTjeneste iverksetteVedtakHistorikkTjeneste;

    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null);

    private IverksetteVedtakStegImpl iverksetteVedtakSteg;

    private BehandlingVedtak vedtak;

    @Mock
    private KanVedtaketIverksettesTjeneste kanVedtaketIverksettesTjeneste;

    @Mock
    private BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer;

    private BehandlingVedtakRepository behandlingVedtakRepository;


    private void opprettSteg(ScenarioKlageEngangsstønad scenario) {
        behandling = scenario.lagMocked();
        vedtak = scenario.mockBehandlingVedtak();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        mockOpprettTaskAvsluttOppgave();
        iverksetteVedtakSteg = new IverksetteVedtakStegImpl(repositoryProvider, prosessTaskRepository,
            behandlingVedtakEventPubliserer, oppgaveTjeneste, iverksetteVedtakHistorikkTjeneste, kanVedtaketIverksettesTjeneste);

    }

    private void mockOpprettTaskAvsluttOppgave() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(AvsluttOppgaveTaskProperties.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setOppgaveId("1001");
        when(oppgaveTjeneste.opprettTaskAvsluttOppgave(any(Behandling.class), any(OppgaveÅrsak.class), anyBoolean())).thenReturn(Optional.of(prosessTaskData));
    }

    @Test
    public void testOpprettIverksettingstaskerForKlagebehandling() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forStadfestetNK(abstractScenario);
        opprettSteg(scenario);
        List<ProsessTaskData> resultat = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(resultat).isEmpty();

        // Act
        iverksetteVedtakSteg.opprettIverksettingstasker(behandling);

        // Assert
        resultat = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(resultat).hasSize(3);
        List<String> tasktyper = resultat.stream().map(ProsessTaskData::getTaskType).collect(Collectors.toList());
        assertThat(tasktyper).contains(AvsluttBehandlingTask.TASKTYPE, SendVedtaksbrevTask.TASKTYPE, AvsluttOppgaveTaskProperties.TASKTYPE);
    }

    @Test
    public void testOpprettIverksettingstaskerForKlagebehandlingMedMedhold() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(abstractScenario);
        opprettSteg(scenario);

        List<ProsessTaskData> resultat = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(resultat).isEmpty();

        // Act
        iverksetteVedtakSteg.opprettIverksettingstasker(behandling);

        // Assert
        resultat = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(resultat).hasSize(4);
        List<String> tasktyper = resultat.stream().map(ProsessTaskData::getTaskType).collect(Collectors.toList());
        assertThat(tasktyper).contains(AvsluttBehandlingTask.TASKTYPE, SendVedtaksbrevTask.TASKTYPE, AvsluttOppgaveTaskProperties.TASKTYPE, OpprettOppgaveVurderKonsekvensTask.TASKTYPE);
    }

    @Test
    public void testUtførStegForIverksetteVedtakUtenResultat() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forUtenVurderingResultat(abstractScenario);
        opprettSteg(scenario);

        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));

        List<ProsessTaskData> resultat = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(resultat).isEmpty();

        // Act
        Fagsak fagsak = behandling.getFagsak();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat behStegRestulat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås));

        // Assert
        assertThat(behStegRestulat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        verify(vedtak, never()).setIverksettingStatus(any());
    }

    @Test
    public void testUtførStegForIverksetteVedtakMedResultat() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(abstractScenario);
        opprettSteg(scenario);

        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.IKKE_IVERKSATT);
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));

        List<ProsessTaskData> resultat = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(resultat).isEmpty();
        Fagsak fagsak = behandling.getFagsak();
        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat behStegRestulat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås));

        // Assert
        assertThat(behStegRestulat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        verify(vedtak).setIverksettingStatus(IverksettingStatus.UNDER_IVERKSETTING);
    }

    @Test
    public void testUtførStegForIverksetteVedtakNårIverksettelsePågår() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(abstractScenario);
        opprettSteg(scenario);

        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.UNDER_IVERKSETTING);
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));
        Fagsak fagsak = behandling.getFagsak();
        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat behStegRestulat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås));

        // Assert
        assertThat(behStegRestulat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        verify(vedtak, never()).setIverksettingStatus(any());
    }

    @Test
    public void testUtførStegForIverksetteVedtakNårIverksettelseFerdig() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(abstractScenario);
        opprettSteg(scenario);

        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));
        Fagsak fagsak = behandling.getFagsak();
        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat behStegRestulat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås));

        // Assert
        assertThat(behStegRestulat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        verify(vedtak, never()).setIverksettingStatus(any());
    }

    @Test
    public void testIverksettingHindresIkkeNårDetBareErEnBehandling() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(abstractScenario);
        opprettSteg(scenario);

        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer())).thenReturn(Collections.singletonList(behandling));
        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.IKKE_IVERKSATT);

        // Act
        boolean hindres = iverksetteVedtakSteg.iverksettingHindresAvAnnenBehandling(behandling);

        // Assert
        assertThat(hindres).isEqualTo(false);
        verify(behandlingRepository).hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer());
    }

    @Test
    public void testIverksettingHindresIkkeNårErToBehandlingerMenBareEttVedtak() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(abstractScenario);
        opprettSteg(scenario);

        Behandling annenBehandling = ScenarioKlageEngangsstønad.forMedholdNK(ScenarioMorSøkerEngangsstønad.forFødsel()).lagMocked();

        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.IKKE_IVERKSATT);
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer())).thenReturn(Arrays.asList(behandling, annenBehandling));
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(annenBehandling.getId())).thenReturn(Optional.empty());

        // Act
        boolean hindres = iverksetteVedtakSteg.iverksettingHindresAvAnnenBehandling(behandling);

        // Assert
        assertThat(hindres).isEqualTo(false);
        verify(behandlingRepository).hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer());
    }

    @Test
    public void testIverksettingHindresIkkeNårErToBehandlingerOgDenAndreErFullført() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(abstractScenario);
        opprettSteg(scenario);

        Behandling annenBehandling = ScenarioKlageEngangsstønad.forMedholdNK(ScenarioMorSøkerEngangsstønad.forFødsel()).lagMocked();
        BehandlingVedtak annetVedtak = mock(BehandlingVedtak.class);

        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.IKKE_IVERKSATT);
        when(annetVedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.IVERKSATT);
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(annenBehandling.getId())).thenReturn(Optional.of(annetVedtak));
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer())).thenReturn(Arrays.asList(behandling, annenBehandling));

        // Act
        boolean hindres = iverksetteVedtakSteg.iverksettingHindresAvAnnenBehandling(behandling);

        // Assert
        assertThat(hindres).isEqualTo(false);
        verify(behandlingRepository).hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer());
    }

    @Test
    public void testIverksettingHindresNårErToBehandlingerOgDenAndreErUnderIverksetting() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(abstractScenario);
        opprettSteg(scenario);

        Behandling annenBehandling = ScenarioKlageEngangsstønad.forMedholdNK(ScenarioMorSøkerEngangsstønad.forFødsel()).lagMocked();
        BehandlingVedtak annetVedtak = mock(BehandlingVedtak.class);

        when(annetVedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.UNDER_IVERKSETTING);
        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.IKKE_IVERKSATT);
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(annenBehandling.getId())).thenReturn(Optional.of(annetVedtak));
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer())).thenReturn(Arrays.asList(behandling, annenBehandling));

        // Act
        boolean hindres = iverksetteVedtakSteg.iverksettingHindresAvAnnenBehandling(behandling);

        // Assert
        assertThat(hindres).isEqualTo(true);
        verify(behandlingRepository).hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer());
    }

    @Test
    public void testIverksettInnsyn() {
        // Arrange
        ScenarioMorSøkerEngangsstønad abstractScenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        ScenarioInnsynEngangsstønad scenario = ScenarioInnsynEngangsstønad.innsyn(abstractScenario);
        opprettInnsynSteg(scenario);

        Behandling annenBehandling = ScenarioInnsynEngangsstønad.innsyn(ScenarioMorSøkerEngangsstønad.forFødsel()).lagMocked();
        BehandlingVedtak annetVedtak = mock(BehandlingVedtak.class);

        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.IKKE_IVERKSATT);
        when(annetVedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.UNDER_IVERKSETTING);
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())).thenReturn(Optional.of(vedtak));
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(annenBehandling.getId())).thenReturn(Optional.of(annetVedtak));

        // Act
        boolean hindres = iverksetteVedtakSteg.iverksettingHindresAvAnnenBehandling(behandling);

        // Assert
        assertThat(hindres).isEqualTo(false);
        verify(behandlingRepository, never()).hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer());
    }

    private void opprettInnsynSteg(ScenarioInnsynEngangsstønad scenario) {
        behandling = scenario.lagMocked();
        vedtak = scenario.mockBehandlingVedtak();
        behandlingRepository = scenario.mockBehandlingRepository();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        mockOpprettTaskAvsluttOppgave();
        iverksetteVedtakSteg = new IverksetteVedtakStegImpl(repositoryProvider, prosessTaskRepository, behandlingVedtakEventPubliserer,
            oppgaveTjeneste, iverksetteVedtakHistorikkTjeneste, kanVedtaketIverksettesTjeneste);
    }

}
