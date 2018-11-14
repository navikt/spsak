package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.VurderOgSendØkonomiOppdrag;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.LogSniffer;

public class VurderOgSendØkonomiOppdragTaskTest {

    private static final Long BEHANDLING_ID = 139L;

    private static final Long TASK_ID = 238L;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public final LogSniffer logSniffer = new LogSniffer();


    @Mock
    private VurderOgSendØkonomiOppdrag tjeneste;

    @Mock
    private ProsessTaskRepository repo;

    @Mock
    private ProsessTaskData prosessTaskData;

    private VurderOgSendØkonomiOppdragTask task;

    @Before
    public void setUp() throws Exception {
        when(prosessTaskData.getBehandlingId()).thenReturn(BEHANDLING_ID);
        when(prosessTaskData.getId()).thenReturn(TASK_ID);
        task = new VurderOgSendØkonomiOppdragTask(tjeneste,
            repo, ScenarioMorSøkerForeldrepenger.forFødsel().mockBehandlingRepositoryProvider());
    }

    @Test
    public void testSkalSendeOppdrag() {
        // Arrange
        when(tjeneste.skalSendeOppdrag(BEHANDLING_ID)).thenReturn(true);
        when(prosessTaskData.getHendelse()).thenReturn(Optional.empty());
        when(prosessTaskData.getPropertyValue(VurderOgSendØkonomiOppdragTask.SEND_OPPDRAG)).thenReturn(null);

        // Act
        task.doTask(prosessTaskData);

        // Assert
        logSniffer.assertHasInfoMessage("Klargjør økonomioppdrag");
        logSniffer.assertHasInfoMessage("Økonomioppdrag er klargjort");
        verify(prosessTaskData).venterPåHendelse(ProsessTaskHendelse.ØKONOMI_OPPDRAG_KVITTERING);
        verify(repo).lagre(prosessTaskData);
        verify(tjeneste).sendOppdrag(BEHANDLING_ID, TASK_ID, true);
    }

    @Test
    public void skalOppdragSendesTilØkonomiFlagBørSettesFalseNårTaskenHarTilsvarendeProperty() {
        // Arrange
        when(tjeneste.skalSendeOppdrag(BEHANDLING_ID)).thenReturn(true);
        when(prosessTaskData.getHendelse()).thenReturn(Optional.empty());
        when(prosessTaskData.getPropertyValue(VurderOgSendØkonomiOppdragTask.SEND_OPPDRAG)).thenReturn("false");

        // Act
        task.doTask(prosessTaskData);

        // Assert
        logSniffer.assertHasInfoMessage("Klargjør økonomioppdrag");
        logSniffer.assertHasInfoMessage("Økonomioppdrag er klargjort");
        verify(prosessTaskData).venterPåHendelse(ProsessTaskHendelse.ØKONOMI_OPPDRAG_KVITTERING);
        verify(repo).lagre(prosessTaskData);
        verify(tjeneste).sendOppdrag(BEHANDLING_ID, TASK_ID,false);
    }

    @Test
    public void testSkalIkkeSendeOppdrag() {
        // Arrange
        when(tjeneste.skalSendeOppdrag(BEHANDLING_ID)).thenReturn(false);
        when(prosessTaskData.getHendelse()).thenReturn(Optional.empty());
        when(prosessTaskData.getPropertyValue(VurderOgSendØkonomiOppdragTask.SEND_OPPDRAG)).thenReturn(null);

        // Act
        task.doTask(prosessTaskData);

        // Assert
        logSniffer.assertHasInfoMessage("Ikke aktuelt");
        verify(prosessTaskData, never()).venterPåHendelse(any());
        verify(repo, never()).lagre((ProsessTaskData) any());
        verify(tjeneste, never()).sendOppdrag(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    public void testSkalBehandleKvittering() {
        // Arrange
        when(prosessTaskData.getHendelse()).thenReturn(Optional.of(ProsessTaskHendelse.ØKONOMI_OPPDRAG_KVITTERING));

        // Act
        task.doTask(prosessTaskData);

        // Assert
        logSniffer.assertHasInfoMessage("Økonomioppdrag-kvittering mottatt");
        verify(tjeneste, never()).skalSendeOppdrag(anyLong());
        verify(prosessTaskData, never()).venterPåHendelse(any());
        verify(repo, never()).lagre(any(ProsessTaskData.class));
        verify(tjeneste, never()).sendOppdrag(anyLong(), anyLong(), anyBoolean());
    }
}
