package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class OpprettOppgaveRegistrerSøknadTaskTest {

    private static final long BEHANDLING_ID = 1L;

    private OppgaveTjeneste oppgaveTjeneste;
    private OpprettOppgaveRegistrerSøknadTask opprettOppgaveRegistrerSøknadTask;

    @Before
    public void before() {
        oppgaveTjeneste = mock(OppgaveTjeneste.class);
        opprettOppgaveRegistrerSøknadTask = new OpprettOppgaveRegistrerSøknadTask(oppgaveTjeneste, ScenarioMorSøkerForeldrepenger.forFødsel().mockBehandlingRepositoryProvider());
    }

    @Test
    public void skal_opprette_oppgave_for_å_registere_søknad() {
        // Arrange
        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettOppgaveRegistrerSøknadTask.TASKTYPE);
        prosessTaskData.setBehandling(1L, BEHANDLING_ID, "99");
        ArgumentCaptor<Long> behandlingIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<OppgaveÅrsak> årsakCaptor = ArgumentCaptor.forClass(OppgaveÅrsak.class);

        // Act
        opprettOppgaveRegistrerSøknadTask.doTask(prosessTaskData);

        // Assert
        verify(oppgaveTjeneste).opprettBasertPåBehandlingId(behandlingIdCaptor.capture(), årsakCaptor.capture());
        assertThat(behandlingIdCaptor.getValue()).isEqualTo(BEHANDLING_ID);
        assertThat(årsakCaptor.getValue()).isEqualTo(OppgaveÅrsak.REGISTRER_SØKNAD);
    }
}
