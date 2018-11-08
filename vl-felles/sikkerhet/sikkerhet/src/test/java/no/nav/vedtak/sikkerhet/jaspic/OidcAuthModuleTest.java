package no.nav.vedtak.sikkerhet.jaspic;

import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_HOST;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_ISSUER;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_USERNAME;
import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.nav.vedtak.sikkerhet.loginmodule.LoginContextConfiguration;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenGenerator;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProviderForTest;
import org.jose4j.jwt.NumericDate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.isso.config.ServerInfoTestUtil;
import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.sikkerhet.context.StaticSubjectHandler;
import no.nav.vedtak.sikkerhet.context.SubjectHandlerUtils;
import no.nav.vedtak.sikkerhet.oidc.IdTokenProvider;
import no.nav.vedtak.sikkerhet.oidc.OidcLogin;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidator;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorResult;
import no.nav.vedtak.sts.client.SecurityConstants;

public class OidcAuthModuleTest {

    @Rule
    public LogSniffer logSniffer = new LogSniffer();

    private OidcTokenValidator tokenValidator = Mockito.mock(OidcTokenValidator.class);
    private IdTokenProvider idTokenProvider = Mockito.mock(IdTokenProvider.class);
    private TokenLocator tokenLocator = Mockito.mock(TokenLocator.class);
    private CallbackHandler callbackHandler = Mockito.mock(CallbackHandler.class);
    private final Configuration configuration = new LoginContextConfiguration();

    private WSS4JProtectedServlet wsServlet = new WSS4JProtectedServletTestImpl();
    private OidcAuthModule authModule = new OidcAuthModule(idTokenProvider, tokenLocator, configuration, wsServlet);
    private HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

    private Subject subject = new Subject();
    private Subject serviceSubject = new Subject();

    public OidcAuthModuleTest() throws Exception {
        when(request.getRequestURI()).thenReturn("/fpsak");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://foo.devillo.no/fpsak/"));
        when(request.getHeader("Accept")).thenReturn("application/json");
        authModule.initialize(null, null, callbackHandler, null);

        System.setProperty(OPEN_ID_CONNECT_USERNAME, "OIDC");
        System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, "https://bar.devillo.no/isso/oauth2");
        System.setProperty(SecurityConstants.SYSTEMUSER_USERNAME, "JUnit Test");
        System.setProperty(OPEN_ID_CONNECT_ISSO_ISSUER, OidcTokenGenerator.ISSUER);

