package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveForBehandlingSendtTilbakeTask.TASKTYPE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.task.BehandlingProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class OpprettOppgaveForBehandlingSendtTilbakeTask extends BehandlingProsessTask {
    public static final String TASKTYPE = "oppgavebehandling.opprettOppgaveSakSendtTilbake";
    private static final Logger log = LoggerFactory.getLogger(OpprettOppgaveForBehandlingSendtTilbakeTask.class);
    private OppgaveTjeneste oppgaveTjeneste;

    OpprettOppgaveForBehandlingSendtTilbakeTask() {
        // for CDI proxy
    }

    @Inject
    public OpprettOppgaveForBehandlingSendtTilbakeTask(BehandlingRepositoryProvider repositoryProvider, OppgaveTjeneste oppgaveTjeneste) {
        super(repositoryProvider);
        this.oppgaveTjeneste = oppgaveTjeneste;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        String beskrivelse = "Sak har blitt sendt tilbake fra beslutter";
        String oppgaveId = oppgaveTjeneste.opprettBehandleOppgaveForBehandlingMedPrioritetOgFrist(prosessTaskData.getBehandlingId(),
            beskrivelse, true, 0);
        if (oppgaveId != null) {
            log.info("Oppgave opprettet i GSAK for å behandle sak sendt tilbake. Oppgavenummer: {}", oppgaveId); //NOSONAR
        }
    }
}
