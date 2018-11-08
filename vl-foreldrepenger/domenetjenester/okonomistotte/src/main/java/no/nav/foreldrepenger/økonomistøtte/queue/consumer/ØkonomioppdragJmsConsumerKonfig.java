package no.nav.foreldrepenger.økonomistøtte.queue.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;

@Named("økonomioppdragjmsconsumerkonfig")
@ApplicationScoped
public class ØkonomioppdragJmsConsumerKonfig extends BaseJmsKonfig {

    public static final String JNDI_QUEUE = "jms/QueueFpsakOkonomiOppdragMotta";

    private static final String INN_QUEUE_PREFIX = "fpsak_okonomi_oppdrag_mottak";

    public ØkonomioppdragJmsConsumerKonfig() {
        super(INN_QUEUE_PREFIX);
    }
}
