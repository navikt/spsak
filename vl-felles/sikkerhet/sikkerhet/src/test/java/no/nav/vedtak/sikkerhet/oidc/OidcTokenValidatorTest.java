package no.nav.vedtak.sikkerhet.oidc;

import ch.qos.logback.classic.Level;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.sikkerhet.jwks.JwksKeyHandlerImpl;
import org.jose4j.jwt.NumericDate;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Base64;

import static java.util.Arrays.asList;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_HOST;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_ISSO_ISSUER;
import static no.nav.vedtak.isso.OpenAMHelper.OPEN_ID_CONNECT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

public class OidcTokenValidatorTest {

    @Rule
    public LogSniffer logSniffer = new LogSniffer(Level.DEBUG);
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OidcTokenValidator tokenValidator;

    public OidcTokenValidatorTest() {
        System.setProperty(OPEN_ID_CONNECT_ISSO_HOST, "https://foo.bar.adeo.no");
        System.setProperty(OPEN_ID_CONNECT_ISSO_ISSUER, "https://foo.bar.adeo.no/openam/oauth2");
        System.setProperty(OPEN_ID_CONNECT_USERNAME, "OIDC");

        tokenValidator = new OidcTokenValidator(new JwksKeyHandlerFromString(KeyStoreTool.getJwks()));
    }

    @Test
    public void skal_godta_token_som_har_forventede_verdier() throws Exception {
        String token = new OidcTokenGenerator().create();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertValid(result);
    }

    @Test
    public void skal_godta_token_som_har_forventede_verdier_og_i_tillegg_har_noen_ukjente_claims() throws Exception {
        String token = new OidcTokenGenerator()
                .withClaim("email", "foo@bar.nav.no")
                .create();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertValid(result);
    }

