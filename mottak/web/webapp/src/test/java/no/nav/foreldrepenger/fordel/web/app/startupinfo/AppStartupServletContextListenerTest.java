package no.nav.foreldrepenger.fordel.web.app.startupinfo;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.ServletContextEvent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.web.app.startupinfo.AppStartupInfoLogger;
import no.nav.foreldrepenger.fordel.web.app.startupinfo.AppStartupServletContextListener;

public class AppStartupServletContextListenerTest {

    private AppStartupServletContextListener listener; // objekter vi tester

    private AppStartupInfoLogger mockAppStartupInfoLogger;

    @Before
    public void setup() {
        listener = new AppStartupServletContextListener();
        mockAppStartupInfoLogger = mock(AppStartupInfoLogger.class);
        listener.setAppStartupInfoLogger(mockAppStartupInfoLogger);
    }

    @Test
    public void test_contextInitialized_ok() {
        listener.contextInitialized(mock(ServletContextEvent.class));

        verify(mockAppStartupInfoLogger).logAppStartupInfo();
    }

    /*@Test
    public void test_contextInitialized_exception() {
        doThrow(new RuntimeException("!")).when(mockAppStartupInfoLogger).logAppStartupInfo();

        listener.contextInitialized(mock(ServletContextEvent.class));

        loggggSniffer.assertHasErrorMessage("FP-753407");
    }*/


    @Test
    public void test_contextDestroyed_ok() {
        listener.contextDestroyed(mock(ServletContextEvent.class));
    }
}
