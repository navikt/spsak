package no.nav.foreldrepenger.mottak.felles;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.task.OpprettSakTask;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public abstract class WrappedProsessTaskHandler implements ProsessTaskHandler {

    protected ProsessTaskRepository prosessTaskRepository;
    protected KodeverkRepository kodeverkRepository;
    private MetricRegistry metricRegistry;

    public WrappedProsessTaskHandler(ProsessTaskRepository prosessTaskRepository, KodeverkRepository kodeverkRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.kodeverkRepository = kodeverkRepository;
    }

    @Inject
    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Timed
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);
        precondition(dataWrapper);

        MottakMeldingDataWrapper prosessTaskDataNesteMedDataFraInput = doTask(dataWrapper);

        if (prosessTaskDataNesteMedDataFraInput != null) {
            if (skalMåleForFag(prosessTaskDataNesteMedDataFraInput.getProsessTaskData().getTaskType())) {
                metricRegistry.meter(generateUniqueJointName(prosessTaskDataNesteMedDataFraInput)).mark();
            }
            metricRegistry.meter(generateUniqueToFromKey(dataWrapper, prosessTaskDataNesteMedDataFraInput)).mark();
            postcondition(prosessTaskDataNesteMedDataFraInput);
            prosessTaskRepository.lagre(prosessTaskDataNesteMedDataFraInput.getProsessTaskData());
        }
    }

    public abstract void precondition(MottakMeldingDataWrapper dataWrapper);

    public void postcondition(@SuppressWarnings("unused") MottakMeldingDataWrapper dataWrapper) {
        //Override i subtasks hvor det er krav til precondition. Det er typisk i tasker hvor tasken henter data og det er behov for å sjekke at alt er OK etter at task er kjørt.
    }

    public abstract MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper);

    private String generateUniqueJointName(MottakMeldingDataWrapper dataWrapper) {
        String taskMeterName;
        switch (dataWrapper.getProsessTaskData().getTaskType()) {
            case OpprettSakTask.TASKNAME:
                taskMeterName = "ny.fpsak";
                break;
            case TilJournalføringTask.TASKNAME:
                taskMeterName = "journalforing.fpsak";
                break;
            case OpprettGSakOppgaveTask.TASKNAME:
                taskMeterName = "journalforing.manuell";
                break;
            default:
                throw new IllegalArgumentException();
        }
        return "mottak.soknader." + dataWrapper.getBehandlingTema().getKode() + "." + taskMeterName;
    }

    private boolean skalMåleForFag(String taskType) {
        final List<String> meterableTasks = Arrays.asList(OpprettSakTask.TASKNAME,
                TilJournalføringTask.TASKNAME,
                OpprettGSakOppgaveTask.TASKNAME);
        return meterableTasks.contains(taskType);
    }

    String generateUniqueToFromKey(MottakMeldingDataWrapper orginalDataWrapper, MottakMeldingDataWrapper nesteStegDataWrapper) {
        String fraTaskType = orginalDataWrapper.getProsessTaskData().getTaskType();
        String tilTaskType = nesteStegDataWrapper.getProsessTaskData().getTaskType();
        return metricMeterNameForProsessTasksFraTil(fraTaskType, tilTaskType);
    }

    public static String metricMeterNameForProsessTasksFraTil(String fraTaskType, String tilTaskType) {
        return "mottak.tasks.fra." + fraTaskType + ".til." + tilTaskType;
    }
}
