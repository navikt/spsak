package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.tjeneste.virksomhet.oppgave.v3.binding.HentOppgaveOppgaveIkkeFunnet;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeResponse;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.HentOppgaveRequest;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.HentOppgaveResponse;
import no.nav.vedtak.felles.integrasjon.oppgave.FinnOppgaveListeRequestMal;
import no.nav.vedtak.felles.integrasjon.oppgave.OppgaveConsumer;

@Alternative
@Priority(1)
@Dependent
public class OppgaveConsumerMock implements OppgaveConsumer {


    @Override
    public FinnOppgaveListeResponse finnOppgaveListe(FinnOppgaveListeRequestMal finnOppgaveListeRequestMal) {
        return new FinnOppgaveListeResponse();
    }

    @Override
    public HentOppgaveResponse hentOppgave(HentOppgaveRequest hentOppgaveRequest) throws HentOppgaveOppgaveIkkeFunnet {
        return new HentOppgaveResponse();
    }
}
