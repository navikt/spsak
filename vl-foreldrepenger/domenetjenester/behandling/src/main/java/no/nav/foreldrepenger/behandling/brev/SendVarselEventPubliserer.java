package no.nav.foreldrepenger.behandling.brev;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * Håndterer fyring av events via CDI når det skal sendes brev
 */
@ApplicationScoped
class SendVarselEventPubliserer {

    private BeanManager beanManager;

    SendVarselEventPubliserer() {
        // null ctor, publiserer ingen events
    }

    @Inject
    SendVarselEventPubliserer(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public void fireEvent(SendVarselEvent event) {
        doFireEvent(event);
    }

    /** Fyrer event via BeanManager slik at håndtering av events som subklasser andre events blir korrekt. */
    private void doFireEvent(SendVarselEvent event) {
        if (beanManager == null) {
            return;
        }
        beanManager.fireEvent(event, new Annotation[] {});
    }

}
