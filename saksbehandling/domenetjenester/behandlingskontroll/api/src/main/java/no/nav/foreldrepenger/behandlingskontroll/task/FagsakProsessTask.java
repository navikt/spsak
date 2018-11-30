package no.nav.foreldrepenger.behandlingskontroll.task;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * Task som utfører noe på en fagsak, før prosessen kjøres videre.
 * Sikrer at fagsaklås task på riktig plass..
 */
public abstract class FagsakProsessTask implements ProsessTaskHandler {

    private FagsakLåsRepository låsRepository;

    protected FagsakProsessTask(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.låsRepository = behandlingRepositoryProvider.getFagsakLåsRepository();
    }

    protected FagsakProsessTask() {
        // for CDI proxy
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long fagsakId = prosessTaskData.getFagsakId();
        låsRepository.taLås(fagsakId);
        prosesser(prosessTaskData);
    }

    protected abstract void prosesser(ProsessTaskData prosessTaskData);

}
