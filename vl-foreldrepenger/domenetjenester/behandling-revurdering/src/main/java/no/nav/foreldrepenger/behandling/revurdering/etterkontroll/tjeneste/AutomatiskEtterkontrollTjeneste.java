package no.nav.foreldrepenger.behandling.revurdering.etterkontroll.tjeneste;

import java.time.Period;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;

import no.nav.foreldrepenger.behandling.revurdering.etterkontroll.task.AutomatiskEtterkontrollTask;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.etterkontroll.BehandlingEtterkontrollRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.etterkontroll.EtterkontrollLogg;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class AutomatiskEtterkontrollTjeneste {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AutomatiskEtterkontrollTjeneste.class);
        
    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private Period etterkontrollTidTilbake;
    private BehandlingEtterkontrollRepository behandlingEtterkontrollRepository;

    AutomatiskEtterkontrollTjeneste() {
        // For CDI?
    }

    @Inject
    public AutomatiskEtterkontrollTjeneste(BehandlingRepository behandlingRepository,
            ProsessTaskRepository prosessTaskRepository,
            BehandlingEtterkontrollRepository behandlingEtterkontrollRepository,
            @KonfigVerdi("etterkontroll.tid.tilbake") Instance<Period> etterkontrollTidTilbake) {
        this.behandlingRepository = behandlingRepository;
        this.prosessTaskRepository = prosessTaskRepository;
        this.behandlingEtterkontrollRepository = behandlingEtterkontrollRepository;
        this.etterkontrollTidTilbake = etterkontrollTidTilbake.get();
    }

    public void etterkontrollerBehandlinger() {
        List<Behandling> kontrollKandidater = behandlingEtterkontrollRepository.finnKandidaterForAutomatiskEtterkontroll(etterkontrollTidTilbake);

        String callId = MDCOperations.getCallId();
        callId = (callId == null ? MDCOperations.generateCallId() : callId) + "_";
        
        for (Behandling kandidat : kontrollKandidater) {
            String nyCallId = callId + kandidat.getId();
            log.info("{} oppretter task med ny callId: {} ", getClass().getSimpleName(), nyCallId);
            opprettEtterkontrollTask(kandidat, nyCallId);
            EtterkontrollLogg etterkontrollLogg = new EtterkontrollLogg.Builder(kandidat).build();
            behandlingEtterkontrollRepository.lagre(etterkontrollLogg, behandlingRepository.taSkriveLås(kandidat));
        }
    }

    private String opprettEtterkontrollTask(Behandling kandidat, String callId) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskEtterkontrollTask.TASKNAME);
        prosessTaskData.setBehandling(kandidat.getFagsakId(), kandidat.getId(), kandidat.getAktørId().getId());
        prosessTaskData.setSekvens("1");
        prosessTaskData.setPrioritet(100);

        // unik per task da det er ulike tasks for hver behandling
        prosessTaskData.setCallId(callId);
        
        prosessTaskRepository.lagre(prosessTaskData);
        //TODO(OJR) må endres i forbindelsen med at løsningen ser på task_grupper på en annet måte nå, hvis en prosess feiler i en gruppe stopper alt opp..
        return prosessTaskData.getGruppe();
    }

    public List<TaskStatus> hentStatusForEtterkontrollGruppe(String gruppe) {
        return prosessTaskRepository.finnStatusForTaskIGruppe(AutomatiskEtterkontrollTask.TASKNAME, gruppe);
    }
}
