package no.nav.foreldrepenger.batch.task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.batch.BatchSupportTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Enkel scheduler for dagens situasjon der man kjører batcher mandag-fredag og det er noe variasjon i parametere.
 *
 * Kan evt endres slik at BatchSchedulerTask kjører tidlig på døgnet og oppretter dagens batches (hvis ikke tidspunkt passert)
 *
 * Skal man utvide med ukentlige, måndedlige batcher etc bør man se på cron-aktige uttrykk for spesifikasjon av kjøring.
 * FC har implementert et rammeverk på github
 */
@ApplicationScoped
@ProsessTask(BatchSchedulerTask.TASKTYPE)
public class BatchSchedulerTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "batch.scheduler";

    private static final String AVSTEMMING = "BVL001";

    private BatchSupportTjeneste batchSupportTjeneste;

    private final List<BatchConfig> batchOppsettAvstemmingMandag = Arrays.asList(
        new BatchConfig(6, 55, AVSTEMMING, "antallDager=3, fagomrade=REFUTG"),
        new BatchConfig(6, 56, AVSTEMMING, "antallDager=3, fagomrade=FP"),
        new BatchConfig(6, 57, AVSTEMMING, "antallDager=3, fagomrade=FPREF")
    );

    private final List<BatchConfig> batchOppsettAvstemmingUkedag = Arrays.asList(
        new BatchConfig(6, 55, AVSTEMMING, "antallDager=1, fagomrade=REFUTG"),
        new BatchConfig(6, 56, AVSTEMMING, "antallDager=1, fagomrade=FP"),
        new BatchConfig(6, 57, AVSTEMMING, "antallDager=1, fagomrade=FPREF")
    );

    // TODO(diamant): Når stabilt i produksjon - legg til en BRT.B_N_RETRY_TASKS fx kl 06:59. Dekker nedetid i andre system
    private final List<BatchConfig> batchOppsettFelles = Arrays.asList(
        new BatchConfig(7, 0, "BVL005", null), // Kodeverk
        new BatchConfig(7, 1, "BVL004", null) // Gjenoppta
    );

    private LocalDate dagensDato;

    BatchSchedulerTask() {
        // for CDI proxy
    }

    @Inject
    public BatchSchedulerTask(BatchSupportTjeneste batchSupportTjeneste) {
        this.batchSupportTjeneste = batchSupportTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        dagensDato = LocalDate.now(FPDateUtil.getOffset());
        DayOfWeek dagensUkedag = DayOfWeek.from(dagensDato);

        // Lagre neste instans av daglig scheduler straks over midnatt
        ProsessTaskData batchScheduler = new ProsessTaskData(BatchSchedulerTask.TASKTYPE);
        LocalDateTime nesteScheduler = dagensDato.plusDays(1).atStartOfDay().plusHours(1).plusMinutes(1);
        batchScheduler.setNesteKjøringEtter(nesteScheduler);
        ProsessTaskGruppe gruppeScheduler = new ProsessTaskGruppe(batchScheduler);
        batchSupportTjeneste.opprettScheduledTasks(gruppeScheduler);

        // Ingenting å kjøre i helgene enn så lenge
        if (DayOfWeek.FRIDAY.getValue() < dagensUkedag.getValue()) {
            return;
        }

        List<BatchConfig> batchOppsett = new ArrayList<>();
        if (DayOfWeek.MONDAY.equals(dagensUkedag)) {
            batchOppsett.addAll(batchOppsettAvstemmingMandag);
        } else {
            batchOppsett.addAll(batchOppsettAvstemmingUkedag);
        }
        batchOppsett.addAll(batchOppsettFelles);

        if (!batchOppsett.isEmpty()) {
            List<ProsessTaskData> batchtasks = batchOppsett.stream()
                .map(this::mapBatchConfigTilBatchRunnerTask)
                .collect(Collectors.toList());
            ProsessTaskGruppe gruppeRunner = new ProsessTaskGruppe();
            gruppeRunner.addNesteParallell(batchtasks);

            batchSupportTjeneste.opprettScheduledTasks(gruppeRunner);
        }
    }

    private ProsessTaskData mapBatchConfigTilBatchRunnerTask(BatchConfig config) {
        ProsessTaskData batchRunnerTask = new ProsessTaskData(BatchRunnerTask.TASKTYPE);
        batchRunnerTask.setProperty(BatchRunnerTask.BATCH_NAME, config.getName());
        if (config.getParams() != null) {
            batchRunnerTask.setProperty(BatchRunnerTask.BATCH_PARAMS, config.getParams());
        }
        batchRunnerTask.setProperty(BatchRunnerTask.BATCH_RUN_DATE, dagensDato.toString());
        batchRunnerTask.setNesteKjøringEtter(LocalDateTime.of(dagensDato, config.getKjøreTidspunkt()));
        return batchRunnerTask;
    }
}
