package no.nav.foreldrepenger.web.app.selftest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("internal/isAlive")
public class IsAliveEndpoint extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsAliveEndpoint.class);

    private static final String RESPONSE_ENCODING = "UTF-8";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("ALIVE");
    }
}
