package no.nav.foreldrepenger.batch.task;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchSupportTjeneste;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.batch.feil.BatchFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Opp
 */
@ApplicationScoped
@ProsessTask(BatchRunnerTask.TASKTYPE)
public class BatchRunnerTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(BatchRunnerTask.class);

    public static final String TASKTYPE = "batch.runner";
    static final String BATCH_NAME = "batch.runner.name";
    static final String BATCH_PARAMS = "batch.runner.params";
    static final String BATCH_RUN_DATE = "batch.runner.onlydate";
    static final String BATCH_NAME_RETRY_TASKS = "RETRY_FAILED_TASKS";


    private BatchSupportTjeneste batchSupportTjeneste;

    BatchRunnerTask() {
        // for CDI proxy
    }

    @Inject
    public BatchRunnerTask(BatchSupportTjeneste batchSupportTjeneste) {
        this.batchSupportTjeneste = batchSupportTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String batchName = prosessTaskData.getPropertyValue(BATCH_NAME);
        String batchParams = prosessTaskData.getPropertyValue(BATCH_PARAMS);
        String batchDate = prosessTaskData.getPropertyValue(BATCH_RUN_DATE);
        if (BATCH_NAME_RETRY_TASKS.equals(batchName)) {
            batchSupportTjeneste.retryAlleProsessTasksFeilet();
            return;
        }
        if (batchDate != null && !batchDate.equals(LocalDate.now(FPDateUtil.getOffset()).toString())) {
            String logMessage = batchName + " dato passert " + batchDate;
            logger.warn("Kjører ikke batch {}", logMessage);
            return;
        }
        final BatchTjeneste batchTjeneste = batchSupportTjeneste.finnBatchTjenesteForNavn(batchName);
        if (batchTjeneste == null) {
            throw BatchFeil.FACTORY.ugyldiJobbNavnOppgitt(batchName).toException();
        }
        final BatchArguments batchArguments = batchTjeneste.createArguments(parseJobParams(batchParams));

        if (batchArguments.isValid()) {
            String logMessage = batchName + " parametere " + (batchParams != null ? batchParams : "");
            logger.info("Starter batch {}", logMessage);
            batchTjeneste.launch(batchArguments);
        } else {
            throw BatchFeil.FACTORY.ugyldigeJobParametere(batchArguments).toException();
        }
    }

    private Map<String, String> parseJobParams(String jobParameters) {
        Map<String, String> resultat = new HashMap<>();
        if (jobParameters != null && jobParameters.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(jobParameters, ",");
            while (tokenizer.hasMoreTokens()) {
                String keyValue = tokenizer.nextToken().trim();
                String[] keyValArr = keyValue.split("=");
                if (keyValArr.length == 2) {
                    resultat.put(keyValArr[0], keyValArr[1]);
                }
            }
        }
        return resultat;
    }
}
