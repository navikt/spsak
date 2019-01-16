package no.nav.foreldrepenger.behandlingskontroll;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * Task som utfører noe på en behandling, før prosessen kjøres videre.
 * Sikrer at behandlingslås task på riktig plass. Tasks som forsøker å kjøre behandling videre bør extende denne.
*/
public abstract class BehandlingskontrollProsessTask implements ProsessTaskHandler {

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste;

    protected BehandlingskontrollProsessTask(GrunnlagRepositoryProvider grunnlagRepositoryProvider,
                                             BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                             BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste) {
        this.behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.behandlingskontrollTaskTjeneste = behandlingskontrollTaskTjeneste;
    }

    protected BehandlingskontrollProsessTask() {
        // for CDI proxy
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        Behandling behandling = finnBehandling(behandlingId);

        prosesser(behandling);

        lagreBehandling(behandling, kontekst);
        fortsettBehandlingen(prosessTaskData);
    }

    protected Behandling finnBehandling(Long behandlingId) {
        return behandlingRepository.hentBehandling(behandlingId);
    }

    protected void fortsettBehandlingen(ProsessTaskData prosessTaskData) {
        behandlingskontrollTaskTjeneste.opprettFortsettBehandlingTaskNesteSekvens(prosessTaskData);
    }

    protected void lagreBehandling(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    protected abstract void prosesser(Behandling behandling);

}
