package no.nav.foreldrepenger.grensesnittavstemming.queue.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;

@Named("grensesnittavstemmingjmsproducerkonfig")
@ApplicationScoped
public class GrensesnittavstemmingJmsProducerKonfig extends BaseJmsKonfig {

    public static final String JNDI_QUEUE = "jms/QueueFpsakGrensesnittavstemmingSend";

    private static final String UT_QUEUE_PREFIX = "RAY.AVSTEM_DATA";

    public GrensesnittavstemmingJmsProducerKonfig() {
        super(UT_QUEUE_PREFIX);
    }
}
