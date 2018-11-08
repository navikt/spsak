package no.nav.foreldrepenger.web.app.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

import javax.inject.Inject;

public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    @Inject
    private MetricRegistry metricRegistry;  // NOSONAR

    @Override
    protected MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
