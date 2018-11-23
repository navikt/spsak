package no.nav.sykepenger.kafka;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.apptjeneste.AppServiceHandler;

@ApplicationScoped
public class KafkaConsumerManager implements AppServiceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerManager.class);

    private List<KafkaConsumer> consumerList;

    KafkaConsumerManager() {
    }

    @Inject
    public KafkaConsumerManager(@Any Instance<KafkaConsumer> consumersInstance) {
        consumerList = new ArrayList<>();
        for (KafkaConsumer consumer : consumersInstance) {
            consumerList.add(consumer);
        }
        LOGGER.info("initConsumers la til {} consumers", consumerList.size());
    }

    // Får inn (indirekte) liste over alle beans av type KafkaConsumer
    @Inject
    public void initConsumers(@Any Instance<KafkaConsumer> consumersInstance) { // NOSONAR Joda, kalles av CDI
        consumerList = new ArrayList<>();
        for (KafkaConsumer consumer : consumersInstance) {
            consumerList.add(consumer);
        }
        LOGGER.info("initConsumers la til {} consumers", consumerList.size());
    }


    @Override
    public void start() {
        LOGGER.debug("start ...");
        for (KafkaConsumer consumer : consumerList) {
            LOGGER.info("starter {}" + consumer.getClass().getSimpleName());
            consumer.start();
        }
        LOGGER.info("startet");
    }

    @Override
    public void stop() {
        LOGGER.debug("stop ...");
        for (KafkaConsumer consumer : consumerList) {
            LOGGER.info("stopper {}" + consumer.getClass().getSimpleName());
            consumer.stop();
        }
        LOGGER.info("stoppet");
    }
}
