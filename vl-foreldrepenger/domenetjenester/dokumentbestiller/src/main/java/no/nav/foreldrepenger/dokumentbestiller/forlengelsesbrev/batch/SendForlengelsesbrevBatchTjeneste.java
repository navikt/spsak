package no.nav.foreldrepenger.dokumentbestiller.forlengelsesbrev.batch;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.forlengelsesbrev.tjeneste.SendForlengelsesbrevTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * Henter ut åpne behandlinger der behandlingsfrist er utløpt,
 * sender informasjonsbrev om forlenget behandlingstid og oppdaterer behandlingsfristen.
 */

@ApplicationScoped
public class SendForlengelsesbrevBatchTjeneste implements BatchTjeneste {

    private static final String BATCHNAME = "BVL003";
    private SendForlengelsesbrevTjeneste tjeneste;

    @Inject
    public SendForlengelsesbrevBatchTjeneste(SendForlengelsesbrevTjeneste tjeneste) {
        this.tjeneste = tjeneste;
    }

    @Override
    public String launch(BatchArguments arguments) {
        final String gruppe = tjeneste.sendForlengelsesbrev();
        return BATCHNAME + "-" + gruppe;
    }

    @Override
    public BatchStatus status(String batchInstanceNumber) {
        final String gruppe = batchInstanceNumber.substring(batchInstanceNumber.indexOf('-') + 1);
        final List<TaskStatus> taskStatuses = tjeneste.hentStatusForForlengelsesbrevBatchGruppe(gruppe);

        if (isCompleted(taskStatuses)) {
            if (isContainingFailures(taskStatuses)) {
                return BatchStatus.WARNING;
            }
            return BatchStatus.OK;
        }
        // Is still running
        return BatchStatus.RUNNING;
    }

    private boolean isContainingFailures(List<TaskStatus> taskStatuses) {
        return taskStatuses.stream().anyMatch(it -> it.getStatus() == ProsessTaskStatus.FEILET);
    }

    private boolean isCompleted(List<TaskStatus> taskStatuses) {
        return taskStatuses.isEmpty() || taskStatuses.stream().noneMatch(it -> it.getStatus() == ProsessTaskStatus.KLAR);
    }

    @Override
    public String getBatchName() {
        return BATCHNAME;
    }
}
