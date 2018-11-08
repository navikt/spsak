package no.nav.foreldrepenger.web.app.tjenester;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import no.nav.vedtak.felles.integrasjon.jms.QueueConsumerManager;

/**
 * Triggers start of async JMS consumers.
 */
public class AsyncJmsConsumerStarter implements ServletContextListener {

    @Inject //NOSONAR
    private QueueConsumerManager asyncJmsConsumerManager; //NOSONAR

    public AsyncJmsConsumerStarter() { //NOSONAR
        // For CDI
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        asyncJmsConsumerManager.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        asyncJmsConsumerManager.stop();
    }
}
