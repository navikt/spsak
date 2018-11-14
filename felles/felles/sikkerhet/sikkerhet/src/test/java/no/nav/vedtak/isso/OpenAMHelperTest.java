package no.nav.vedtak.isso;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.sikkerhet.domene.IdTokenAndRefreshToken;
import no.nav.vedtak.sts.client.SecurityConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_HOST;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_PASSWORD;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class OpenAMHelperTest {

    @Rule
    public LogSniffer logSniffer = new LogSniffer(Level.DEBUG);
    private OpenAMHelper helper;

    private String rpUsername;
    private String rpPassword;
    private String systembrukerUsername;
    private String systembrukerPassword;

    private static Level ORG_HTTP_CLIENT_LOG_LEVEL;
    private static Logger HTTP_CLIENT_LOGGER;

    private static void setProperty(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        } else {
            System.clearProperty(key);
        }
    }

    @BeforeClass
    public static void ensureFrameworkLogging(){
        HTTP_CLIENT_LOGGER = (Logger)LoggerFactory.getLogger("org.apache.http.client");
        ORG_HTTP_CLIENT_LOG_LEVEL = HTTP_CLIENT_LOGGER.getLevel();
        HTTP_CLIENT_LOGGER.setLevel(Level.WARN);
    }

    @Before
    public void setUp() {
        logSniffer.clearLog();

        System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, "https://isso-t.adeo.no/isso/oauth2");
        System.setProperty(OPEN_ID_CONNECT_USERNAME, "fpsak-localhost");
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8080");

        backupSystemProperties();
        ContextPathHolder.instance("/fpsak");
        helper = new OpenAMHelper();
    }

    @AfterClass
    public static void tearDownClass() {
        System.clearProperty(OPEN_ID_CONNECT_USERNAME);
        System.clearProperty(OPEN_ID_CONNECT_PASSWORD);
        System.clearProperty(SecurityConstants.SYSTEMUSER_USERNAME);
        System.clearProperty(SecurityConstants.SYSTEMUSER_PASSWORD);
        System.clearProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL);
        HTTP_CLIENT_LOGGER.setLevel(ORG_HTTP_CLIENT_LOG_LEVEL);
    }

    @After
    public void tearDown() {
        restoreSystemProperties();
    }

    @Test(expected = IllegalArgumentException.class)
    public void skalFeileVedManglendeProperties() throws Exception {
        System.setProperty(SecurityConstants.SYSTEMUSER_USERNAME, "");
        helper.getToken();
    }

    @Test
    public void skalReturnereGyldigTokenVedGyldigBrukernavnOgPassord() throws Exception {
        ignoreTestHvisPropertiesIkkeErsatt();

        IdTokenAndRefreshToken tokens = helper.getToken();

        assertThat(tokens.getIdToken()).isNotNull();
        assertThat(tokens.getRefreshToken()).isNotNull();
        logSniffer.assertHasWarnMessage("Cookie rejected");
        int entries = logSniffer.countEntries("F-050157:Uventet format for host");
        if(entries > 0) { //HACK (u139158): ServerInfo.cookieDomain beregnes kun en gang så når man kjører alle testene i modulen blir denne spist tidligere
            logSniffer.assertHasWarnMessage("F-050157:Uventet format for host");
        }
    }

    @Test
    public void skalFeilePåTokenVedFeilIRPUserMenGyldigBrukernavnOgPassord() throws Exception {
        ignoreTestHvisPropertiesIkkeErsatt();
        System.setProperty(OPEN_ID_CONNECT_USERNAME, ""); // Settes til ugyldig verdi slik at det vil feile på access_token.
        try {
            helper.getToken();
        } catch (TekniskException e) {
            assertThat(e.getMessage()).isEqualTo("F-909480:Fant ikke auth-code på responsen, får respons: '400 - Bad Request'");
            logSniffer.assertHasWarnMessage("Cookie rejected");
        }
    }

    @Test
    public void skalFåHTTP401FraOpenAM_VedUgyldigBrukernavnOgEllerPassord() throws Exception {
    	ignoreTestHvisPropertiesIkkeErsatt();
        try {
            helper.getToken("NA", "NA");
        } catch (TekniskException e) {
            assertThat(e.getMessage()).startsWith("F-011609:Ikke-forventet respons fra OpenAm, statusCode 401");
            logSniffer.assertHasWarnMessage("Cookie rejected");
        }
    }

    private void ignoreTestHvisPropertiesIkkeErsatt() {
        assumeTrue("Systembruker: Brukernavn må være satt som VM-property", erSatt(SecurityConstants.SYSTEMUSER_USERNAME));
        assumeTrue("Systembruker: Passord må være satt som VM-property", erSatt(SecurityConstants.SYSTEMUSER_PASSWORD));
        assumeTrue("RP-bruker: Brukernavn må være satt som VM-property", erSatt(OPEN_ID_CONNECT_USERNAME));
        assumeTrue("RP-bruker: Passord må være satt som VM-property", erSatt(OPEN_ID_CONNECT_PASSWORD));
    }

    private boolean erSatt(String key) {
        String property = System.getProperty(key);
        return property != null && !property.isEmpty();
    }

    private void backupSystemProperties() {
        rpUsername = System.getProperty(OPEN_ID_CONNECT_USERNAME);
        rpPassword = System.getProperty(OPEN_ID_CONNECT_PASSWORD);
        systembrukerUsername = System.getProperty(SecurityConstants.SYSTEMUSER_USERNAME);
        systembrukerPassword = System.getProperty(SecurityConstants.SYSTEMUSER_PASSWORD);
    }

    private void restoreSystemProperties() {
        setProperty(OPEN_ID_CONNECT_USERNAME, rpUsername);
        setProperty(OPEN_ID_CONNECT_PASSWORD, rpPassword);
        setProperty(SecurityConstants.SYSTEMUSER_USERNAME, systembrukerUsername);
        setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, systembrukerPassword);
    }
}
