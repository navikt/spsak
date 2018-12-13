package no.nav.foreldrepenger.autotest.sykepenger.eksempler;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.foreldrepenger.autotest.aktoerer.Aktoer;
import no.nav.foreldrepenger.autotest.sykepenger.SpsakTestBase;
import no.nav.foreldrepenger.autotest.sykepenger.modell.InntektsmeldingWrapper;
import no.nav.foreldrepenger.autotest.sykepenger.modell.SykepengesøknadWrapper;
import no.nav.foreldrepenger.fpmock2.dokumentgenerator.inntektsmelding.erketyper.InntektsmeldingBuilder;
import no.nav.foreldrepenger.fpmock2.server.api.scenario.TestscenarioDto;
import no.nav.foreldrepenger.fpmock2.testmodell.dokument.modell.koder.DokumenttypeId;
import no.nav.foreldrepenger.fpmock2.testmodell.inntektytelse.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.fpmock2.testmodell.util.JsonMapper;
import no.nav.sykepenger.kontrakter.søknad.v1.SykepengesøknadV1;
import no.nav.sykepenger.kontrakter.søknad.v1.fravær.FraværType;
import no.nav.sykepenger.kontrakter.søknad.v1.fravær.FraværsPeriode;
import no.nav.sykepenger.kontrakter.søknad.v1.perioder.EgenmeldingPeriode;
import no.nav.sykepenger.kontrakter.søknad.v1.perioder.KorrigertArbeidstidPeriode;
import no.nav.sykepenger.spmock.kafka.LocalKafkaProducer;

@Tag("eksempel")
class SykepengesøknadTest extends SpsakTestBase {

    private LocalKafkaProducer producer = new LocalKafkaProducer();
    private JsonMapper jsonMapper = new JsonMapper();

    @Test
    public void testSykepengesøknadInnsending() throws Exception {

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
        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();
        fordel.erLoggetInnMedRolle(Aktoer.Rolle.SAKSBEHANDLER);
        // ikkeAndreVeien fordi journalpostId er "fake"
        String journalpostId = fordel.journalførSøknad(søknad, testscenario, DokumenttypeId.FOEDSELSSOKNAD_FORELDREPENGER, null);

        final Long saksnummer = fordel.opprettSakKnyttetTilJournalpostMenIkkeAndreVeien(journalpostId, "ab0061", aktørId);

        var sykepengesøknadWrapper = new SykepengesøknadWrapper(journalpostId, aktørId, saksnummer.toString(),
            Base64.getEncoder().encodeToString(søknadJson.getBytes(Charset.forName("UTF-8"))), søknadJson.length());

        System.out.println("Opprettet sak: " + saksnummer);

        producer.sendSynkront("sykepengesoeknad",
            testscenario.getPersonopplysninger().getSøkerAktørIdent(),
            new JsonMapper().lagObjectMapper().writeValueAsString(sykepengesøknadWrapper));
    }

    @Test
    public void testSykepengesøknadInnsendingMedInntektsmelding() throws Exception {
        TestscenarioDto testscenario = opprettScenario("40");
        final LocalDate sykFom = LocalDate.now().minusMonths(1);
        final LocalDate sykTom = LocalDate.now().minusDays(1);
        List<FraværsPeriode> fravær = List.of(new FraværsPeriode(sykFom.plusDays(7), sykFom.plusDays(8), FraværType.UTENLANDSOPPHOLD));
        List<KorrigertArbeidstidPeriode> korrigertArbeidstid = List.of(new KorrigertArbeidstidPeriode(sykFom, sykTom, 100, 0, 0));
        List<EgenmeldingPeriode> egenmeldinger = List.of(new EgenmeldingPeriode(sykFom, sykFom.plusDays(2)));

        sendInnSøknadFor(testscenario, fravær, korrigertArbeidstid, egenmeldinger);
    }

    @Test
    public void scenario_50() throws Exception {
        TestscenarioDto testscenario = opprettScenario("40");
        final LocalDate sykFom = LocalDate.now().minusMonths(2);
        final LocalDate sykTom = LocalDate.now().minusMonths(1).minusDays(1);
        List<FraværsPeriode> fravær = List.of(new FraværsPeriode(sykFom.plusDays(7), sykFom.plusDays(8), FraværType.UTENLANDSOPPHOLD));
        List<KorrigertArbeidstidPeriode> korrigertArbeidstid = List.of(new KorrigertArbeidstidPeriode(sykFom, sykTom, 100, 0, 0));
        List<EgenmeldingPeriode> egenmeldinger = List.of(new EgenmeldingPeriode(sykFom, sykFom.plusDays(2)));

        sendInnSøknadFor(testscenario, fravær, korrigertArbeidstid, egenmeldinger);
    }

