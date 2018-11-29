package no.nav.foreldrepenger.autotest.sykepenger.eksempler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.foreldrepenger.autotest.aktoerer.Aktoer;
import no.nav.foreldrepenger.autotest.sykepenger.SpsakTestBase;
import no.nav.foreldrepenger.autotest.sykepenger.modell.SykepengesøknadWrapper;
import no.nav.foreldrepenger.fpmock2.server.api.scenario.TestscenarioDto;
import no.nav.foreldrepenger.fpmock2.testmodell.util.JsonMapper;
import no.nav.sykepenger.kontrakter.søknad.v1.SykepengesøknadV1;
import no.nav.sykepenger.kontrakter.søknad.v1.fravær.FraværType;
import no.nav.sykepenger.kontrakter.søknad.v1.fravær.FraværsPeriode;
import no.nav.sykepenger.kontrakter.søknad.v1.perioder.EgenmeldingPeriode;
import no.nav.sykepenger.kontrakter.søknad.v1.perioder.KorrigertArbeidstidPeriode;
import no.nav.sykepenger.spmock.kafka.LocalKafkaProducer;

@Tag("eksempel")
class SykepengesøknadTest extends SpsakTestBase {

    @Test
    public void testSykepengesøknadInnsending() throws IOException {

        TestscenarioDto testscenario = opprettScenario("40");

        var søknad = new SykepengesøknadV1();
        final String søknadId = UUID.randomUUID().toString();
        søknad.setSøknadId(søknadId);
        søknad.setBrukerAktørId(testscenario.getPersonopplysninger().getSøkerAktørIdent());
        søknad.setArbeidsgiverId(testscenario.getScenariodata().getArbeidsforholdModell().getArbeidsforhold().get(0).getArbeidsgiverOrgnr());
        final String sykemeldingId = UUID.randomUUID().toString();
        søknad.setSykemeldingId(sykemeldingId);

        final LocalDate sykFom = LocalDate.now().minusMonths(1);
        final LocalDate sykTom = LocalDate.now().minusDays(1);

        søknad.setKorrigertArbeidstid(List.of(new KorrigertArbeidstidPeriode(sykFom, sykTom, 100, 0, 0)));

        søknad.setFravær(List.of(new FraværsPeriode(sykFom.plusDays(7), sykFom.plusDays(8), FraværType.UTENLANDSOPPHOLD)));
        søknad.setSøktOmUtenlandsopphold(false);

        søknad.setEgenmeldinger(List.of(new EgenmeldingPeriode(sykFom, sykFom.plusDays(2))));

        søknad.setPapirsykemeldinger(null);

        søknad.setAndreInntektskilder(null);

        ObjectWriter mapper = new JsonMapper().lagObjectMapper().writerWithDefaultPrettyPrinter();
        String søknadJson = mapper.writeValueAsString(søknad);

        System.out.println(søknadJson);

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();
        final String journalpostId = "" + lagFalskJournalpostId();
        fordel.erLoggetInnMedRolle(Aktoer.Rolle.SAKSBEHANDLER);
        // ikkeAndreVeien fordi journalpostId er "fake"

        final Long saksnummer = fordel.opprettSakKnyttetTilJournalpostMenIkkeAndreVeien(journalpostId, "ab0061", aktørId);

        var sykepengesøknadWrapper = new SykepengesøknadWrapper(journalpostId, aktørId, saksnummer,
                Base64.getEncoder().encodeToString(søknadJson.getBytes(Charset.forName("UTF-8"))), søknadJson.length());

        System.out.println(mapper.writeValueAsString(sykepengesøknadWrapper));

        new LocalKafkaProducer().sendSynkront("sykepengesoeknad",
                testscenario.getPersonopplysninger().getSøkerAktørIdent(),
                new JsonMapper().lagObjectMapper().writeValueAsString(sykepengesøknadWrapper));
    }

    private long lagFalskJournalpostId() {
        return System.currentTimeMillis() / 1000 - 1000000000;
    }

}
