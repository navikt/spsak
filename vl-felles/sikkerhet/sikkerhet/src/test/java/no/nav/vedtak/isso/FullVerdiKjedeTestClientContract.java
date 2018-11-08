package no.nav.vedtak.isso;

import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_HOST;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import no.nav.modig.core.test.LogSniffer;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.tjeneste.virksomhet.virgo.v2.VirgoPortType;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.sikkerhet.context.SubjectHandlerUtils;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;
import no.nav.vedtak.sikkerhet.domene.IdTokenAndRefreshToken;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;
import no.nav.vedtak.sts.client.SecurityConstants;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

/** Kjør direkte ved behov. Plukkes ikke opp normalt i bygg. */
public class FullVerdiKjedeTestClientContract {

    private static final QName VIRGO_V_2 = new QName("http://nav.no/tjeneste/virksomhet/virgo/v2", "Virgo_v2");
    @Rule
    public LogSniffer logSniffer = new LogSniffer(Level.DEBUG);

    @BeforeClass
    public static void settPåTestsertifikater() {
        TestCertificates.setupKeyAndTrustStore();
    }

    @BeforeClass
    public static void settLoadbalancerUrl() {
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8080");
        ContextPathHolder.instance("/fpsak");
    }

    @AfterClass
    public static void clearLoadbalancerUrl() {
        System.clearProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL);
    }

    @Test
    public void innlogging_i_OpenAM_og_kall_til_Virgo_med_SAML_fra_STS() throws Exception {
        sjekkNødvendigeProperties();

        String username = "demo";
        oppsettAvPropertiesForOpenAM();
        oppsettAvPropertiesForSTS();
        SubjectHandlerUtils.useSubjectHandler(ThreadLocalSubjectHandler.class);

        // oppsett av WS-endpoint
        VirgoPortType port = createVirgoPort("https://service-gw-t11.test.local/");
        try (Client client = ClientProxy.getClient(port)) {
            StsConfigurationUtil.configureStsForOnBehalfOfWithOidc(client);
        }
        // Innlogging i OpenAM og henting av tokens
        OpenAMHelper helper = new OpenAMHelper();
        IdTokenAndRefreshToken tokens = helper.getToken(username, "changeit");

        assertThat(tokens.getIdToken()).isNotNull();
        assertThat(tokens.getRefreshToken()).isNotNull();

        // "Innlogging" i container, tilsvarende OidcAuthModule.validateRequest()
        Set<SluttBruker> pricipals = new HashSet<>();
        pricipals.add(new SluttBruker(username, IdentType.InternBruker));
        Set<Object> publicCredentials = new HashSet<>();
        publicCredentials.add(tokens.getIdToken());
        Set<?> privateCredentials = new HashSet<>();
        Subject subject = new Subject(false, pricipals, publicCredentials, privateCredentials);
        ((ThreadLocalSubjectHandler) ThreadLocalSubjectHandler.getSubjectHandler()).setSubject(subject);

        // Gjør WS-kall inkludert automagisk innveksling av OIDC-token til SAML-token
        assertThat(port.echo("heisann")).contains("heisann(fra: jboss v2) subject uid demo");

        logSniffer.assertNoErrors();
        logSniffer.clearLog(); // unngå stø i consollet
    }

    @Test
    public void henter_systemSAML_fra_STS_og_gjør_ping() throws Exception {
        sjekkNødvendigeProperties();

        oppsettAvPropertiesForSTS();

        // oppsett av WS-endpoint
        VirgoPortType port = createVirgoPort("https://service-gw-t11.test.local/");
        try (Client client = ClientProxy.getClient(port)) {
            StsConfigurationUtil.configureStsForSystemUser(client);
        }

        // Gjør WS-kall inkludert automagisk innveksling av OIDC-token til SAML-token
        assertThat(port.echo("heisann")).containsIgnoringCase("heisann(fra: jboss v2) subject uid srvengangsstonad");

        logSniffer.assertNoErrors();
        logSniffer.clearLog(); // unngå stø i consollet
    }

    private void oppsettAvPropertiesForOpenAM() {
        System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, "https://isso-t.adeo.no/isso/oauth2");
        System.setProperty(OPEN_ID_CONNECT_USERNAME, "fpsak-localhost");
    }

    private void oppsettAvPropertiesForSTS() {
        System.setProperty(SecurityConstants.STS_URL_KEY, "https://sts-t11.test.local:443/SecurityTokenServiceProvider/");
    }

    private void sjekkNødvendigeProperties() {
        if (isEmpty(OpenAMHelper.getIssoPassword()) || isEmpty(System.getProperty(SecurityConstants.SYSTEMUSER_PASSWORD))) {
            throw new IllegalStateException(OpenAMHelper.OPEN_ID_CONNECT_PASSWORD + " og " + SecurityConstants.SYSTEMUSER_PASSWORD +
                " må være konfigurert -D" + OpenAMHelper.OPEN_ID_CONNECT_PASSWORD + "=<passordet> -D" + SecurityConstants.SYSTEMUSER_PASSWORD
                + "=<passordet>\" " +
                "Disse finnes på følgende URL'er: https://fasit.adeo.no/resources/edit?11&resource=2234096 og https://fasit.adeo.no/resources/edit?8&resource=2246684&revision=2246704");
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private VirgoPortType createVirgoPort(String endpoint) {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL("wsdl/Virgo_v2.wsdl");
        factoryBean.setServiceName(VIRGO_V_2);
        factoryBean.setEndpointName(VIRGO_V_2);
        factoryBean.setServiceClass(VirgoPortType.class);
        factoryBean.setAddress(endpoint);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        return factoryBean.create(VirgoPortType.class);
    }

}