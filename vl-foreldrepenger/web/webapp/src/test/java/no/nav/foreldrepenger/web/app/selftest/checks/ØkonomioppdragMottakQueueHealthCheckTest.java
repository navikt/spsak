package no.nav.foreldrepenger.web.app.selftest.checks;

import org.junit.Test;

import no.nav.foreldrepenger.økonomistøtte.queue.consumer.ØkonomioppdragAsyncJmsConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ØkonomioppdragMottakQueueHealthCheckTest {

    @Test
    public void skalRetunererKøNavn() {
        ØkonomioppdragAsyncJmsConsumer økonomioppdragAsyncJmsConsumer = mock(ØkonomioppdragAsyncJmsConsumer.class);

        ØkonomioppdragMottakQueueHealthCheck økonomioppdragMottakQueueHealthCheck = new ØkonomioppdragMottakQueueHealthCheck(økonomioppdragAsyncJmsConsumer);

        assertThat(økonomioppdragMottakQueueHealthCheck.getDescriptionSuffix()).isEqualTo("Økonomioppdrag Mottak");
    }
}