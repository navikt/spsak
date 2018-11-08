package no.nav.foreldrepenger.behandling.revurdering.etterkontroll.batch;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.etterkontroll.tjeneste.AutomatiskEtterkontrollTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

/**
 * Henter ut behandlinger som har fått innvilget engangsstønad på bakgrunn av terminbekreftelsen,
 * for å etterkontrollere om rett antall barn har blitt født.
 * 
 * Vedtak er innvilget og fattet med bakgrunn i bekreftet terminbekreftelse
 *      Det har gått minst 60 dager siden termin
 *      Det er ikke registrert fødselsdato på barnet/barna
 *      Det ikke allerede er opprettet revurderingsbehandling med en av disse årsakene:
 *      Manglende fødsel i TPS
 *      Manglende fødsel i TPS mellom uke 26 og 29
 *      Avvik i antall barn
 * 
 * Ved avvik så opprettes det, hvis det ikke allerede finnes, revurderingsbehandling på saken
 */

@ApplicationScoped
public class AutomatiskEtterkontrollBatchTjeneste implements BatchTjeneste {

    private static final String BATCHNAME = "BVL002";
    private AutomatiskEtterkontrollTjeneste tjeneste;

    @Inject
    public AutomatiskEtterkontrollBatchTjeneste(AutomatiskEtterkontrollTjeneste tjeneste) {
        this.tjeneste = tjeneste;
    }

    @Override
    public String launch(BatchArguments arguments) {
        tjeneste.etterkontrollerBehandlinger();
        return BATCHNAME;
    }

    @Override
    public BatchStatus status(String batchInstanceNumber) {
        final String gruppe = batchInstanceNumber.substring(batchInstanceNumber.indexOf('-') + 1);
        final List<TaskStatus> taskStatuses = tjeneste.hentStatusForEtterkontrollGruppe(gruppe);

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
