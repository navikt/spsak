package no.nav.foreldrepenger.behandling.steg.gjenopptagelse;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.behandling.steg.gjenopptagelse.batch.AutomatiskGjenopptagelseBatchTjeneste;
import no.nav.foreldrepenger.behandling.steg.gjenopptagelse.tjeneste.AutomatiskGjenopptagelseTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

public class AutomatiskGjenopptagelseBatchTjenesteTest {

    private AutomatiskGjenopptagelseBatchTjeneste batchTjeneste; // objektet vi tester

    private AutomatiskGjenopptagelseTjeneste mockTjeneste;

    private static final String BATCHNAME = AutomatiskGjenopptagelseBatchTjeneste.BATCHNAME;
    private static final String GRUPPE = "1023";
    private static final String EXECUTION_ID = BATCHNAME + "-" + GRUPPE;

    private static final TaskStatus FERDIG_1 = new TaskStatus(ProsessTaskStatus.FERDIG, new BigDecimal(1));
    private static final TaskStatus FERDIG_2 = new TaskStatus(ProsessTaskStatus.FERDIG, new BigDecimal(1));
    private static final TaskStatus FEILET_1 = new TaskStatus(ProsessTaskStatus.FEILET, new BigDecimal(1));
    private static final TaskStatus KLAR_1 = new TaskStatus(ProsessTaskStatus.KLAR, new BigDecimal(1));

    @Before
    public void setup() {
        mockTjeneste = mock(AutomatiskGjenopptagelseTjeneste.class);
        batchTjeneste = new AutomatiskGjenopptagelseBatchTjeneste(mockTjeneste);
    }

    @Test
    public void skal_gi_status_ok_når_alle_tasks_ferdig_uten_feil() {
        // Arrange
        when(mockTjeneste.hentStatusForGjenopptaBehandlingGruppe(GRUPPE)).thenReturn(asList(FERDIG_1, FERDIG_2));

        // Act
        BatchStatus batchStatus = batchTjeneste.status(EXECUTION_ID);

        // Assert
        assertThat(batchStatus).isEqualTo(BatchStatus.OK);
    }

    @Test
    public void skal_gi_status_ok_når_ingen_tasks_funnet() {
        // Arrange
        when(mockTjeneste.hentStatusForGjenopptaBehandlingGruppe(GRUPPE)).thenReturn(Collections.emptyList());

        // Act
        BatchStatus batchStatus = batchTjeneste.status(EXECUTION_ID);

        // Assert
        assertThat(batchStatus).isEqualTo(BatchStatus.OK);
    }

    @Test
    public void skal_gi_status_warning_når_minst_en_task_feilet() {
        // Arrange
        when(mockTjeneste.hentStatusForGjenopptaBehandlingGruppe(GRUPPE)).thenReturn(asList(FERDIG_1, FEILET_1));

        // Act
        BatchStatus batchStatus = batchTjeneste.status(EXECUTION_ID);

        // Assert
        assertThat(batchStatus).isEqualTo(BatchStatus.WARNING);
    }

    @Test
    public void skal_gi_status_running_når_minst_en_task_ikke_er_startet() {
        // Arrange
        when(mockTjeneste.hentStatusForGjenopptaBehandlingGruppe(GRUPPE)).thenReturn(asList(FERDIG_1, FEILET_1, KLAR_1));

        // Act
        BatchStatus batchStatus = batchTjeneste.status(EXECUTION_ID);

        // Assert
        assertThat(batchStatus).isEqualTo(BatchStatus.RUNNING);
    }
}
