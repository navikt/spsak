package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagConsumerConfig;

@Alternative
@Priority(1)
class MeldekortUtbetalingsgrunnlagConsumerConfigMock extends MeldekortUtbetalingsgrunnlagConsumerConfig {
    public MeldekortUtbetalingsgrunnlagConsumerConfigMock() {
        super(null);
    }

}