    @Test
    public void scenario_50_søkt_for_seint() throws Exception {
        TestscenarioDto testscenario = opprettScenario("40");
        final LocalDate sykFom = LocalDate.now().minusMonths(6);
        final LocalDate sykTom = LocalDate.now().minusMonths(5).minusDays(1);
        List<FraværsPeriode> fravær = List.of(new FraværsPeriode(sykFom.plusDays(7), sykFom.plusDays(8), FraværType.UTENLANDSOPPHOLD));
        List<KorrigertArbeidstidPeriode> korrigertArbeidstid = List.of(new KorrigertArbeidstidPeriode(sykFom, sykTom, 100, 0, 0));
        List<EgenmeldingPeriode> egenmeldinger = List.of(new EgenmeldingPeriode(sykFom, sykFom.plusDays(2)));

        sendInnSøknadFor(testscenario, fravær, korrigertArbeidstid, egenmeldinger);
    }

    private void sendInnSøknadFor(TestscenarioDto testscenario, List<FraværsPeriode> fravær, List<KorrigertArbeidstidPeriode> korrigertArbeidstid, List<EgenmeldingPeriode> egenmeldinger) throws Exception {
        final String sykemeldingId = UUID.randomUUID().toString();
        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();
        Long saksnummer = null;

        Set<String> arbeidsgivere = testscenario.getScenariodata()
            .getArbeidsforholdModell()
            .getArbeidsforhold()
            .stream()
            .map(Arbeidsforhold::getArbeidsgiverOrgnr)
            .collect(Collectors.toSet());
        for (String arbeidsgiver : arbeidsgivere) {

            final String søknadId = UUID.randomUUID().toString();
            var søknad = new SykepengesøknadV1();
            søknad.setSøknadId(søknadId);
            søknad.setBrukerAktørId(aktørId);
            søknad.setArbeidsgiverId(arbeidsgiver);
            søknad.setSykemeldingId(sykemeldingId);
            søknad.setKorrigertArbeidstid(korrigertArbeidstid);
            søknad.setFravær(fravær);
            søknad.setSøktOmUtenlandsopphold(false);
            søknad.setEgenmeldinger(egenmeldinger);

            ObjectWriter mapper = jsonMapper.lagObjectMapper().writerWithDefaultPrettyPrinter();
            String søknadJson = mapper.writeValueAsString(søknad);
            fordel.erLoggetInnMedRolle(Aktoer.Rolle.SAKSBEHANDLER);
            String journalpostId = fordel.journalførSøknad(søknad, testscenario, DokumenttypeId.FOEDSELSSOKNAD_FORELDREPENGER, saksnummer);
            if (saksnummer == null) {
                saksnummer = fordel.opprettSakKnyttetTilJournalpost(journalpostId, "ab0061", aktørId);
                System.out.println("Opprettet sak: " + saksnummer);
            }
            var sykepengesøknadWrapper = new SykepengesøknadWrapper(journalpostId, aktørId, saksnummer.toString(),
                Base64.getEncoder().encodeToString(søknadJson.getBytes(Charset.forName("UTF-8"))), søknadJson.length());

            producer.sendSynkront("sykepengesoeknad",
                testscenario.getPersonopplysninger().getSøkerAktørIdent(),
                jsonMapper.lagObjectMapper().writeValueAsString(sykepengesøknadWrapper));
        }

        List<InntektsmeldingBuilder> inntektsmeldinger = makeInntektsmeldingFromTestscenario(testscenario, LocalDate.now());
        InntektsmeldingBuilder inntektsmelding = inntektsmeldinger.get(0);
        long beloep = inntektsmelding.getArbeidsforhold().getBeregnetInntekt().getValue().getBeloep().getValue().longValue();

        // ikke noe gradringsinfo for SP (?)
        //inntektsmelding.addGradertperiode(100, InntektsmeldingBuilder.createPeriode(LocalDate.now().plusWeeks(3), LocalDate.now().plusWeeks(5)));

        inntektsmelding.setRefusjon(InntektsmeldingBuilder.createRefusjon(new BigDecimal(beloep), null, null));

        inntektsmelding.setSykepengerIArbeidsgiverperioden(
            InntektsmeldingBuilder.createSykepengerIArbeidsgiverperioden(
                new BigDecimal(beloep / 31 * 16),
                Collections.singletonList(InntektsmeldingBuilder.createPeriode(LocalDate.now(), LocalDate.now().plusDays(16))),
                null) //request.getInntektsmeldingSykepengerIArbeidsgiverperiodenDTO().getBegrunnelseForReduksjon()
        );
        final String journalpostIdIm = fordel.journalførInnektsmelding(inntektsmelding, testscenario, saksnummer);
        String xml = inntektsmelding.createInntektesmeldingXML();
        var inntektsmeldingWrapper = new InntektsmeldingWrapper(journalpostIdIm, aktørId, saksnummer,
            Base64.getEncoder().encodeToString(xml.getBytes(Charset.forName("UTF-8"))), xml.length());

        String json = jsonMapper.lagObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(inntektsmeldingWrapper);
        producer.sendSynkront("inntektsmelding",
            aktørId,
            json);
    }

}
