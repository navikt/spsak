package no.nav.foreldrepenger.web.app.selftest.checks;

import org.junit.Test;

import no.nav.foreldrepenger.økonomistøtte.queue.producer.ØkonomioppdragJmsProducer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ØkonomioppdragSendQueueHealthCheckTest {

    @Test
    public void skalRetunererKøNavn() {
        ØkonomioppdragJmsProducer økonomioppdragJmsProducer = mock(ØkonomioppdragJmsProducer.class);

        ØkonomioppdragSendQueueHealthCheck økonomioppdragSendQueueHealthCheck = new ØkonomioppdragSendQueueHealthCheck(økonomioppdragJmsProducer);

        assertThat(økonomioppdragSendQueueHealthCheck.getDescriptionSuffix()).isEqualTo("Økonomioppdrag Send");
    }
}