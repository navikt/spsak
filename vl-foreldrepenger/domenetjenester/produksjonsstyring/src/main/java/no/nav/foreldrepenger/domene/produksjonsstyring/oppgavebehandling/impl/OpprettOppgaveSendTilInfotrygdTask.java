package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.behandlingskontroll.task.BehandlingProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(OpprettOppgaveSendTilInfotrygdTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class OpprettOppgaveSendTilInfotrygdTask extends BehandlingProsessTask {
    public static final String TASKTYPE = "oppgavebehandling.opprettOppgaveSakTilInfotrygd";
    private static final Logger log = LoggerFactory.getLogger(OpprettOppgaveSendTilInfotrygdTask.class);

    private OppgaveTjeneste oppgaveTjeneste;

    OpprettOppgaveSendTilInfotrygdTask() {
        // for CDI proxy
    }

    @Inject
    public OpprettOppgaveSendTilInfotrygdTask(OppgaveTjeneste oppgaveTjeneste, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.oppgaveTjeneste = oppgaveTjeneste;
    }

    @Timed
    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        String oppgaveId = oppgaveTjeneste.opprettOppgaveSakSkalTilInfotrygd(prosessTaskData.getBehandlingId());
        log.info("Oppgave opprettet i GSAK slik at Infotrygd kan behandle saken videre. Oppgavenummer: {}", oppgaveId);
    }
}
