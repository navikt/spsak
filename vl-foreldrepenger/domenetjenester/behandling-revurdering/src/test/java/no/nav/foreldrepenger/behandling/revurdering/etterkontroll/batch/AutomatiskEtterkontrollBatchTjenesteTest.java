package no.nav.foreldrepenger.behandling.revurdering.etterkontroll.batch;

import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.behandling.revurdering.etterkontroll.batch.AutomatiskEtterkontrollBatchTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.etterkontroll.tjeneste.AutomatiskEtterkontrollTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutomatiskEtterkontrollBatchTjenesteTest {

    private AutomatiskEtterkontrollBatchTjeneste tjeneste;
    private AutomatiskEtterkontrollTjeneste etterkontrollTjeneste;


    @Before
    public void setUp() throws Exception {
        etterkontrollTjeneste = mock(AutomatiskEtterkontrollTjeneste.class);
        tjeneste = new AutomatiskEtterkontrollBatchTjeneste(etterkontrollTjeneste);
    }

    @Test
    public void skal_returnere_status_ok_ved_fullført() throws Exception {
        final List<TaskStatus> statuses = Collections.singletonList(new TaskStatus(ProsessTaskStatus.FERDIG, BigDecimal.ONE));
        when(etterkontrollTjeneste.hentStatusForEtterkontrollGruppe("1234")).thenReturn(statuses);

        final BatchStatus status = tjeneste.status("1234");

        assertThat(status).isEqualTo(BatchStatus.OK);
    }

    @Test
    public void skal_returnere_status_warning_ved_fullført_med_feilet() throws Exception {
        final List<TaskStatus> statuses = Arrays.asList(new TaskStatus(ProsessTaskStatus.FERDIG, BigDecimal.ONE), new TaskStatus(ProsessTaskStatus.FEILET, BigDecimal.ONE));
        when(etterkontrollTjeneste.hentStatusForEtterkontrollGruppe("1234")).thenReturn(statuses);

        final BatchStatus status = tjeneste.status("1234");

        assertThat(status).isEqualTo(BatchStatus.WARNING);
    }

    @Test
    public void skal_returnere_status_running_ved_ikke_fullført() throws Exception {
        final List<TaskStatus> statuses = Arrays.asList(new TaskStatus(ProsessTaskStatus.FERDIG, BigDecimal.ONE), new TaskStatus(ProsessTaskStatus.FEILET, BigDecimal.ONE), new TaskStatus(ProsessTaskStatus.KLAR, BigDecimal.TEN));
        when(etterkontrollTjeneste.hentStatusForEtterkontrollGruppe("1234")).thenReturn(statuses);

        final BatchStatus status = tjeneste.status("1234");

        assertThat(status).isEqualTo(BatchStatus.RUNNING);
    }
}
