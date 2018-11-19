package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.behandlingskontroll.task.BehandlingProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.vedtak.VurderOmArenaYtelseSkalOpphøre;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(VurderOppgaveArenaTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class VurderOppgaveArenaTask extends BehandlingProsessTask {

    private static final Logger log = LoggerFactory.getLogger(VurderOppgaveArenaTask.class);

    public static final String TASKTYPE = "iverksetteVedtak.oppgaveArena";

    private BehandlingRepository behandlingRepository;
    private VurderOmArenaYtelseSkalOpphøre vurdereOmArenaYtelseSkalOpphøre;

    VurderOppgaveArenaTask() {
        // for CDI proxy
    }

    @Inject
    public VurderOppgaveArenaTask(BehandlingRepositoryProvider repositoryProvider,
                                  VurderOmArenaYtelseSkalOpphøre vurdereOmArenaYtelseSkalOpphøre) {
        super(repositoryProvider);
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vurdereOmArenaYtelseSkalOpphøre = vurdereOmArenaYtelseSkalOpphøre;
    }

    @Timed
    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.getFagsakYtelseType().gjelderForeldrepenger()) {
            vurdereOmArenaYtelseSkalOpphøre.opprettOppgaveHvisArenaytelseSkalOpphøre(prosessTaskData.getAktørId(), behandling);
            log.info("VurderOppgaveArenaTask: Vurderer for behandling: {}", behandlingId); //$NON-NLS-1$
        } else {
            log.info("VurderOppgaveArenaTask: Ikke aktuelt for behandling: {}", behandlingId); //$NON-NLS-1$
        }
    }
}
