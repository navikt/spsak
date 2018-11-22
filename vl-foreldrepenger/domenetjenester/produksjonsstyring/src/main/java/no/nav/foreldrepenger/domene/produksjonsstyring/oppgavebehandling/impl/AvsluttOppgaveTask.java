package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.AvsluttOppgaveTaskProperties.TASKTYPE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.task.BehandlingProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class AvsluttOppgaveTask extends BehandlingProsessTask {
    private OppgaveTjeneste oppgaveTjeneste;

    AvsluttOppgaveTask() {
        // for CDI proxy
    }
    
    @Inject
    public AvsluttOppgaveTask(OppgaveTjeneste oppgaveTjeneste, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.oppgaveTjeneste = oppgaveTjeneste;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        String oppgaveId = prosessTaskData.getOppgaveId()
            .orElseThrow(() -> new IllegalStateException("Mangler oppgaveId"));

        oppgaveTjeneste.avslutt(prosessTaskData.getBehandlingId(), oppgaveId);
    }
}
