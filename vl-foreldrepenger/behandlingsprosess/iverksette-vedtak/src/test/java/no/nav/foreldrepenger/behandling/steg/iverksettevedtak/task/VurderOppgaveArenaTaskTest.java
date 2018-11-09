package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.vedtak.VurderOmArenaYtelseSkalOpphøre;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class VurderOppgaveArenaTaskTest {

    private static final Long TASK_ID = 2L;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Mock
    private BehandlingRepositoryProvider behandlingRepository;
    @Mock
    private VurderOmArenaYtelseSkalOpphøre vurdereOmArenaYtelseSkalOpphøre;
    @Mock
    private ProsessTaskData prosessTaskData;

    private VurderOppgaveArenaTask oppgaveArenaTask;
    private Behandling behandling;


    @Before
    public void setUp(){
        when(prosessTaskData.getId()).thenReturn(TASK_ID);
    }

    @Test
    public void skal_opprett_oppgave(){
        behandling = ScenarioMorSøkerForeldrepenger.forFødsel().lagMocked();
        when(prosessTaskData.getBehandlingId()).thenReturn(behandling.getId());
        behandlingRepository = ScenarioMorSøkerEngangsstønad.forFødsel().mockBehandlingRepositoryProvider();
        when(behandlingRepository.getBehandlingRepository().hentBehandling(behandling.getId())).thenReturn(behandling);
        oppgaveArenaTask = new VurderOppgaveArenaTask(behandlingRepository, vurdereOmArenaYtelseSkalOpphøre);

        oppgaveArenaTask.doTask(prosessTaskData);

        verify(vurdereOmArenaYtelseSkalOpphøre).opprettOppgaveHvisArenaytelseSkalOpphøre(any(), any());
    }
}
