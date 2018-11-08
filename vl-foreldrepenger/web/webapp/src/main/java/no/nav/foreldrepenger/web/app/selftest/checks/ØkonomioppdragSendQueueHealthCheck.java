package no.nav.foreldrepenger.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.økonomistøtte.queue.producer.ØkonomioppdragJmsProducer;

@ApplicationScoped
public class ØkonomioppdragSendQueueHealthCheck extends QueueHealthCheck {

    ØkonomioppdragSendQueueHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public ØkonomioppdragSendQueueHealthCheck(ØkonomioppdragJmsProducer client) {
        super(client);
    }

    @Override
    protected String getDescriptionSuffix() {
        return "Økonomioppdrag Send";
    }
}