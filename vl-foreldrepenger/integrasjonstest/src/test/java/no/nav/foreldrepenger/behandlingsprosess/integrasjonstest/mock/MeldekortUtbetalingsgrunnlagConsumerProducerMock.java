package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;


import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.MeldekortTestSett;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.AktoerId;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagConsumer;
import no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagConsumerProducer;

@Alternative
@Priority(1)
@Dependent
class MeldekortUtbetalingsgrunnlagConsumerProducerMock extends MeldekortUtbetalingsgrunnlagConsumerProducer {

    @Override
    public MeldekortUtbetalingsgrunnlagConsumer meldekortUtbetalingsgrunnlagConsumer() {
        class MeldekortUtbetalingsgrunnlagConsumerMock implements MeldekortUtbetalingsgrunnlagConsumer {

            @Override
            public FinnMeldekortUtbetalingsgrunnlagListeResponse finnMeldekortUtbetalingsgrunnlagListe(FinnMeldekortUtbetalingsgrunnlagListeRequest var1) {
                String aktørId = Optional.ofNullable(var1.getIdent()).map(it -> ((AktoerId) it).getAktoerId()).orElse(null);
                return MeldekortTestSett.finnResponse(aktørId);
            }
        }
        return new MeldekortUtbetalingsgrunnlagConsumerMock();
    }

}
