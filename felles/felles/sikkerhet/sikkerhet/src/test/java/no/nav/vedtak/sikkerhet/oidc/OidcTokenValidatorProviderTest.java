package no.nav.vedtak.sikkerhet.oidc;

import ch.qos.logback.classic.Level;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_HOST;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_ISSUER;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_JWKS;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_PASSWORD;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_USERNAME;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.AGENT_NAME_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.ALT_ISSUER_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.ALT_JWKS_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.HOST_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.ISSUER_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.JWKS_URL_KEY;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.PROVIDERNAME_AAD_B2C;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.PROVIDERNAME_OPEN_AM;
import static no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider.PROVIDERNAME_STS;
import static org.assertj.core.api.Assertions.assertThat;

public class OidcTokenValidatorProviderTest {

    private static final String openam_iss = "https://openam.no";
    private static final String openam_agent = "openam";
    private static final String sts_iss = "https://sts.no";
    private static final String sts_agent = "sts";
    private static final String aad_b2c_iss = "https://aad.b2c.no";
    private static final String aad_b2c_agent = "aad.b2c";
    private static final String testhub_iss = "https://testhub.nav.no/supersecrettokenfacility";

    private static final String validUrl = "https://this.is.a.valid.url";
    private static final String invalidUrl = "this.is.a.invalid.url";
    private static String OPPRINNELIG_OPEN_ID_CONNECT_USERNAME;
    private static String OPPRINNELIG_OPEN_ID_CONNECT_PASSWORD;
    private static String OPPRINNELIG_OPEN_ID_CONNECT_ISSO_ISSUER;
    private static String OPPRINNELIG_OPEN_ID_CONNECT_ISSO_HOST;
    private static String OPPRINNELIG_OPEN_ID_CONNECT_ISSO_JWKS;

    @Rule
    public LogSniffer logSniffer = new LogSniffer(Level.DEBUG);
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void saveState() {
        OPPRINNELIG_OPEN_ID_CONNECT_USERNAME = OpenAMHelper.getIssoUserName();
        OPPRINNELIG_OPEN_ID_CONNECT_PASSWORD = OpenAMHelper.getIssoPassword();
        OPPRINNELIG_OPEN_ID_CONNECT_ISSO_ISSUER = OpenAMHelper.getIssoIssuerUrl();
        OPPRINNELIG_OPEN_ID_CONNECT_ISSO_HOST = OpenAMHelper.getIssoHostUrl();
        OPPRINNELIG_OPEN_ID_CONNECT_ISSO_JWKS = OpenAMHelper.getIssoJwksUrl();
    }

    @Before
    public void setUp() {
        System.setProperty(PROVIDERNAME_OPEN_AM + AGENT_NAME_KEY, openam_agent);
        System.setProperty(PROVIDERNAME_OPEN_AM + ISSUER_URL_KEY, openam_iss);
        System.setProperty(PROVIDERNAME_OPEN_AM + JWKS_URL_KEY, validUrl);
        System.setProperty(PROVIDERNAME_OPEN_AM + HOST_URL_KEY, validUrl);

        System.setProperty(PROVIDERNAME_STS + AGENT_NAME_KEY, sts_agent);
        System.setProperty(PROVIDERNAME_STS + ALT_ISSUER_URL_KEY, sts_iss);
        System.setProperty(PROVIDERNAME_STS + ALT_JWKS_URL_KEY, validUrl);
        System.setProperty(PROVIDERNAME_STS + HOST_URL_KEY, validUrl);

        System.setProperty(PROVIDERNAME_AAD_B2C + AGENT_NAME_KEY, aad_b2c_agent);
        System.setProperty(PROVIDERNAME_AAD_B2C + ISSUER_URL_KEY, aad_b2c_iss);
        System.setProperty(PROVIDERNAME_AAD_B2C + JWKS_URL_KEY, validUrl);
        System.setProperty(PROVIDERNAME_AAD_B2C + HOST_URL_KEY, validUrl);

        System.clearProperty("develop-local");
        System.clearProperty("environment.class");
        System.clearProperty("fasit.environment.name");

        OidcTokenValidatorProvider.clearInstance();
    }

