package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.BerørtBehandlingKontroller;
import no.nav.foreldrepenger.behandlingskontroll.task.FagsakProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(StartBerørtBehandlingTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class StartBerørtBehandlingTask  extends FagsakProsessTask {

    private static final Logger log = LoggerFactory.getLogger(StartBerørtBehandlingTask.class);

    public static final String TASKTYPE = "iverksetteVedtak.startBerørtBehandling";

    private BerørtBehandlingKontroller tjeneste;


    private BehandlingLåsRepository behandlingLåsRepository;

    StartBerørtBehandlingTask() {
        // for CDI proxy
    }
    @Inject
    public StartBerørtBehandlingTask(BerørtBehandlingKontroller tjeneste, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.tjeneste = tjeneste;
        this.behandlingLåsRepository = repositoryProvider.getBehandlingLåsRepository();
    }

    @Timed
    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Long fagsakId = prosessTaskData.getFagsakId();
        Long behandlingId = prosessTaskData.getBehandlingId();

        // ta skrive lås før noe gjøres
        behandlingLåsRepository.taLås(behandlingId);

        tjeneste.vurderNesteOppgaveIBehandlingskø(behandlingId);
        log.info("Utført for fagsak: {}", fagsakId);
    }
}
