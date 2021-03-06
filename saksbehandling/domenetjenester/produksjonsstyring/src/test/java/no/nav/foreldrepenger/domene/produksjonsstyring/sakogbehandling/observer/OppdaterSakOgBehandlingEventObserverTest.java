package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent.BehandlingAvsluttetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent.BehandlingOpprettetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.task.SakOgBehandlingTask;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class OppdaterSakOgBehandlingEventObserverTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private OppdaterSakOgBehandlingEventObserver observer;
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private ProsessTaskRepository prosessTaskRepositoryMock;
    private KodeverkRepository kodeverkRepository;

    @Before
    public void setup() {

        prosessTaskRepositoryMock = mock(ProsessTaskRepository.class);

        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();

        observer = new OppdaterSakOgBehandlingEventObserver(repositoryProvider, prosessTaskRepositoryMock);
    }

    @Test
    public void skalOppretteOppdaterSakOgBehandlingTaskMedAlleParametereNårBehandlingErOpprettet() {
        
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        final Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        refreshBehandlingType(scenario);

        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), scenario.taSkriveLåsForBehandling());
        BehandlingOpprettetEvent event = BehandlingStatusEvent.nyEvent(kontekst, BehandlingStatus.OPPRETTET);

        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        observer.observerBehandlingStatus(event);

        verify(prosessTaskRepositoryMock).lagre(captor.capture());
        ProsessTaskData prosessTaskData = captor.getValue();
        verifiserProsessTaskData(scenario, prosessTaskData, BehandlingStatus.OPPRETTET.getKode());

    }

    @Test
    public void skalOppretteOppdaterSakOgBehandlingTaskMedAlleParametereNårBehandlingErAvsluttet() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        refreshBehandlingType(scenario);
        Fagsak fagsak =behandling.getFagsak(); 
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), scenario.taSkriveLåsForBehandling());
        BehandlingAvsluttetEvent event = BehandlingStatusEvent.nyEvent(kontekst, BehandlingStatus.AVSLUTTET);

        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        observer.observerBehandlingStatus(event);

        verify(prosessTaskRepositoryMock).lagre(captor.capture());
        ProsessTaskData prosessTaskData = captor.getValue();
        verifiserProsessTaskData(scenario, prosessTaskData, BehandlingStatus.AVSLUTTET.getKode());
    }

    private void refreshBehandlingType(ScenarioMorSøkerEngangsstønad scenario) {
        BehandlingType behandlingType = kodeverkRepository.finn(BehandlingType.class, scenario.getBehandling().getType());
        Whitebox.setInternalState(scenario.getBehandling(), "behandlingType", behandlingType);
    }

    private void verifiserProsessTaskData(ScenarioMorSøkerEngangsstønad scenario, ProsessTaskData prosessTaskData,
                                          String behandlingStatusKode) {
        assertThat(prosessTaskData.getTaskType()).isEqualTo(SakOgBehandlingTask.TASKNAME);
        assertThat(new AktørId(prosessTaskData.getAktørId()))
            .isEqualTo(scenario.getFagsak().getNavBruker().getAktørId());
        assertThat(prosessTaskData.getBehandlingId())
            .isEqualTo(scenario.getBehandling().getId());
        assertThat(prosessTaskData.getPropertyValue(SakOgBehandlingTask.ANSVARLIG_ENHET_KEY))
            .isEqualTo(scenario.getBehandling().getBehandlendeEnhet());
        assertThat(prosessTaskData.getPropertyValue(SakOgBehandlingTask.SAKSTEMA_KEY))
            .isEqualTo(OppdaterSakOgBehandlingEventObserver.FORELDREPENGER_SAKSTEMA);
        assertThat(prosessTaskData.getPropertyValue(SakOgBehandlingTask.BEHANDLINGS_TYPE_KODE_KEY))
            .isEqualTo(scenario.getBehandling().getType().getOffisiellKode());
        assertThat(prosessTaskData.getPropertyValue(SakOgBehandlingTask.BEHANDLINGSTEMAKODE))
            .isEqualTo(kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.SYKEPENGER.getKode()).getOffisiellKode());
        assertThat(prosessTaskData.getPropertyValue(SakOgBehandlingTask.BEHANDLING_STATUS_KEY)).isEqualTo(behandlingStatusKode);
    }

}
