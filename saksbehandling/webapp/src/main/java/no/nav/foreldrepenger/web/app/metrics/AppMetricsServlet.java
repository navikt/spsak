package no.nav.foreldrepenger.web.app.metrics;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.servlets.MetricsServlet;

/**
 * Implementasjon som automatisk setter UTF-8 encoding for JSON resultat.
 */
@ApplicationScoped
public class AppMetricsServlet extends MetricsServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        super.doGet(req, resp);  // NOSONAR
    }
}
