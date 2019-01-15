package no.nav.foreldrepenger.web.app.tjenester;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.util.FPDateUtil;

public class VurderProsessTaskStatusForPollingApi {
    private static final Logger log = LoggerFactory.getLogger(VurderProsessTaskStatusForPollingApi.class);

    private ProsessTaskFeilmelder feilmelder;
    private Long entityId;

    public interface ProsessTaskFeilmelder {
        Feil feilIProsessTaskGruppe(String callId, Long entityId, String gruppe, Long taskId, ProsessTaskStatus taskStatus);

        Feil utsattKjøringAvProsessTask(String callId, Long entityId, String gruppe, Long taskId, ProsessTaskStatus taskStatus, LocalDateTime nesteKjøringEtter);

        Feil venterPåSvar(String callId, Long entityId, String gruppe, Long id, ProsessTaskStatus status);
    }

    public VurderProsessTaskStatusForPollingApi(ProsessTaskFeilmelder feilmelder, Long entityId) {
        this.feilmelder = feilmelder;
        this.entityId = entityId;
    }

    public Optional<AsyncPollingStatus> sjekkStatusNesteProsessTask(String gruppe, Map<String, ProsessTaskData> nesteTask) {
        LocalDateTime maksTidFørNesteKjøring = LocalDateTime.now(FPDateUtil.getOffset()).plusMinutes(2);
        nesteTask = nesteTask.entrySet().stream()
            .filter(e -> !ProsessTaskStatus.FERDIG.equals(e.getValue().getStatus())) // trenger ikke FERDIG
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!nesteTask.isEmpty()) {
            Optional<ProsessTaskData> optTask = Optional.ofNullable(nesteTask.get(gruppe));
            if (!optTask.isPresent()) {
                // plukker neste til å polle på
                optTask = nesteTask.entrySet().stream()
                    .map(e -> e.getValue())
                    .findFirst();
            }

            if (optTask.isPresent()) {
                return sjekkStatus(maksTidFørNesteKjøring, optTask);
            }
        }
        return Optional.empty();
    }

    private Optional<AsyncPollingStatus> sjekkStatus(LocalDateTime maksTidFørNesteKjøring, Optional<ProsessTaskData> optTask) {
        ProsessTaskData task = optTask.get();
        String gruppe = task.getGruppe();
        String callId = task.getPropertyValue("callId");
        ProsessTaskStatus taskStatus = task.getStatus();
        if (ProsessTaskStatus.KLAR.equals(taskStatus)) {
            return ventPåKlar(gruppe, maksTidFørNesteKjøring, task, callId);
        } else if (ProsessTaskStatus.VENTER_SVAR.equals(taskStatus)) {
            return ventPåSvar(gruppe, task, callId);
        } else {
            // dekker SUSPENDERT, FEILET, VETO
            return håndterFeil(gruppe, task, callId);
        }
    }

    private Optional<AsyncPollingStatus> håndterFeil(String gruppe, ProsessTaskData task, String callId) {
        Feil feil = feilmelder.feilIProsessTaskGruppe(callId, entityId, gruppe, task.getId(), task.getStatus());
        feil.log(log);

        AsyncPollingStatus status = new AsyncPollingStatus(AsyncPollingStatus.Status.HALTED,
            null, task.getSisteFeil());
        return Optional.of(status);// fortsett å polle på gruppe, er ikke ferdig.
    }

    private Optional<AsyncPollingStatus> ventPåSvar(String gruppe, ProsessTaskData task, String callId) {
        Feil feil = feilmelder.venterPåSvar(callId, entityId, gruppe, task.getId(), task.getStatus());
        feil.log(log);

        AsyncPollingStatus status = new AsyncPollingStatus(
            AsyncPollingStatus.Status.DELAYED,
            task.getNesteKjøringEtter(),
            feil.getFeilmelding());

        return Optional.of(status);// er ikke ferdig, men ok å videresende til visning av behandling med feilmelding der.
    }

    private Optional<AsyncPollingStatus> ventPåKlar(String gruppe, LocalDateTime maksTidFørNesteKjøring, ProsessTaskData task, String callId) {
        if (task.getNesteKjøringEtter().isBefore(maksTidFørNesteKjøring)) {

            AsyncPollingStatus status = new AsyncPollingStatus(
                AsyncPollingStatus.Status.PENDING,
                task.getNesteKjøringEtter(),
                "Venter på prosesstask [" + task.getTaskType() + "][id: " + task.getId() + "]",
                null, 500L);

            return Optional.of(status);// fortsett å polle på gruppe, er ikke ferdig.
        } else {
            Feil feil = feilmelder.utsattKjøringAvProsessTask(
               callId, entityId, gruppe, task.getId(), task.getStatus(), task.getNesteKjøringEtter());
            feil.log(log);

            AsyncPollingStatus status = new AsyncPollingStatus(
                AsyncPollingStatus.Status.DELAYED,
                task.getNesteKjøringEtter(),
                feil.getFeilmelding());

            return Optional.of(status);// er ikke ferdig, men ok å videresende til visning av behandling med feilmelding der.
        }
    }

}
