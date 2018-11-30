package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak.REGISTRER_SØKNAD;
import static no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveRegistrerSøknadTask.TASKTYPE;

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
public class OpprettOppgaveRegistrerSøknadTask extends BehandlingProsessTask {
    public static final String TASKTYPE = "oppgavebehandling.opprettOppgaveRegistrerSøknad";
    private static final Logger log = LoggerFactory.getLogger(OpprettOppgaveRegistrerSøknadTask.class);
    private OppgaveTjeneste oppgaveTjeneste;

    OpprettOppgaveRegistrerSøknadTask() {
        // for CDI proxy
    }

    @Inject
    public OpprettOppgaveRegistrerSøknadTask(OppgaveTjeneste oppgaveTjeneste, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.oppgaveTjeneste = oppgaveTjeneste;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        String oppgaveId = oppgaveTjeneste.opprettBasertPåBehandlingId(prosessTaskData.getBehandlingId(), REGISTRER_SØKNAD);
        log.info("Oppgave opprettet i GSAK for å registrere søknad. Oppgavenummer: {}", oppgaveId); // NOSONAR
    }
}
