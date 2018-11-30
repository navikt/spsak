package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class OpprettOppgaveForBehandlingSendtTilbakeTaskTest {
    private static final String BEHANDLENDE_ENHET_ID = "1234";

    @Mock
    private OppgaveTjeneste oppgaveTjeneste;
    private OpprettOppgaveForBehandlingSendtTilbakeTask task;
    private Behandling behandling;
    private ProsessTaskData taskData;

    @Before
    public void setup() {
        oppgaveTjeneste = mock(OppgaveTjeneste.class);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør().medBehandlendeEnhet(BEHANDLENDE_ENHET_ID);
        behandling = scenario.lagMocked();
        task = new OpprettOppgaveForBehandlingSendtTilbakeTask(scenario.mockBehandlingRepositoryProvider(), oppgaveTjeneste);

        taskData = new ProsessTaskData(OpprettOppgaveForBehandlingSendtTilbakeTask.TASKTYPE);
        taskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());

        when(oppgaveTjeneste.opprettBehandleOppgaveForBehandlingMedPrioritetOgFrist(anyLong(), anyString(), anyBoolean(), anyInt())).thenReturn("54321");
    }

    @Test
    public void shouldCallOppgaveTjeneste() {
        // Act
        task.doTask(taskData);

        // Assert
        verify(oppgaveTjeneste).opprettBehandleOppgaveForBehandlingMedPrioritetOgFrist(
            eq(behandling.getId()),
            anyString(),
            eq(true),
            eq(0));
    }
}
