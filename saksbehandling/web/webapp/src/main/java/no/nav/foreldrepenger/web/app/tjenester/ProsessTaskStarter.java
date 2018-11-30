package no.nav.foreldrepenger.web.app.tjenester;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import no.nav.vedtak.felles.prosesstask.impl.TaskManager;

/**
 * Initialiserer bakgrunns tasks.
 */
@WebListener
public class ProsessTaskStarter implements ServletContextListener {

    @Inject
    private TaskManager taskManager;  // NOSONAR

    public ProsessTaskStarter() { // NOSONAR
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        taskManager.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        taskManager.stop();
    }

}
