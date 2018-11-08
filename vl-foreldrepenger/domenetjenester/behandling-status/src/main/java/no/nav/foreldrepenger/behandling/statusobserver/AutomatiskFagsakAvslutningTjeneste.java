package no.nav.foreldrepenger.behandling.statusobserver;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import no.nav.foreldrepenger.behandling.statusobserver.task.AutomatiskFagsakAvslutningTask;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class AutomatiskFagsakAvslutningTjeneste {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AutomatiskFagsakAvslutningTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private FagsakRepository fagsakRepository;
    private OppdaterFagsakStatusFelles oppdaterFagsakStatusFelles;

    AutomatiskFagsakAvslutningTjeneste() {
        // For CDI?
    }

    @Inject
    public AutomatiskFagsakAvslutningTjeneste(BehandlingRepository behandlingRepository,
                                              ProsessTaskRepository prosessTaskRepository,
                                              FagsakRepository fagsakRepository,
                                              OppdaterFagsakStatusFelles oppdaterFagsakStatusFelles) {
        this.behandlingRepository = behandlingRepository;
        this.prosessTaskRepository = prosessTaskRepository;
        this.fagsakRepository = fagsakRepository;
        this.oppdaterFagsakStatusFelles = oppdaterFagsakStatusFelles;
    }

    public String avsluttFagsaker() {
        List<Fagsak> løpendeFagsaker = fagsakRepository.hentForStatus(FagsakStatus.LØPENDE);
        List<Behandling> kontrollKandidater = new ArrayList<>();

        løpendeFagsaker.forEach(lf -> behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(lf.getId()).ifPresent(beh -> {
            if (oppdaterFagsakStatusFelles.ingenLøpendeYtelsesvedtak(beh)) {
                kontrollKandidater.add(beh);
            }
        }));

        String gruppe = null;

        String callId = MDCOperations.getCallId();
        callId = (callId == null ? MDCOperations.generateCallId() : callId) + "_";

        for (Behandling kandidat : kontrollKandidater) {
            String nyCallId = callId + kandidat.getId();
            log.info("{} oppretter task med ny callId: {} ", getClass().getSimpleName(), nyCallId);
            gruppe = opprettFagsakAvslutningTask(kandidat, nyCallId);
        }
        //TODO(OJR) må endres i forbindelsen med at løsningen ser på task_grupper på en annet måte nå, hvis en prosess feiler i en gruppe stopper alt opp..
        return gruppe;
    }

    private String opprettFagsakAvslutningTask(Behandling kandidat, String callId) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskFagsakAvslutningTask.TASKNAME);
        prosessTaskData.setBehandling(kandidat.getFagsakId(), kandidat.getId(), kandidat.getAktørId().getId());
        prosessTaskData.setSekvens("1");
        prosessTaskData.setPrioritet(100);

        // unik per task da det er ulike tasks for hver behandling
        prosessTaskData.setCallId(callId);

        prosessTaskRepository.lagre(prosessTaskData);
        return prosessTaskData.getGruppe();
    }

    public List<TaskStatus> hentStatusForFagsakAvslutningGruppe(String gruppe) {
        return prosessTaskRepository.finnStatusForTaskIGruppe(AutomatiskFagsakAvslutningTask.TASKNAME, gruppe);
    }
}
