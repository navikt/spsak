package no.nav.foreldrepenger.web.app.selftest.checks;

import no.nav.foreldrepenger.grensesnittavstemming.queue.producer.GrensesnittavstemmingJmsProducer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GrensesnittavstemmingQueueHealthCheck extends QueueHealthCheck {

    GrensesnittavstemmingQueueHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public GrensesnittavstemmingQueueHealthCheck(GrensesnittavstemmingJmsProducer client) {
        super(client);
    }

    @Override
    protected String getDescriptionSuffix() {
        return "Grensesnittavstemming";
    }
}
