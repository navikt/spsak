package no.nav.foreldrepenger.behandlingsprosess.automatiskgjenopptagelse.tjeneste;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class AutomatiskGjenopptagelseTjeneste {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AutomatiskGjenopptagelseTjeneste.class);

    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingKandidaterRepository behandlingKandidaterRepository;

    @Inject
    public AutomatiskGjenopptagelseTjeneste(BehandlingKandidaterRepository behandlingKandidaterRepository,
                                            ProsessTaskRepository prosessTaskRepository) {
        this.behandlingKandidaterRepository = behandlingKandidaterRepository;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    public AutomatiskGjenopptagelseTjeneste() {
        // for CDI
    }

    public String gjenopptaBehandlinger() {
        List<Behandling> behandlingListe = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();
        String callId = MDCOperations.getCallId();
        callId = (callId == null ? MDCOperations.generateCallId() : callId) + "_";

        for (Behandling behandling : behandlingListe) {
            String nyCallId = callId + behandling.getId();
            log.info("oppretter task med ny callId: {} ", nyCallId);
            opprettProsessTask(behandling, nyCallId);
        }

        //TODO(OJR) må endres i forbindelsen med at løsningen ser på task_grupper på en annet måte nå, hvis en prosess feiler i en gruppe stopper alt opp..
        return "-";
    }

    private void opprettProsessTask(Behandling behandling, String callId) {

        ProsessTaskData prosessTaskData = new ProsessTaskData(GjenopptaBehandlingTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");
        prosessTaskData.setPrioritet(100);

        // unik per task da det gjelder  ulike behandlinger, gjenbruker derfor ikke
        prosessTaskData.setCallId(callId);

        prosessTaskRepository.lagre(prosessTaskData);
    }

    public List<TaskStatus> hentStatusForGjenopptaBehandlingGruppe(String gruppe) {

        return prosessTaskRepository.finnStatusForTaskIGruppe(GjenopptaBehandlingTask.TASKTYPE, gruppe);
    }
}
