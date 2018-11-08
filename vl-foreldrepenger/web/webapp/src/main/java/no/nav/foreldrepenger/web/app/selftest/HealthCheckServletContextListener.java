package no.nav.foreldrepenger.web.app.selftest;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;

import javax.inject.Inject;

public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

    private HealthCheckRegistry healthCheckRegistry;

    public HealthCheckServletContextListener() {
        // for CDi
    }

    @Inject
    public HealthCheckServletContextListener(HealthCheckRegistry healthCheckRegistry) {
        this.healthCheckRegistry = healthCheckRegistry;
    }

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }
}
