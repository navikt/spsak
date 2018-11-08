package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumer;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumerConfig;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumerProducer;

@Alternative
@Priority(1)
@Dependent
public class BehandleoppgaveConsumerProducerMock extends BehandleoppgaveConsumerProducer {

    @Override
    public void setConfig(BehandleoppgaveConsumerConfig consumerConfig) {
        // no-op
    }

    @Override
    public BehandleoppgaveConsumer behandleoppgaveConsumer() {
        // returnerer null, ingen i integrasjonstest so bruker foreløbig
        return null;
    }
}