    @AfterClass
    public static void cleanupState() {
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith(PROVIDERNAME_OPEN_AM) ||
                    key.startsWith(PROVIDERNAME_STS) ||
                    key.startsWith(PROVIDERNAME_AAD_B2C)) {
                System.clearProperty(key);
            }
        }
        if (OPPRINNELIG_OPEN_ID_CONNECT_USERNAME == null) {
            System.clearProperty(OPEN_ID_CONNECT_USERNAME);
        } else {
            System.setProperty(OPEN_ID_CONNECT_USERNAME, OPPRINNELIG_OPEN_ID_CONNECT_USERNAME);
        }
        if (OPPRINNELIG_OPEN_ID_CONNECT_PASSWORD == null) {
            System.clearProperty(OPEN_ID_CONNECT_PASSWORD);
        } else {
            System.setProperty(OPEN_ID_CONNECT_PASSWORD, OPPRINNELIG_OPEN_ID_CONNECT_PASSWORD);
        }
        if (OPPRINNELIG_OPEN_ID_CONNECT_ISSO_ISSUER == null) {
            System.clearProperty(OPEN_ID_CONNECT_ISSO_ISSUER);
        } else {
            System.setProperty(OPEN_ID_CONNECT_ISSO_ISSUER, OPPRINNELIG_OPEN_ID_CONNECT_ISSO_ISSUER);
        }
        if (OPPRINNELIG_OPEN_ID_CONNECT_ISSO_HOST == null) {
            System.clearProperty(OPEN_ID_CONNECT_ISSO_HOST);
        } else {
            System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, OPPRINNELIG_OPEN_ID_CONNECT_ISSO_HOST);
        }
        if (OPPRINNELIG_OPEN_ID_CONNECT_ISSO_JWKS == null) {
            System.clearProperty(OPEN_ID_CONNECT_ISSO_JWKS);
        } else {
            System.setProperty(OPEN_ID_CONNECT_ISSO_JWKS, OPPRINNELIG_OPEN_ID_CONNECT_ISSO_JWKS);
        }
    }

    @Test
    public void finner_alle_kjente_providere() {
        assertThat(OidcTokenValidatorProvider.instance().getValidator(openam_iss)).isNotNull();
        assertThat(OidcTokenValidatorProvider.instance().getValidator(sts_iss)).isNotNull();
        assertThat(OidcTokenValidatorProvider.instance().getValidator(aad_b2c_iss)).isNotNull();
    }

    @Test
    public void finner_OpenAm_provider_konfigurert_for_OpenAMHelper() {
        cleanupState();
        System.setProperty(OPEN_ID_CONNECT_USERNAME, openam_agent);
        System.setProperty(OPEN_ID_CONNECT_PASSWORD, "aPassword");
        System.setProperty(OPEN_ID_CONNECT_ISSO_ISSUER, openam_iss);
        System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, validUrl);
        System.setProperty(OPEN_ID_CONNECT_ISSO_JWKS, validUrl);

        assertThat(OidcTokenValidatorProvider.instance().getValidator(openam_iss)).isNotNull();
    }

    @Test
    public void logger_manglende_konfigurasjon_for_alle_kjente_providere() throws Exception {
        cleanupState();
        System.clearProperty(OPEN_ID_CONNECT_USERNAME);
        assertThat(OidcTokenValidatorProvider.instance().getValidator("https://foo.no.such.domain/")).isNull();
        logSniffer.assertHasInfoMessage("Fant ingen OIDC konfigurasjon for '" + PROVIDERNAME_OPEN_AM);
        logSniffer.assertHasInfoMessage("Fant ingen OIDC konfigurasjon for '" + PROVIDERNAME_STS);
        logSniffer.assertHasInfoMessage("Fant ingen OIDC konfigurasjon for '" + PROVIDERNAME_AAD_B2C);
    }

    @Test
    public void feilkonfigurert_issuer_urler_gir_feil() {
        System.setProperty(PROVIDERNAME_OPEN_AM + ISSUER_URL_KEY, invalidUrl);
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("Syntaksfeil i OIDC konfigurasjonen av 'issuer' for '" + PROVIDERNAME_OPEN_AM);

        OidcTokenValidatorProvider.instance();
    }

    @Test
    public void feilkonfigurert_jwks_urler_gir_feil() {
        System.setProperty(PROVIDERNAME_OPEN_AM + JWKS_URL_KEY, invalidUrl);
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("Syntaksfeil i OIDC konfigurasjonen av 'jwks' for '" + PROVIDERNAME_OPEN_AM);

        OidcTokenValidatorProvider.instance();
    }

    @Test
    public void feilkonfigurert_host_urler_gir_feil() {
        System.setProperty(PROVIDERNAME_OPEN_AM + HOST_URL_KEY, invalidUrl);
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("Syntaksfeil i OIDC konfigurasjonen av 'host' for '" + PROVIDERNAME_OPEN_AM);

        OidcTokenValidatorProvider.instance();
    }

    @Test
    public void interne_providere_skal_IKKE_validere_aud() {
        Set<OpenIDProviderConfig> configs = new OidcTokenValidatorProvider.OpenIDProviderConfigProvider().getConfigs();
        Set<OpenIDProviderConfig> configForInternBruker = configs.stream().filter(config -> config.getIdentTyper().contains(IdentType.InternBruker)).collect(Collectors.toSet());
        assertThat(configForInternBruker).allSatisfy(config -> config.isSkipAudienceValidation());
    }

    @Test
    public void eksterne_providere_skal_validere_aud() {
        Set<OpenIDProviderConfig> configs = new OidcTokenValidatorProvider.OpenIDProviderConfigProvider().getConfigs();
        Set<OpenIDProviderConfig> configForEksternBruker = configs.stream().filter(config -> config.getIdentTyper().contains(IdentType.EksternBruker)).collect(Collectors.toSet());

        assertThat(configForEksternBruker).allSatisfy(config ->
                assertThat(config.isSkipAudienceValidation()).isFalse());
    }

    @Test
    public void providere_skal_støtte_enten_InternBruker_eller_EksternBruker() {
        Set<OpenIDProviderConfig> configs = new OidcTokenValidatorProvider.OpenIDProviderConfigProvider().getConfigs();
        assertThat(configs.stream()
                .filter(config -> !config.getIdentTyper().contains(IdentType.InternBruker) && !config.getIdentTyper().contains(IdentType.EksternBruker))
                .collect(Collectors.toSet()))
                .isEmpty();
    }

    @Test
    public void finner_ikke_TestHub_provider() {
        assertThat(OidcTokenValidatorProvider.instance().getValidator(testhub_iss)).isNull();
    }

    @Test
    public void finner_TestHub_provider_ved_lokal_utvikling() {
        System.setProperty("develop-local", "true");
        assertThat(OidcTokenValidatorProvider.instance().getValidator(testhub_iss)).isNotNull();
    }

    @Test
    public void finner_TestHub_provider_i_skya_t() {
        System.setProperty("environment.class", "t");
        assertThat(OidcTokenValidatorProvider.instance().getValidator(testhub_iss)).isNotNull();
    }

    @Test
    public void finner_TestHub_provider_i_skya_q() {
        System.setProperty("environment.class", "q");
        assertThat(OidcTokenValidatorProvider.instance().getValidator(testhub_iss)).isNotNull();
    }

    @Test
    public void finner_ikke_TestHub_provider_i_skya_p() {
        System.setProperty("environment.class", "p");
        assertThat(OidcTokenValidatorProvider.instance().getValidator(testhub_iss)).isNull();
    }

    @Test
    public void finner_TestHub_provider_i_nais_t() {
        System.setProperty("fasit.environment.name", "t10");
        assertThat(OidcTokenValidatorProvider.instance().getValidator(testhub_iss)).isNotNull();
    }

    @Test
    public void finner_TestHub_provider_i_nais_q() {
        System.setProperty("fasit.environment.name", "q10");
        assertThat(OidcTokenValidatorProvider.instance().getValidator(testhub_iss)).isNotNull();
    }

    @Test
    public void finner_ikke_TestHub_provider_i_nais_p() {
        System.setProperty("fasit.environment.name", "p");
        assertThat(OidcTokenValidatorProvider.instance().getValidator(testhub_iss)).isNull();
    }
}