package no.nav.foreldrepenger.web.app.selftest;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.codahale.metrics.health.HealthCheck;

import no.nav.modig.core.test.LogSniffer;

public class SelftestServletTest {

    private SelftestServlet servlet; // objektet vi tester

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private Selftests mockSelftests;
    private ServletOutputStream mockServletOutputStream;

    private static final String MSG_KRITISK_FEIL = "kritisk feil";
    private static final String MSG_IKKEKRITISK_FEIL = "ikke-kritisk feil";

    @Rule
    public final LogSniffer logSniffer = new LogSniffer();

    @Before
    public void setup() throws IOException {
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getContentType()).thenReturn(MediaType.APPLICATION_JSON);

        mockServletOutputStream = mock(ServletOutputStream.class);
        mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getOutputStream()).thenReturn(mockServletOutputStream);

        mockSelftests = mock(Selftests.class);

        servlet = new SelftestServlet();
        servlet.setSelftests(mockSelftests);
    }

    @Test
    public void test_doGet_alleDeltesterOk() throws ServletException, IOException {
        SelftestResultat resultat = lagSelftestResultat(true, true);
        when(mockSelftests.run()).thenReturn(resultat);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockServletOutputStream, atLeast(1)).close();
        logSniffer.assertNoErrors();
        logSniffer.assertNoWarnings();
    }

    @Test
    public void test_doGet_kritiskeDeltesterOkIkkeKritiskeDeltesterFeil() throws ServletException, IOException {
        SelftestResultat resultat = lagSelftestResultat(true, false);
        when(mockSelftests.run()).thenReturn(resultat);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockServletOutputStream, atLeast(1)).close();
        logSniffer.assertNoErrors();
        logSniffer.assertHasWarnMessage(MSG_IKKEKRITISK_FEIL);
    }

    @Test
    public void test_doGet_kritiskeDeltesterFeilIkkeKritiskeDeltesterOk() throws ServletException, IOException {
        SelftestResultat resultat = lagSelftestResultat(false, true);
        when(mockSelftests.run()).thenReturn(resultat);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockServletOutputStream, atLeast(1)).close();
        logSniffer.assertHasErrorMessage(MSG_KRITISK_FEIL);
        logSniffer.assertNoWarnings();
    }

    @Test
    public void test_doGet_kritiskeDeltesterFeilIkkeKritiskeDeltesterFeil() throws ServletException, IOException {
        SelftestResultat resultat = lagSelftestResultat(false, false);
        when(mockSelftests.run()).thenReturn(resultat);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockServletOutputStream, atLeast(1)).close();
        logSniffer.assertHasErrorMessage(MSG_KRITISK_FEIL);
        logSniffer.assertHasWarnMessage(MSG_IKKEKRITISK_FEIL);
    }

    @Test
    public void test_doGet_html() throws ServletException, IOException {
        when(mockRequest.getContentType()).thenReturn(MediaType.TEXT_HTML);
        SelftestResultat resultat = lagSelftestResultat(true, true);
        when(mockSelftests.run()).thenReturn(resultat);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockServletOutputStream, atLeast(1)).close();
    }

    @Test
    public void test_doGet_jsonAsHtml() throws ServletException, IOException {
        when(mockRequest.getContentType()).thenReturn(MediaType.TEXT_HTML);
        when(mockRequest.getParameter("json")).thenReturn("true");
        SelftestResultat resultat = lagSelftestResultat(true, true);
        when(mockSelftests.run()).thenReturn(resultat);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockServletOutputStream, atLeast(1)).close();
    }

    @Test
    public void test_doGet_exceptionWritingResponse() throws ServletException, IOException {
        when(mockRequest.getContentType()).thenReturn(MediaType.TEXT_HTML);
        doThrow(new IOException("bad io")).when(mockServletOutputStream).close();
        SelftestResultat resultat = lagSelftestResultat(true, true);
        when(mockSelftests.run()).thenReturn(resultat);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockServletOutputStream, atLeast(1)).close();
        logSniffer.assertHasErrorMessage("FP-409676");
    }

    //-------

    private SelftestResultat lagSelftestResultat(boolean kritiskeOk, boolean ikkeKritiskeOk) {
        SelftestResultat resultat = lagSelftestResultat();

        HealthCheck.Result delRes1 = kritiskeOk ? HealthCheck.Result.healthy() : HealthCheck.Result.unhealthy(MSG_KRITISK_FEIL);
        resultat.leggTilResultatForKritiskTjeneste(delRes1);

        HealthCheck.Result delRes2 = ikkeKritiskeOk ? HealthCheck.Result.healthy() : HealthCheck.Result.unhealthy(MSG_IKKEKRITISK_FEIL);
        resultat.leggTilResultatForIkkeKritiskTjeneste(delRes2);

        return resultat;
    }

    private SelftestResultat lagSelftestResultat() {
        SelftestResultat resultat = new SelftestResultat();
        resultat.setApplication("test-appl");
        resultat.setRevision("1");
        resultat.setVersion("2");
        resultat.setBuildTime("nu");
        resultat.setTimestamp(LocalDateTime.now());
        return resultat;
    }
}
