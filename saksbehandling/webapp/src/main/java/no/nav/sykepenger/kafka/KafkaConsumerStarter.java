package no.nav.sykepenger.kafka;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import no.nav.vedtak.kafka.KafkaConsumerManager;

/**
 * Triggers start of Kafka consumers.
 */
@WebListener
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
