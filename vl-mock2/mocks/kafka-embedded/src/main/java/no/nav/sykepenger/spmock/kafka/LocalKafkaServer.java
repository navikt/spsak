package no.nav.sykepenger.spmock.kafka;

import java.util.Properties;

public class LocalKafkaServer {

    private static KafkaLocal kafka;

    public static void startKafka() {
        startKafka(2181, 9092);
    }

    private static int zookeeperPort;
    private static int kafkaBrokerPort;

    public static int getZookeperPort() {
        return zookeeperPort;
    }

    public static int getKafkaBrokerPort() {
        return kafkaBrokerPort;
    }

    public static void startKafka(final int zookeeperPort, final int kafkaBrokerPort){
        LocalKafkaServer.kafkaBrokerPort = kafkaBrokerPort;
        LocalKafkaServer.zookeeperPort = zookeeperPort;
        Properties kafkaProperties = new Properties();
        Properties zkProperties = new Properties();

        kafkaProperties.put("zookeeper.connect", "localhost:" + zookeeperPort);
        kafkaProperties.put("offsets.topic.replication.factor", "1");
        kafkaProperties.put("logs.dirs", "target/kafka-logs");
        kafkaProperties.put("listeners", "PLAINTEXT://:" + kafkaBrokerPort);

        final String zookeeperTempInstanceDataDir = ""+ System.currentTimeMillis(); // For å hindre NodeExists-feil på restart p.g.a. at data allerede finnes i katalogen.
        zkProperties.put("dataDir", "target/zookeeper/" + zookeeperTempInstanceDataDir);
        zkProperties.put("clientPort", "" + zookeeperPort);
        zkProperties.put("maxClientCnxns", "0");

        try {
            kafka = new KafkaLocal(kafkaProperties, zkProperties);
        } catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}
