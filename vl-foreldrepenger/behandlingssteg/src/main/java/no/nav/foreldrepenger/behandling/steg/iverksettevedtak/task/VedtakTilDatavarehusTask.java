package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.datavarehus.tjeneste.DatavarehusTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(VedtakTilDatavarehusTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class VedtakTilDatavarehusTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "iverksetteVedtak.vedtakTilDatavarehus";

    private DatavarehusTjeneste datavarehusTjeneste;

    VedtakTilDatavarehusTask() {
        // for CDI proxy
    }

    @Inject
    public VedtakTilDatavarehusTask(DatavarehusTjeneste datavarehusTjeneste) {
        this.datavarehusTjeneste = datavarehusTjeneste;
    }

    @Timed
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        datavarehusTjeneste.opprettOgLagreVedtakXml(behandlingId);
    }
}
