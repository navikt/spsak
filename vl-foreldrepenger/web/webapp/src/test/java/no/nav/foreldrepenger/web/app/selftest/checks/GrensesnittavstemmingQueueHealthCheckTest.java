package no.nav.foreldrepenger.web.app.selftest.checks;

import no.nav.foreldrepenger.grensesnittavstemming.queue.producer.GrensesnittavstemmingJmsProducer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class GrensesnittavstemmingQueueHealthCheckTest {

    @Test
    public void skalRetunererKøNavn() {
        GrensesnittavstemmingJmsProducer grensesnittavstemmingJmsProducer = mock(GrensesnittavstemmingJmsProducer.class);

        GrensesnittavstemmingQueueHealthCheck grensesnittavstemmingQueueHealthCheck = new GrensesnittavstemmingQueueHealthCheck(grensesnittavstemmingJmsProducer);

        assertThat(grensesnittavstemmingQueueHealthCheck.getDescriptionSuffix()).isEqualTo("Grensesnittavstemming");
    }
}
