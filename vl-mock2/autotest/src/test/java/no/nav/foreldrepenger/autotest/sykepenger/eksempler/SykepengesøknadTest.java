package no.nav.foreldrepenger.autotest.sykepenger.eksempler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.autotest.sykepenger.SpsakTestBase;
import no.nav.foreldrepenger.autotest.sykepenger.modell.sykepengesøknad.EgenmeldingPeriode;
import no.nav.foreldrepenger.autotest.sykepenger.modell.sykepengesøknad.FraværType;
import no.nav.foreldrepenger.autotest.sykepenger.modell.sykepengesøknad.FraværsPeriode;
import no.nav.foreldrepenger.autotest.sykepenger.modell.sykepengesøknad.KorrigertArbeidstidPeriode;
import no.nav.foreldrepenger.autotest.sykepenger.modell.sykepengesøknad.Sykepengesøknad;
import no.nav.foreldrepenger.fpmock2.server.api.scenario.TestscenarioDto;
import no.nav.foreldrepenger.fpmock2.testmodell.util.JsonMapper;
import no.nav.sykepenger.spmock.kafka.LocalKafkaProducer;

@Tag("eksempel")
class SykepengesøknadTest extends SpsakTestBase {

    @Test
    public void testSykepengesøknadInnsending() throws IOException {

        TestscenarioDto testscenario = opprettScenario("40");

        Sykepengesøknad søknad = new Sykepengesøknad();
        final String søknadId = UUID.randomUUID().toString();
        søknad.setSøknadId(søknadId);
        søknad.setBrukerAktørId(testscenario.getPersonopplysninger().getSøkerAktørIdent());
        søknad.setArbeidsgiverId(testscenario.getScenariodata().getArbeidsforholdModell().getArbeidsforhold().get(0).getArbeidsgiverOrgnr());
        final String sykemeldingId = UUID.randomUUID().toString();
        søknad.setSykemeldingId(sykemeldingId);

        final LocalDate sykFom = LocalDate.now().minusMonths(1);
        final LocalDate sykTom = LocalDate.now().minusDays(1);

        søknad.setKorrigertArbeidstid(Arrays.asList(new KorrigertArbeidstidPeriode(sykFom, sykTom, 100, 0, 0)));

        søknad.setFravær(Arrays.asList(new FraværsPeriode(sykFom.plusDays(7), sykFom.plusDays(8), FraværType.UTENLANDSOPPHOLD)));
        søknad.setSøktOmUtenlandsopphold(false);

        søknad.setEgenmeldinger(Arrays.asList(new EgenmeldingPeriode(sykFom, sykFom.plusDays(2))));

        søknad.setPapirsykemeldinger(null);

        søknad.setAndreInntektskilder(null);

        ObjectMapper mapper = new JsonMapper().lagObjectMapper();
        String json = mapper.writeValueAsString(søknad);

        System.out.println(json);

        new LocalKafkaProducer().sendSynkront("sykepengesoeknad", testscenario.getPersonopplysninger().getSøkerAktørIdent(), json);
    }

}
