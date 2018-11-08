package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.AvsluttBehandling;
import no.nav.foreldrepenger.behandlingskontroll.task.BehandlingProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(AvsluttBehandlingTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class AvsluttBehandlingTask extends BehandlingProsessTask {

    public static final String TASKTYPE = "iverksetteVedtak.avsluttBehandling";
    private static final Logger log = LoggerFactory.getLogger(AvsluttBehandlingTask.class);
    private AvsluttBehandling tjeneste;

    AvsluttBehandlingTask() {
        // for CDI proxy
    }

    @Inject
    public AvsluttBehandlingTask(AvsluttBehandling tjeneste, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.tjeneste = tjeneste;
    }

    @Timed
    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        tjeneste.avsluttBehandling(behandlingId);
        log.info("Utført for behandling: {}", behandlingId);
    }
}
