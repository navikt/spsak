package no.nav.sykepenger.kafka;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KafkaConsumer {

    private final String topic;
    private final KafkaStreams streams;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    KafkaConsumer() {
        // CDI
        topic = null;
        streams = null;
    }

    KafkaConsumer(String topic) {
        this.topic = topic;

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        final StreamsBuilder builder = new StreamsBuilder();

        builder.<String, String>stream(topic)
            .foreach((key, val) -> {
                if (val.equals("error3")) {
                    throw new RuntimeException("UGH");
                } else {
                    handleMessage(key, val);
                }
            });

        final Topology topology = builder.build();
        streams = new KafkaStreams(topology, props);
        streams.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.error("Feil ved consumering av kafka-topic " + topic, e);
            }
        });

        // attach shutdown handler to catch control-c
        /*Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
            }
        });*/
    }

    protected abstract void handleMessage(String key, String payload);

    public void start() {
        streams.start();
    }

    public void stop() {
        streams.close();
    }

}
