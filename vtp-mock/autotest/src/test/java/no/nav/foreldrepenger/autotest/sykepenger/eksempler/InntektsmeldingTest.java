package no.nav.foreldrepenger.autotest.sykepenger.eksempler;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.autotest.aktoerer.Aktoer.Rolle;
import no.nav.foreldrepenger.autotest.sykepenger.SpsakTestBase;
import no.nav.foreldrepenger.autotest.sykepenger.modell.InntektsmeldingWrapper;
import no.nav.foreldrepenger.fpmock2.dokumentgenerator.inntektsmelding.erketyper.InntektsmeldingBuilder;
import no.nav.foreldrepenger.fpmock2.server.api.scenario.TestscenarioDto;
import no.nav.foreldrepenger.fpmock2.testmodell.util.JsonMapper;
import no.nav.sykepenger.spmock.kafka.LocalKafkaProducer;

@Tag("eksempel")
public class InntektsmeldingTest extends SpsakTestBase {

    @Test
    public void test1() throws IOException {
        TestscenarioDto testscenario = opprettScenario("40");
        List<InntektsmeldingBuilder> inntektsmeldinger = makeInntektsmeldingFromTestscenario(testscenario, LocalDate.now());
        InntektsmeldingBuilder inntektsmelding = inntektsmeldinger.get(0); // bruker en av inntektsrapporteringene fra skatt, som grunnlag for inntektsmelding

        long beloep = inntektsmelding.getArbeidsforhold().getBeregnetInntekt().getValue().getBeloep().getValue().longValue();

        // ikke noe gradringsinfo for SP (?)
        //inntektsmelding.addGradertperiode(100, InntektsmeldingBuilder.createPeriode(LocalDate.now().plusWeeks(3), LocalDate.now().plusWeeks(5)));

        inntektsmelding.setRefusjon(InntektsmeldingBuilder.createRefusjon(new BigDecimal(beloep), null, null));

        inntektsmelding.setSykepengerIArbeidsgiverperioden(
                InntektsmeldingBuilder.createSykepengerIArbeidsgiverperioden(
                        new BigDecimal(beloep / 31 * 16),
                        Arrays.asList(InntektsmeldingBuilder.createPeriode(LocalDate.now(), LocalDate.now().plusDays(16))),
                        null) //request.getInntektsmeldingSykepengerIArbeidsgiverperiodenDTO().getBegrunnelseForReduksjon()
        );


        final String xml = inntektsmelding.createInntektesmeldingXML();
        System.out.println(xml);

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();
        fordel.erLoggetInnMedRolle(Rolle.SAKSBEHANDLER);
        final String journalpostId = fordel.journalførInntektsmeldingUtenSaksnummer(inntektsmelding, testscenario);
        final Long saksnummer = fordel.opprettSakKnyttetTilJournalpost(journalpostId, "ab0061", aktørId);

        var inntektsmeldingWrapper = new InntektsmeldingWrapper(journalpostId, aktørId, saksnummer,
                Base64.getEncoder().encodeToString(xml.getBytes(Charset.forName("UTF-8"))), xml.length());

        String json = new JsonMapper().lagObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(inntektsmeldingWrapper);
        System.out.println(json);
        new LocalKafkaProducer().sendSynkront("inntektsmelding",
                aktørId,
                json);
    }

    @Test
    public void test2() throws IOException {
        TestscenarioDto testscenario = opprettScenario("40");
        List<InntektsmeldingBuilder> inntektsmeldinger = makeInntektsmeldingFromTestscenario(testscenario, LocalDate.now());
        InntektsmeldingBuilder inntektsmelding = inntektsmeldinger.get(0); // bruker en av inntektsrapporteringene fra skatt, som grunnlag for inntektsmelding

        long beloep = inntektsmelding.getArbeidsforhold().getBeregnetInntekt().getValue().getBeloep().getValue().longValue();

        // ikke noe gradringsinfo for SP (?)
        //inntektsmelding.addGradertperiode(100, InntektsmeldingBuilder.createPeriode(LocalDate.now().plusWeeks(3), LocalDate.now().plusWeeks(5)));

        inntektsmelding.setRefusjon(InntektsmeldingBuilder.createRefusjon(new BigDecimal(beloep), null, null));

        inntektsmelding.setSykepengerIArbeidsgiverperioden(
                InntektsmeldingBuilder.createSykepengerIArbeidsgiverperioden(
                        new BigDecimal(beloep / 31 * 16),
                        Arrays.asList(InntektsmeldingBuilder.createPeriode(LocalDate.now(), LocalDate.now().plusDays(16))),
                        null) //request.getInntektsmeldingSykepengerIArbeidsgiverperiodenDTO().getBegrunnelseForReduksjon()
        );


        final String xml = inntektsmelding.createInntektesmeldingXML();
        System.out.println(xml);

        final String aktørId = testscenario.getPersonopplysninger().getSøkerAktørIdent();


        var inntektsmeldingWrapper = new InntektsmeldingWrapper("1130152002", aktørId, 1130152001L,
                Base64.getEncoder().encodeToString(xml.getBytes(Charset.forName("UTF-8"))), xml.length());

        String json = new JsonMapper().lagObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(inntektsmeldingWrapper);
        System.out.println(json);
        new LocalKafkaProducer().sendSynkront("inntektsmelding",
                aktørId,
                json);
    }

}

