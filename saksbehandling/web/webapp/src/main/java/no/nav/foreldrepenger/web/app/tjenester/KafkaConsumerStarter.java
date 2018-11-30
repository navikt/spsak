package no.nav.foreldrepenger.web.app.tjenester;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import no.nav.sykepenger.kafka.KafkaConsumerManager;

/**
 * Triggers start of Kafka consumers.
 */
public class KafkaConsumerStarter implements ServletContextListener {

    @Inject //NOSONAR
    private KafkaConsumerManager kafkaConsumerManager; //NOSONAR

    public KafkaConsumerStarter() { //NOSONAR
        // For CDI
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        kafkaConsumerManager.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        kafkaConsumerManager.stop();
    }
}
