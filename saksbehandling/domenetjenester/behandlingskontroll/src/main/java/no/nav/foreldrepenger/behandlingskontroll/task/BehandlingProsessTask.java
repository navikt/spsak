package no.nav.foreldrepenger.behandlingskontroll.task;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * Task som utfører noe på en behandling, før prosessen kjøres videre.
 * Sikrer at behandlingslås task på riktig plass.
 * Tasks som forsøker å kjøre behandling videre bør extende denne.
 */
public abstract class BehandlingProsessTask implements ProsessTaskHandler {

    private BehandlingLåsRepository behandlingLåsRepository;

    protected BehandlingProsessTask(GrunnlagRepositoryProvider grunnlagRepositoryProvider) {
        this.behandlingLåsRepository = grunnlagRepositoryProvider.getBehandlingLåsRepository();
    }

    protected BehandlingProsessTask() {
        // for CDI proxy
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        behandlingLåsRepository.taLås(behandlingId);
        prosesser(prosessTaskData);
    }

    protected abstract void prosesser(ProsessTaskData prosessTaskData);

}
