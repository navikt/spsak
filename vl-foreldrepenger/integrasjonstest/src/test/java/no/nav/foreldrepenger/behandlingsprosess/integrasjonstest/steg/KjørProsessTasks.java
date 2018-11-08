package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.behandlingskontroll.task.StartBehandlingTask;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.HåndterMottattDokumentTaskProperties;
import no.nav.foreldrepenger.domene.mottak.hendelser.KlargjørHendelseTask;
import no.nav.foreldrepenger.domene.mottak.hendelser.impl.MottaHendelseFagsakTask;
import no.nav.foreldrepenger.domene.registerinnhenting.impl.InnhentRelaterteYtelserTask;
import no.nav.foreldrepenger.domene.registerinnhenting.impl.RegisterdataOppdatererTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.BasicCdiProsessTaskDispatcher;

/** Hjelpeklasse for å utføre prosesstasks i forbindelse med tester. */
public class KjørProsessTasks {

    private ProsessTaskRepository prosessTaskRepository;

    public KjørProsessTasks(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
    }

    public void utførTasks() {
        String[] enabledTasks = { RegisterdataOppdatererTask.TASKTYPE, FortsettBehandlingTaskProperties.TASKTYPE, InnhentRelaterteYtelserTask.TASKTYPE, StartBehandlingTask.TASKTYPE};
        BasicCdiProsessTaskDispatcher dispatcher = new BasicCdiProsessTaskDispatcher();

        List<ProsessTaskData> ikkeStartet = prosessTaskRepository.finnIkkeStartet();
        while (!ikkeStartet.isEmpty()) {
            List<ProsessTaskData> tasks = ikkeStartet.stream()
                .filter(t -> Arrays.asList(enabledTasks).contains(t.getTaskType()))
                .sorted(Comparator.comparing(ProsessTaskData::getGruppe).thenComparing(Comparator.comparing(ProsessTaskData::getSekvens)))
                .collect(Collectors.toList());

            tasks.forEach(t -> {
                dispatcher.findHandler(t).doTask(t);
            });

            ikkeStartet.stream()
                .forEach(t -> {
                    t.setStatus(ProsessTaskStatus.FERDIG);
                    prosessTaskRepository.lagre(t);
                });
            ikkeStartet = prosessTaskRepository.finnIkkeStartet().stream()
                .filter(t -> ProsessTaskStatus.KLAR.equals(t.getStatus()))
                .collect(Collectors.toList());
        }
    }

    public void utførAlleTasks() {

        String[] enabledTasks = {
            HåndterMottattDokumentTaskProperties.TASKTYPE,
            StartBehandlingTask.TASKTYPE,
            RegisterdataOppdatererTask.TASKTYPE,
            FortsettBehandlingTaskProperties.TASKTYPE,
            InnhentRelaterteYtelserTask.TASKTYPE,
            KlargjørHendelseTask.TASKNAME,
            MottaHendelseFagsakTask.TASKTYPE};
        BasicCdiProsessTaskDispatcher dispatcher = new BasicCdiProsessTaskDispatcher();

        List<ProsessTaskData> ikkeStartet;
        Set<ProsessTaskData> alleredeKjørt = new HashSet<>();
        do {
            ikkeStartet = prosessTaskRepository.finnIkkeStartet();
            List<ProsessTaskData> tasks = ikkeStartet.stream()
                .filter(t -> Arrays.asList(enabledTasks).contains(t.getTaskType()))
                .sorted(Comparator.comparing(ProsessTaskData::getGruppe).thenComparing(Comparator.comparing(ProsessTaskData::getSekvens)))
                .collect(Collectors.toList());

            if (alleredeKjørt.containsAll(tasks)) {
                // Bug gjør at prosessTaskRepository ikke flusher ut ProsessTaskStatus.FERDIG nedenfor. Dirty trick
                return;
            }
            tasks.forEach(t -> {
                dispatcher.findHandler(t).doTask(t);
            });
            alleredeKjørt.addAll(tasks);

            ikkeStartet.stream()
                .forEach(t -> {
                    t.setStatus(ProsessTaskStatus.FERDIG);
                    prosessTaskRepository.lagre(t);
                });
        } while (!ikkeStartet.isEmpty());
    }

    public boolean validerTaskProperty(String tasktype, String key, String value) {
        return prosessTaskRepository.finnAlle(ProsessTaskStatus.FERDIG, ProsessTaskStatus.FEILET, ProsessTaskStatus.KLAR).stream()
            .filter(t -> tasktype.equals(t.getTaskType()))
            .map(t -> t.getPropertyValue(key))
            .filter(Objects::nonNull)
            .anyMatch(value::equals);
    }

}