    @Test
    public void skal_ikke_godta_token_som_har_feil_issuer() throws Exception {
        //OpenID Connect Core 1.0 incorporating errata set 1
        //3.1.3.7 ID Token Validation
        //2 ..The Issuer Identifier for the OpenID  provider .. MUST exactly match the value of the iss (issuer) Claim.
        String token = new OidcTokenGenerator()
                .withIssuer("https://tull.nav.no")
                .create();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result, "rejected due to invalid claims. Additional details: [Issuer (iss) claim value (https://tull.nav.no) doesn't match expected value of https://foo.bar.adeo.no/openam/oauth2]");
    }

    @Test
    public void skal_godta_token_uansett_audience() throws Exception {
        //OpenID Connect Core 1.0 incorporating errata set 1
        //3.1.3.7 ID Token Validation
        //3 ..The ID token MUST be rejected if the ID Token does not list the Client as a valid audience ..

        //OpenID Connect Core 1.0 incorporating errata set 1
        //3.1.3.7 ID Token Validation
        //3 ..The ID token MUST be rejected if the ID Token ... , or if it contains additional audiences not trusted by the Client

        //Det gjøres unntak fra regelene over for å kunne bruke token på tvers i nav.
        //dette er OK siden nav er issuer og bare utsteder tokens til seg selv

        String token = new OidcTokenGenerator()
                .withAud(asList("noe"))
                .create();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertValid(result);
    }

    @Test
    public void skal_ikke_godta_at_azp_mangler_hvis_det_er_multiple_audiences_fordi_dette_trengs_for_å_senere_kunne_gjøre_refresh_av_token() throws Exception {
        //OpenID Connect Core 1.0 incorporating errata set 1
        //3.1.3.7 ID Token Validation
        //4 If the ID Token contains multiple audiences, the Client SHOULD verify that an azp Claim is present
        String token = new OidcTokenGenerator()
                .withoutAzp()
                .withAud(Arrays.asList("foo", "bar"))
                .create();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result, "Either an azp-claim or a single value aud-claim is required");
    }

    @Test
    public void skal_ikke_godta_at_azp_inneholder_noe_annet_enn_aktuelt_klientnavn() throws Exception {
        //OpenID Connect Core 1.0 incorporating errata set 1
        //3.1.3.7 ID Token Validation
        //5 If an azp (authorized party) Claim is present, the Client SHOULD verify that its client_id is the Claim Value

        //Det gjøres unntak fra regelene over for å kunne bruke token på tvers i nav.
        //dette er OK siden nav er issuer og bare utsteder tokens til seg selv

        String token = new OidcTokenGenerator()
                .withClaim("azp", "noe")
                .create();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertValid(result);
    }

    @Test
    public void skal_ikke_godta_token_som_er_signert_med_feil_sertifikat() throws Exception {
        //OpenID Connect Core 1.0 incorporating errata set 1
        //3.1.3.7 ID Token Validation
        //6 ... The Client MUST validate the signature of all other ID Tokens according to JWS using the algorithm specified in the JWT alg Header Parameter
        //      The Client MUST use the keys provided by the issurer

        String token = new OidcTokenGenerator().create();

        OidcTokenValidator tokenValidator = new OidcTokenValidator(new JwksKeyHandlerFromString("{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"1\",\"use\":\"sig\",\"alg\":\"RS256\",\"n\":\"AM2uHZfbHbDfkCTG8GaZO2zOBDmL4sQgNzCSFqlQ-ikAwTV5ptyAHYC3JEy_LtMcRSv3E7r0yCW_7WtzT-CgBYQilb_lz1JmED3TgiThEolN2kaciY06UGycSj8wEYik-3PxuVeKr3uw6LVEohM3rrCjdlkQ_jctuvuUrCedbsb2hVw6Q17PQbWURq8v3gtXmGMD8KcR7e0dtf0ZoMOfZQoFJZ-a5dMFzXeP8Ffz_c0uBLSddd-FqOhzVDiMbvFI9XKE22TWghYanPpPsGGZYioQbJfu5VtphR6zNjiUp9O4lA_qEkbBpRA8SaUTCz3PcirFYDg0zvV8p2hgY9jyCj0\",\"e\":\"AQAB\"}]}"));
        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result, "JWS signature is invalid");
    }

    @Test
    public void skal_ikke_godta_token_som_har_gått_ut_på_tid() throws Exception {
        //OpenID Connect Core 1.0 incorporating errata set 1
        //3.1.3.7 ID Token Validation
        //9 The current time MUST be before the time represented by the exp Claim
        long now = NumericDate.now().getValue();
        String token = new OidcTokenGenerator()
                .withIssuedAt(NumericDate.fromSeconds(now - 3601))
                .withExpiration(NumericDate.fromSeconds(now - 31)).create();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result, "is on or after the Expiration Time");
    }

    @Test
    public void skal_godta_token_som_har_gått_ut_på_tid_i_egen_metode_som_validerer_uten_tid() throws Exception {
        long now = NumericDate.now().getValue();
        String token = new OidcTokenGenerator()
                .withIssuedAt(NumericDate.fromSeconds(now - 3601))
                .withExpiration(NumericDate.fromSeconds(now - 31)).create();

        OidcTokenValidatorResult result = tokenValidator.validateWithoutExpirationTime(token);
        assertValid(result);
    }

    @Test
    public void skal_ikke_godta_å_validere_token_når_det_mangler_konfigurasjon_for_issuer() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Expected issuer must be configured.");

        System.clearProperty(OPEN_ID_CONNECT_ISSO_ISSUER);

        tokenValidator = new OidcTokenValidator(new JwksKeyHandlerFromString(KeyStoreTool.getJwks()));
    }

    @Test
    public void skal_ikke_godta_å_validere_token_når_det_mangler_konfigurasjon_for_audience() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Expected audience must be configured.");

        System.clearProperty(OPEN_ID_CONNECT_USERNAME);

        tokenValidator = new OidcTokenValidator(new JwksKeyHandlerFromString(KeyStoreTool.getJwks()));
    }

    @Test
    public void skal_ikke_godta_token_som_har_kid_som_ikke_finnes_i_jwks() throws Exception {
        String token = new OidcTokenGenerator()
                .withKid("124135g8e")
                .create();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result, "124135g8e", "is not in jwks");
    }

    @Test
    public void skal_ikke_godta_null() throws Exception {
        OidcTokenValidatorResult result = tokenValidator.validate(null);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Missing token (token was null)");
    }

    @Test
    public void skal_ikke_godta_noe_som_ikke_er_et_gyldig_JWT() throws Exception {
        OidcTokenValidatorResult result1 = tokenValidator.validate("");
        assertInvalid(result1,
                "Invalid OIDC Unable to process JOSE object",
                "Invalid JOSE Compact Serialization. Expecting either 3 or 5 parts for JWS or JWE respectively but was 1.)");

        OidcTokenValidatorResult result2 = tokenValidator.validate("tull");
        assertInvalid(result2,
                "Invalid OIDC Unable to process JOSE object",
                "Invalid JOSE Compact Serialization. Expecting either 3 or 5 parts for JWS or JWE respectively but was 1.)");

        OidcTokenValidatorResult result3 = tokenValidator.validate("a.b.c");
        assertInvalid(result3,
                "Invalid OIDC Unable to process JOSE object",
                "cause: org.jose4j.lang.JoseException: Parsing error: org.jose4j.json.internal.json_simple.parser.ParseException: Unexpected token END OF FILE at position 0.): a.b.c");

        String header = "{\"kid\":\"1\", \"alg\": \"RS256\""; //mangler } på slutten
        String claims = "{\"sub\":\"demo\"}";
        String h = Base64.getEncoder().encodeToString(header.getBytes()).replaceAll("=", "");
        String p = Base64.getEncoder().encodeToString(claims.getBytes()).replaceAll("=", "");
        OidcTokenValidatorResult result4 = tokenValidator.validate(h + "." + p + ".123");
        assertInvalid(result4, "Invalid OIDC Unable to process JOSE object");
    }

    @After
    public void cleanSystemProperties() {
        System.clearProperty(OPEN_ID_CONNECT_ISSO_HOST);
        System.clearProperty(OPEN_ID_CONNECT_ISSO_ISSUER);
        System.clearProperty(OPEN_ID_CONNECT_USERNAME);
    }

    private static class JwksKeyHandlerFromString extends JwksKeyHandlerImpl {
        private JwksKeyHandlerFromString(String jwks) {
            super(() -> jwks);
        }
    }

    private static void assertValid(OidcTokenValidatorResult result) {
        if (!result.isValid()) {
            throw new AssertionError("Forventet at token validerte, men fikk istedet feilmeldingen: " + result.getErrorMessage());
        }
    }

    private static void assertInvalid(OidcTokenValidatorResult result, String... forventet) {
        if (result.isValid()) {
            throw new AssertionError("Forventet at token feilet med feilmelding '" + asList(forventet) + "', men var OK");
        }

        for (String forventetDelAvMelding : forventet) {
            assertThat(result.getErrorMessage()).contains(forventetDelAvMelding);
        }
    }

}
