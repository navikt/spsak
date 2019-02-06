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

        Object o = lagSparkelKlient().hentArbeidsforhold(aktørId,
            Date.from(LocalDate.ofYearDay(2017,1).atStartOfDay().toInstant(ZoneOffset.UTC)),
            new Date());
        System.out.println(o);
    }

    @Test
    public void hentInntektslisteViaSparkel() throws IOException {
        TestscenarioDto testscenario = opprettScenario("50");

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();

        Object o = lagSparkelKlient().hentInntektsliste(aktørId,
            Date.from(LocalDate.ofYearDay(2019,1).atStartOfDay().toInstant(ZoneOffset.UTC)),
            new Date());
        System.out.println(o);
    }

    @Test
    public void hentMeldekortViaSparkel() throws IOException {
        TestscenarioDto testscenario = opprettScenario("50");

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();

        Object o = lagSparkelKlient().hentMeldekortGrunnlag(aktørId,
            Date.from(LocalDate.ofYearDay(2019,1).atStartOfDay().toInstant(ZoneOffset.UTC)),
            new Date());
        System.out.println(o);
    }


    @Test
    public void hentOrganisasjonViaSparkel() throws IOException {
        TestscenarioDto testscenario = opprettScenario("50");
        final String orgnr = testscenario.getScenariodata().getArbeidsforholdModell().getArbeidsforhold().get(0).getArbeidsgiverOrgnr(); // "979191138";
        Object o = lagSparkelKlient().hentOrganisasjon(orgnr);
        System.out.println(o);
    }


    @Test
    public void hentPersonViaSparkel() throws IOException {
        TestscenarioDto testscenario = opprettScenario("50");

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();

        Object o = lagSparkelKlient().hentPerson(aktørId);
        System.out.println(o);
    }

    @Test
    public void hentPersonHistorikkViaSparkel() throws IOException {
        TestscenarioDto testscenario = opprettScenario("50");

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();

        Object o = lagSparkelKlient().hentPersonHistorikk(aktørId);
        System.out.println(o);
    }

    @Test
    public void hentSykepengelisteViaSparkel() throws IOException {
        TestscenarioDto testscenario = opprettScenario("50");

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();
        final String fnr = testscenario.getPersonopplysninger().getSøkerIdent();

        Object o = lagSparkelKlient().hentSykepengeliste(aktørId,
            Date.from(LocalDate.ofYearDay(2017,1).atStartOfDay().toInstant(ZoneOffset.UTC)),
            new Date());
        System.out.println(o);
    }


    ///////
    // TODO ? sakOgBehandling
    ///////


    private SparkelKlient lagSparkelKlient() throws IOException {
        FakeAccessTokenKlient.TokenResponse resp = new FakeAccessTokenKlient().hentTokenForSubject("srvspa");
        System.out.println(resp);

        return new SparkelKlient(resp.idToken);
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
INNTEKT_ENDPOINTURL    https://localhost:8063/inntektskomponenten-ws/inntekt/v3/Inntekt
MELDEKORT_UTBETALINGSGRUNNLAG_ENDPOINTURL   https://localhost:8063/ail_ws/MeldekortUtbetalingsgrunnlag_v1
ORGANISASJON_ENDPOINTURL  https://localhost:8063/ereg/ws/OrganisasjonService/v5
PERSON_ENDPOINTURL  https://localhost:8063/tpsws/ws/Person/v3
HENT_SYKEPENGER_ENDPOINTURL   https://localhost:8063/sykepenger/v2/Sykepenger_v2


     */

}