        Map<String, OidcTokenValidator> map = new HashMap<>();
        map.put(OidcTokenGenerator.ISSUER, tokenValidator);
        OidcTokenValidatorProviderForTest.setValidators(map);
    }

    @BeforeClass
    public static void classSetup() {
        ServerInfoTestUtil.clearServerInfoInstance();
        SubjectHandlerUtils.useSubjectHandler(StaticSubjectHandler.class);
        System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "https://foo.devillo.no");
        ContextPathHolder.instance("/fpsak");
    }

    @AfterClass
    public static void classTeardown() {
        SubjectHandlerUtils.unsetSubjectHandler();
        System.clearProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL);
    }

    @Test
    public void skal_slippe_gjennom_forespørsel_etter_ubeskyttet_ressurs() throws Exception {
        MessageInfo request = createRequestForUnprotectedResource();

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SUCCESS);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_men_svare_med_401_etter_beskyttet_ressurs_når_forespørselen_ikke_har_med_id_token()
            throws Exception {
        when(request.getHeader("Accept")).thenReturn("application/json");
        MessageInfo request = createRequestForProtectedResource();

        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_FAILURE);
        verify(response).sendError(401, "Resource is protected, but id token is missing or invalid.");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void skal_sende_401_for_ugyldig_Authorization_header()
            throws Exception {
        String utløptIdToken = getUtløptToken();

        when(request.getHeader("Accept")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer "+utløptIdToken);
        MessageInfo request = createRequestForProtectedResource();

        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(utløptIdToken));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_FAILURE);
        verify(response).sendError(401, "Resource is protected, but id token is missing or invalid.");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_men_svare_med_redirect_til_openam_etter_beskyttet_ressurs_når_forespørselen_ikke_har_med_id_token()
            throws Exception {
        when(request.getHeader("Accept")).thenReturn("*/*");
        MessageInfo request = createRequestForProtectedResource();

        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_FAILURE);

        CookieCollector cookieCollector = new CookieCollector();
        verify(response).addCookie(Mockito.argThat(cookieCollector));

        // hvor redirect skal til slutt, legges i en cookie på browseren. Dette gjør at dersom noen
        // legger sensitiv informajon i parametre eller lignende, vil det ikke sendes til OpenAM
        Cookie stateCookie = cookieCollector.getCookieWhereNameMatches("state_.*");
        assertThat(stateCookie.getValue()).isEqualTo("https%3A%2F%2Ffoo.devillo.no%2Ffpsak%2F");
        assertThat(stateCookie.getDomain()).isNull(); // bare denne serveren
        assertThat(stateCookie.getPath()).isEqualTo("/fpsak/cb"); // bare nødvendig å sende til callback
        assertThat(stateCookie.isHttpOnly()).isTrue();
        assertThat(stateCookie.getSecure()).isTrue();
        String stateCookieName = stateCookie.getName(); // name er tilfeldig valgt, men starter med state_
        verify(response).sendRedirect(
                "https://bar.devillo.no/isso/oauth2/authorize?session=winssochain&authIndexType=service&authIndexValue=winssochain&response_type=code&scope=openid&client_id=OIDC&state="
                        + stateCookieName + "&redirect_uri=https%3A%2F%2Ffoo.devillo.no%2Ffpsak%2Fcb");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void skal_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_id_token_som_validerer()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        String gyldigIdToken = getGyldigToken();
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigIdToken));
        when(tokenLocator.getRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());
        when(tokenValidator.validate(gyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 + 121));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SUCCESS);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_et_utløpt_id_token_og_ikke_noe_refresh_token()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        String ugyldigToken = getUtløptToken();
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(ugyldigToken));
        when(tokenLocator.getRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());
        when(tokenValidator.validate(ugyldigToken))
                .thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validateWithoutExpirationTime(ugyldigToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 - 10));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_FAILURE);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Resource is protected, but id token is missing or invalid.");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_og_svare_med_redirect_når_det_ikke_er_satt_application_json() throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        String ugyldigToken = getUtløptToken();
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(ugyldigToken));
        when(tokenLocator.getRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());
        when(tokenValidator.validate(ugyldigToken))
                .thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validateWithoutExpirationTime(ugyldigToken))
                .thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_FAILURE);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_et_utløpt_id_token_og_ikke_klarer_å_hente_nytt_token_med_refresh_token()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        String ugyldigIdToken = getUtløptToken();
        String ugyldigRefreshToken = "et ugyldig refresh token";
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(ugyldigIdToken));
        when(tokenLocator.getRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of(ugyldigRefreshToken));
        when(tokenValidator.validate(ugyldigIdToken)).thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validate(ugyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 - 10));
        when(idTokenProvider.getToken(ugyldigIdToken, ugyldigRefreshToken)).thenReturn(Optional.empty());

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_FAILURE);
        verify(idTokenProvider).getToken(ugyldigIdToken, ugyldigRefreshToken);
    }

    @Test
    public void skal_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_et_utløpt_id_token_og_klarer_å_hente_gyldig_id_token_vha_refresh_token()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        String utløptIdToken = getUtløptToken();
        String gyldigRefreshToken = "et gyldig refresh token";
        String gyldigIdToken = getGyldigToken();
        int sekunderGjenståendeGyldigTid = Integer.parseInt(OidcLogin.DEFAULT_REFRESH_TIME) + 60;

        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(utløptIdToken));
        when(tokenLocator.getRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigRefreshToken));
        when(tokenValidator.validate(utløptIdToken)).thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validate(utløptIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 - 10));
        when(tokenValidator.validate(gyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));

        when(idTokenProvider.getToken(utløptIdToken, gyldigRefreshToken)).thenReturn(Optional.of(gyldigIdToken));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SUCCESS);
        verify(idTokenProvider).getToken(utløptIdToken, gyldigRefreshToken);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_et_ugyldigid_token_og_klarer_å_hente_gyldig_id_token_vha_refresh_token()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        String ugyldig = getUtløptToken();
        String gyldigRefreshToken = "et gyldig refresh token";
        String gyldigIdToken = getGyldigToken();

        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(ugyldig));
        when(tokenLocator.getRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigRefreshToken));
        when(tokenValidator.validate(ugyldig)).thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validateWithoutExpirationTime(ugyldig)).thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validate(gyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 + 60));

        when(idTokenProvider.getToken(ugyldig, gyldigRefreshToken)).thenReturn(Optional.of(gyldigIdToken));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_FAILURE);
    }

    @Test
    public void skal_sette_nytt_idtoken_i_httponly_cookie_når_det_ble_gjort_refresh_av_token() throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        String utløptIdToken = getUtløptToken();
        String nyttGyldigIdToken = getGyldigToken();
        int sekunderGjenståendeGyldigTid = Integer.parseInt(OidcLogin.DEFAULT_REFRESH_TIME) + 60;
        String gyldigRefreshToken = "et gyldig refresh token";
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(utløptIdToken));
        when(tokenLocator.getRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigRefreshToken));
        when(tokenValidator.validate(utløptIdToken)).thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validate(utløptIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 - 60));
        when(tokenValidator.validate(nyttGyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));
        when(idTokenProvider.getToken(utløptIdToken, gyldigRefreshToken)).thenReturn(Optional.of(nyttGyldigIdToken));

        authModule.validateRequest(request, subject, serviceSubject);

        Cookie forventetCookie = new Cookie(ID_TOKEN_COOKIE_NAME, nyttGyldigIdToken);
        forventetCookie.setSecure(true);
        forventetCookie.setHttpOnly(true);
        forventetCookie.setPath("/");
        forventetCookie.setDomain("devillo.no");
        verify(response).addCookie(Mockito.argThat(new CookieMatcher(forventetCookie)));
    }

    @Test
    public void skal_ikke_gjøre_refresh_av_token_når_det_validerer_og_har_tilstrekkelig_levetid_til_å_brukes_til_kall_til_andre_tjenester()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        int sekunderGjenståendeGyldigTid = Integer.parseInt(OidcLogin.DEFAULT_REFRESH_TIME) + 5;

        String gyldigIdToken = getGyldigToken();
        String gyldigRefreshToken = "et gyldig refresh token";
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigIdToken));
        when(tokenLocator.getRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigRefreshToken));
        when(tokenValidator.validate(gyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));
        when(idTokenProvider.getToken(gyldigIdToken, gyldigRefreshToken)).thenReturn(Optional.of("nok et token som validerer :-)"));

        authModule.validateRequest(request, subject, serviceSubject);

        Mockito.verifyZeroInteractions(idTokenProvider); // skal ikke hente refresh-token
        Mockito.verifyZeroInteractions(response); // skal ikke sette cookie
    }

    @Test
    public void skal_gjøre_refresh_av_token_når_det_validerer_og_har_for_kort_levetid_til_å_sikkert_kunne_brukes_til_kall_til_andre_tjenester()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        int sekunderGjenståendeGyldigTid = Integer.parseInt(OidcLogin.DEFAULT_REFRESH_TIME) - 5;

        String gyldigIdToken = getGyldigToken();
        String nyttGyldigIdToken = getGyldigToken();
        String gyldigRefreshToken = "et gyldig refresh token";
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigIdToken));
        when(tokenLocator.getRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigRefreshToken));
        when(tokenValidator.validate(gyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));
        when(tokenValidator.validate(nyttGyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", System.currentTimeMillis() / 1000 + 3600));
        when(idTokenProvider.getToken(gyldigIdToken, gyldigRefreshToken)).thenReturn(Optional.of(nyttGyldigIdToken));

        authModule.validateRequest(request, subject, serviceSubject);

        verify(idTokenProvider).getToken(gyldigIdToken, gyldigRefreshToken);

        Cookie forventetCookie = new Cookie(ID_TOKEN_COOKIE_NAME, nyttGyldigIdToken);
        forventetCookie.setSecure(true);
        forventetCookie.setHttpOnly(true);
        forventetCookie.setPath("/");
        forventetCookie.setDomain("devillo.no");
        verify(response).addCookie(Mockito.argThat(new CookieMatcher(forventetCookie)));
    }


    @Test
    public void skal_ha_korrekt_scheme_selv_om_TLS_termineres_underveis() throws Exception {
        ServerInfoTestUtil.clearServerInfoInstance();

        when(request.getRequestURL()).thenReturn(new StringBuffer("http://foo.devillo.no/fpsak/"));
        String originalUrl = authModule.getOriginalUrl(request);
        assertThat(originalUrl).startsWith("https");
    }

    static class CookieMatcher implements ArgumentMatcher<Cookie> {

        private Cookie expected;

        CookieMatcher(Cookie expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Cookie item) {
            return check(item, Cookie::getName) &
                    check(item, Cookie::getValue) &
                    check(item, Cookie::getDomain) &
                    check(item, Cookie::getPath) &
                    check(item, Cookie::getMaxAge) &
                    check(item, Cookie::getSecure) &
                    check(item, Cookie::isHttpOnly) &
                    check(item, Cookie::getVersion) &
                    check(item, Cookie::getComment);
        }

        private <T> boolean check(Cookie item, Function<Cookie, T> what) {
            return Objects.equals(what.apply(expected), what.apply(item));
        }

    }

    static class CookieCollector implements ArgumentMatcher<Cookie> {

        private Map<String, Cookie> cookies = new HashMap<>();

        @Override
        public boolean matches(Cookie item) {
            cookies.put(item.getName(), item);
            return true;
        }

        public Cookie getCookieWhereNameMatches(String regexp) {
            List<Cookie> matches = new ArrayList<>();
            for (String name : cookies.keySet()) {
                if (name.matches(regexp)) {
                    matches.add(cookies.get(name));
                }
            }
            switch (matches.size()) {
                case 0:
                    return null;
                case 1:
                    return matches.get(0);
                default:
                    throw new IllegalArgumentException("Multiple cookies matched " + regexp);
            }
        }
    }

    private MessageInfo createRequestForProtectedResource() {
        return createRequestForResource(true);
    }

    private MessageInfo createRequestForUnprotectedResource() {
        return createRequestForResource(false);
    }

    private MessageInfo createRequestForResource(boolean isProtected) {
        MessageInfo messageInfo = Mockito.mock(MessageInfo.class);
        Map<Object, Object> properties = new HashMap<Object, Object>() {
            {
                put("javax.security.auth.message.MessagePolicy.isMandatory", Boolean.toString(isProtected));
            }
        };
        when(messageInfo.getMap()).thenReturn(properties);
        when(messageInfo.getRequestMessage()).thenReturn(request);
        when(messageInfo.getResponseMessage()).thenReturn(response);
        return messageInfo;
    }

    private String getGyldigToken() {
        return new OidcTokenGenerator().create();

    }

    private String getUtløptToken() {
        return new OidcTokenGenerator().withExpiration(NumericDate.fromMilliseconds(System.currentTimeMillis()-1)).create();
    }


    @WebServlet(urlPatterns = { "/tjenester", "/tjenester/", "/tjenester/*" }, loadOnStartup = 1)
    private class WSS4JProtectedServletTestImpl implements WSS4JProtectedServlet {

        @Override
        public boolean isProtectedWithAction(String pathInfo, String requiredAction) {
            return true;
        }
    }
}