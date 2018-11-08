package no.nav.foreldrepenger.behandling.statusobserver.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.statusobserver.OppdaterFagsakStatus;
import no.nav.foreldrepenger.behandling.statusobserver.OppdaterFagsakStatusProvider;
import no.nav.foreldrepenger.behandlingskontroll.task.FagsakProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;


@ApplicationScoped
@ProsessTask(AutomatiskFagsakAvslutningTask.TASKNAME)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class AutomatiskFagsakAvslutningTask extends FagsakProsessTask {

    public static final String TASKNAME = "behandlingskontroll.fagsakAvslutning";

    private BehandlingRepository behandlingRepository;
    private OppdaterFagsakStatusProvider oppdaterFagsakStatusProvider;

    private BehandlingLåsRepository behandlingLåsRepository;

    AutomatiskFagsakAvslutningTask() {
        // for CDI proxy
    }

    @Inject
    public AutomatiskFagsakAvslutningTask(BehandlingRepositoryProvider repositoryProvider,
                                          OppdaterFagsakStatusProvider oppdaterFagsakStatusProvider) {
        super(repositoryProvider);
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.oppdaterFagsakStatusProvider = oppdaterFagsakStatusProvider;
        this.behandlingLåsRepository = repositoryProvider.getBehandlingLåsRepository();
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();

        // ta write-lock før hent og oppdatering (lager en row-level write lock i db, retur verdi trengs ikke
        this.behandlingLåsRepository.taLås(behandlingId);

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        OppdaterFagsakStatus oppdaterFagsakStatus = oppdaterFagsakStatusProvider.getOppdaterFagsakStatus(behandling);
        oppdaterFagsakStatus.oppdaterFagsakNårBehandlingEndret(behandling);
    }
}
