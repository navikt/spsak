package no.nav.foreldrepenger.autotest.foreldrepenger.eksempler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.autotest.foreldrepenger.FpsakTestBase;
import no.nav.foreldrepenger.autotest.klienter.sparkel.FakeAccessTokenKlient;
import no.nav.foreldrepenger.autotest.klienter.sparkel.SparkelKlient;
import no.nav.foreldrepenger.fpmock2.server.api.scenario.TestscenarioDto;

public class SparkelEksempel extends FpsakTestBase {

    @Test
    public void hentArbeidsforholdViaSparkel() throws IOException {
        TestscenarioDto testscenario = opprettScenario("50");

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();
        final String fnr = testscenario.getPersonopplysninger().getSøkerIdent();
        System.out.println(aktørId);

        FakeAccessTokenKlient.TokenResponse resp = new FakeAccessTokenKlient().hentTokenForSubject("srvspa");
        System.out.println(resp);

        SparkelKlient sparkel = new SparkelKlient(resp.idToken);
        Object o = sparkel.hentArbeidsforhold(aktørId,
            Date.from(LocalDate.ofYearDay(2017,1).atStartOfDay().toInstant(ZoneOffset.UTC)),
            new Date());
        System.out.println(o);

        o = sparkel.hentInntektsliste(fnr);
        System.out.println(o);
    }

    @Test
    public void hentInntektslisteViaSparkel() throws IOException {
        TestscenarioDto testscenario = opprettScenario("50");

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();
        final String fnr = testscenario.getPersonopplysninger().getSøkerIdent();
        System.out.println(aktørId);

        FakeAccessTokenKlient.TokenResponse resp = new FakeAccessTokenKlient().hentTokenForSubject("srvspa");
        System.out.println(resp);

        SparkelKlient sparkel = new SparkelKlient(resp.idToken);

        Object o = sparkel.hentInntektsliste(fnr);
        System.out.println(o);
    }


    /*

    SPARKEL:
    VM-OPTIONS:
    -Djavax.net.ssl.trustStore=/Users/<heime>/spsak/truststore.jks -Djavax.net.ssl.trustStorePassword=changeit

    ENVironment variables:

JWKS_URL	https://localhost:8063/isso/oauth2/connect/jwk_uri
JWT_ISSUER	https://localhost:8063/isso/oauth2
AKTORREGISTER_URL	https://localhost:8063/aktoerrest
SECURITY_TOKEN_SERVICE_REST_URL	https://localhost:8063/stsrest
SECURITY_TOKEN_SERVICE_USERNAME	srvheisann
SECURITY_TOKEN_SERVICE_PASSWORD	tralalal
AAREG_ENDPOINTURL	https://localhost:8063/aareg-core/ArbeidsforholdService/v3
SECURITY_TOKEN_SERVICE_URL	https://localhost:8063/SecurityTokenServiceProvider/

     */

}
