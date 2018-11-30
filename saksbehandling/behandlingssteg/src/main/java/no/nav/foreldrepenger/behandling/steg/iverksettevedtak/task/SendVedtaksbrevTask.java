package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.SendVedtaksbrev;
import no.nav.foreldrepenger.behandlingskontroll.task.BehandlingProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(SendVedtaksbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendVedtaksbrevTask extends BehandlingProsessTask {

    public static final String TASKTYPE = "iverksetteVedtak.sendVedtaksbrev";

    private static final Logger log = LoggerFactory.getLogger(SendVedtaksbrevTask.class);

    private SendVedtaksbrev tjeneste;

    SendVedtaksbrevTask() {
        // for CDI proxy
    }

    @Inject
    public SendVedtaksbrevTask(SendVedtaksbrev tjeneste, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.tjeneste = tjeneste;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        tjeneste.sendVedtaksbrev(behandlingId);
        log.info("Utført for behandling: {}", behandlingId);
    }
}
