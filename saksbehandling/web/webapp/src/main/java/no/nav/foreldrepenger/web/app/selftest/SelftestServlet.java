package no.nav.foreldrepenger.web.app.selftest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.foreldrepenger.web.app.selftest.checks.ExtHealthCheck;

@WebServlet("internal/selftest")
public class SelftestServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelftestServlet.class);

    private static final String RESPONSE_ENCODING = "UTF-8";

    @Inject //NOSONAR slik at servlet container får sin 0-arg ctor
    private transient Selftests selftests; //NOSONAR

    // for enhetstester
    void setSelftests(Selftests selftests) {
        this.selftests = selftests; //NOSONAR
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ObjectMapper mapper = (new ObjectMapper()).registerModule(new SelftestsJsonSerializerModule());
        SelftestsHtmlFormatter htmlFormatter = new SelftestsHtmlFormatter();

        resp.setCharacterEncoding(RESPONSE_ENCODING); //$NON-NLS-1$
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store"); // fra orig. HealthCheckServlet

        SelftestResultat samletResultat = selftests.run(); //NOSONAR

        for (HealthCheck.Result result : samletResultat.getKritiskeResultater()) {
            if (!result.isHealthy()) {
                SelftestFeil.FACTORY.kritiskSelftestFeilet(
                        getDetailValue(result, ExtHealthCheck.DETAIL_DESCRIPTION),
                        getDetailValue(result, ExtHealthCheck.DETAIL_ENDPOINT),
                        getDetailValue(result, ExtHealthCheck.DETAIL_RESPONSE_TIME),
                        result.getMessage()
                ).toException().log(LOGGER);
            }
        }
        for (HealthCheck.Result result : samletResultat.getIkkeKritiskeResultater()) {
            if (!result.isHealthy()) {
                SelftestFeil.FACTORY.ikkeKritiskSelftestFeilet(
                        getDetailValue(result, ExtHealthCheck.DETAIL_DESCRIPTION),
                        getDetailValue(result, ExtHealthCheck.DETAIL_ENDPOINT),
                        getDetailValue(result, ExtHealthCheck.DETAIL_RESPONSE_TIME),
                        result.getMessage()
                ).toException().log(LOGGER);
            }
        }

        boolean writeJson = MediaType.APPLICATION_JSON.equals(req.getContentType()); //NOSONAR

        try (ServletOutputStream outStream = resp.getOutputStream()) {
            if (writeJson) {
                resp.setContentType(MediaType.APPLICATION_JSON);
                ObjectWriter objectWriter = mapper.writer();
                objectWriter.writeValue(outStream, samletResultat);
            } else {
                resp.setContentType(MediaType.TEXT_HTML);
                boolean writeJsonAsHtml = "true".equalsIgnoreCase(req.getParameter("json")); //NOSONAR
                if (writeJsonAsHtml) {
                    ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
                    outStream.print("<pre>\n");
                    objectWriter.writeValue(outStream, samletResultat);
                    // dette stenger outStream, så dropp </pre>
                } else {
                    String html = htmlFormatter.format(samletResultat);
                    byte[] htmlEncoded = html.getBytes(Charset.forName(RESPONSE_ENCODING));
                    outStream.write(htmlEncoded);
                }
            }
        } catch (IOException e) {
            SelftestFeil.FACTORY.uventetSelftestFeil(e).log(LOGGER);
        }
    }

    private String getDetailValue(HealthCheck.Result resultat, String key) {
        Map<String, Object> details = resultat.getDetails();
        if (details != null) {
            return (String) details.get(key);
        } else {
            return null;
        }
    }
}
