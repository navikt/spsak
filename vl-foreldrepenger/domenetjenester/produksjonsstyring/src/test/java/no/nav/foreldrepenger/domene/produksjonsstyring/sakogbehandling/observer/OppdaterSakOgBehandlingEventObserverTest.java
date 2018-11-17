package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent.BehandlingAvsluttetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent.BehandlingOpprettetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.task.SakOgBehandlingTask;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class OppdaterSakOgBehandlingEventObserverTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private OppdaterSakOgBehandlingEventObserver observer;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private ProsessTaskRepository prosessTaskRepositoryMock;
    private MetricRegistry metricRegistryMock;
    private KodeverkRepository kodeverkRepository;
    private Meter meterMock;

    @Before
    public void setup() {

        prosessTaskRepositoryMock = mock(ProsessTaskRepository.class);

        metricRegistryMock = mock(MetricRegistry.class);
        meterMock = mock(Meter.class);
        when(metricRegistryMock.meter(any(String.class))).thenReturn(meterMock);

        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();

        observer = new OppdaterSakOgBehandlingEventObserver(repositoryProvider, prosessTaskRepositoryMock, metricRegistryMock);
    }

    @Test
    public void skalOppretteOppdaterSakOgBehandlingTaskMedAlleParametereNårBehandlingErOpprettet() {
        
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();

        final Behandling behandling = scenario.lagre(repositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        refreshBehandlingType(scenario);

        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), scenario.taSkriveLåsForBehandling());
        BehandlingOpprettetEvent event = BehandlingStatusEvent.nyEvent(kontekst, BehandlingStatus.OPPRETTET);

        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        observer.observerBehandlingStatus(event);

        verify(prosessTaskRepositoryMock).lagre(captor.capture());
        ProsessTaskData prosessTaskData = captor.getValue();
        verifiserProsessTaskData(scenario, prosessTaskData, BehandlingStatus.OPPRETTET.getKode());

        verify(meterMock).mark();
    }

    @Test
    public void skalOppretteOppdaterSakOgBehandlingTaskMedAlleParametereNårBehandlingErAvsluttet() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();

        Behandling behandling = scenario.lagre(repositoryProvider);
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
            .isEqualTo(kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.FORELDREPENGER.getKode()).getOffisiellKode());
        assertThat(prosessTaskData.getPropertyValue(SakOgBehandlingTask.BEHANDLING_STATUS_KEY)).isEqualTo(behandlingStatusKode);
    }

}
