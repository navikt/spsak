package no.nav.foreldrepenger.grensesnittavstemming.queue.producer;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import no.nav.vedtak.felles.integrasjon.jms.ExternalQueueProducer;
import no.nav.vedtak.felles.integrasjon.jms.JmsKonfig;
import no.nav.vedtak.felles.integrasjon.jms.JmsMessage;

@ApplicationScoped
public class GrensesnittavstemmingJmsProducer extends ExternalQueueProducer {

    public GrensesnittavstemmingJmsProducer() {
        // CDI
    }

    @Inject
    public GrensesnittavstemmingJmsProducer(@Named("grensesnittavstemmingjmsproducerkonfig") JmsKonfig konfig) {
        super(konfig);
    }

    public void sendGrensesnittavstemming(String xml) {
        sendTextMessage(JmsMessage.builder().withMessage(xml).build());
    }

    @Override
    @Resource(mappedName = GrensesnittavstemmingJmsProducerKonfig.JNDI_JMS_CONNECTION_FACTORY)
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        super.setConnectionFactory(connectionFactory);
    }

    @Override
    @Resource(mappedName = GrensesnittavstemmingJmsProducerKonfig.JNDI_QUEUE)
    public void setQueue(Queue queue) {
        super.setQueue(queue);
    }
}
