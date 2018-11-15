package no.nav.foreldrepenger.autotest.sykepenger.eksempler;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.autotest.aktoerer.Aktoer.Rolle;
import no.nav.foreldrepenger.autotest.sykepenger.SpsakTestBase;
import no.nav.foreldrepenger.fpmock2.dokumentgenerator.inntektsmelding.erketyper.InntektsmeldingBuilder;
import no.nav.foreldrepenger.fpmock2.server.api.scenario.TestscenarioDto;

@Tag("eksempel")
public class Inntektsmelding extends SpsakTestBase {

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


        System.out.println(inntektsmelding.createInntektesmeldingXML());

        fordel.erLoggetInnMedRolle(Rolle.SAKSBEHANDLER);
        long saksnummer = fordel.sendInnInntektsmelding(inntektsmelding, testscenario, null);

        System.out.println(saksnummer);
    }

}

