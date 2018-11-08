package no.nav.foreldrepenger.dokumentbestiller.forlengelsesbrev.tjeneste;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.dokumentbestiller.api.SendForlengelsesbrevTaskProperties;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

@ApplicationScoped
public class SendForlengelsesbrevTjeneste {

    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingKandidaterRepository behandlingKandidaterRepository;

    SendForlengelsesbrevTjeneste() {
        //For CDI?
    }

    @Inject
    public SendForlengelsesbrevTjeneste(BehandlingKandidaterRepository behandlingKandidaterRepository,
            ProsessTaskRepository prosessTaskRepository) {
        this.behandlingKandidaterRepository = behandlingKandidaterRepository;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    public String sendForlengelsesbrev() {
        List<Behandling> kandidater = behandlingKandidaterRepository.finnBehandlingerMedUtløptBehandlingsfrist();
        String gruppe = null;
        for (Behandling kandidat : kandidater) {
            gruppe = opprettSendForlengelsesbrevTask(kandidat);
        }
        //TODO(OJR) må endres i forbindelsen med at løsningen ser på task_grupper på en annet måte nå, hvis en prosess feiler i en gruppe stopper alt opp..
        return gruppe == null ? "0" : gruppe;
    }

    private String opprettSendForlengelsesbrevTask(Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(SendForlengelsesbrevTaskProperties.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");
        prosessTaskData.setPrioritet(100);
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
        return prosessTaskData.getGruppe();
    }

    public List<TaskStatus> hentStatusForForlengelsesbrevBatchGruppe(String gruppe) {
        return prosessTaskRepository.finnStatusForTaskIGruppe(SendForlengelsesbrevTaskProperties.TASKTYPE, gruppe);
    }
}
