package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak.GODKJENNE_VEDTAK;
import static no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveGodkjennVedtakTask.TASKTYPE;

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
@ProsessTask(TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class OpprettOppgaveGodkjennVedtakTask extends BehandlingProsessTask {
    public static final String TASKTYPE = "oppgavebehandling.opprettOppgaveGodkjennVedtak";
    private static final Logger log = LoggerFactory.getLogger(OpprettOppgaveGodkjennVedtakTask.class);
    private OppgaveTjeneste oppgaveTjeneste;

    OpprettOppgaveGodkjennVedtakTask() {
        // for CDI proxy
    }
    
    @Inject
    public OpprettOppgaveGodkjennVedtakTask(OppgaveTjeneste oppgaveTjeneste, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.oppgaveTjeneste = oppgaveTjeneste;
    }

    @Timed
    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        String oppgaveId = oppgaveTjeneste.opprettBasertPåBehandlingId(prosessTaskData.getBehandlingId(), GODKJENNE_VEDTAK);
        if (oppgaveId != null) {
            log.info("Oppgave opprettet i GSAK for å godkjenne vedtak. Oppgavenummer: {}", oppgaveId); //NOSONAR
        }
    }
}
