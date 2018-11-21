package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.avkorting;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagAvkortetPrÅr;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Periodeinntekt;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

/**
 * Testen kjøres når Regelflyt 13 blir implementert
 */

public class RegelFastsettAvkortetBGNårRefusjonOver6GTest {

    private final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    private final long seksG = GRUNNBELØP_2017 * 6;

    private static final String ORGNR1 = "123";
    private static final String ORGNR2 = "456";
    private static final String ORGNR3 = "789";
    private static final String ORGNR4 = "101112";

    @Test
    //Scenario 8: Alle arbeidsgivere har refusjonskrav lik brukers brutto beregningsgrunnlag
    public void skalBeregneNårRefusjonKravLikBruttoBGForBeggeToArbeidsgivere() {
        //Arrange
        double bruttoBG1 = 448000d;
        double bruttoBG2 = 336000d;
        double refusjonsKrav1 = 448000d;
        double refusjonsKrav2 = 336000d;

        double forventetRedusert1 = seksG * bruttoBG1 / (bruttoBG1 + bruttoBG2);
        double forventetRedusert2 = seksG * bruttoBG2 / (bruttoBG1 + bruttoBG2);

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(2, Arrays.asList(bruttoBG1, bruttoBG2), Arrays.asList(refusjonsKrav1, refusjonsKrav2))
                .getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2),
                Arrays.asList(forventetRedusert1, forventetRedusert2));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 8: Alle arbeidsgivere har refusjonskrav lik brukers brutto beregningsgrunnlag
    public void skalBeregneNårRefusjonKravLikBruttoBGForAlleTreArbeidsgivere() {
        //Arrange
        double bruttoBG1 = 100000d;
        double bruttoBG2 = 400000d;
        double bruttoBG3 = 250000d;
        double refusjonsKrav1 = 100000d;
        double refusjonsKrav2 = 400000d;
        double refusjonsKrav3 = 250000d;
        double forventetRedusert1 = seksG * bruttoBG1 / (bruttoBG1 + bruttoBG2 + bruttoBG3);
        double forventetRedusert2 = seksG * bruttoBG2 / (bruttoBG1 + bruttoBG2 + bruttoBG3);
        double forventetRedusert3 = seksG * bruttoBG3 / (bruttoBG1 + bruttoBG2 + bruttoBG3);

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(3, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3),
                Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
                .getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
                Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 8: En av de tre arbeidsgiverne har refusjonskrav mindre enn brukers brutto beregningsgrunnlag ndre
    public void skalBeregneNårRefusjonKravErIkkeLikBGForEnAvDeTreArbeidsgivere() {
        //Arrange
        double bruttoBG1 = 100000d;
        double bruttoBG2 = 400000d;
        double bruttoBG3 = 250000d;
        double refusjonsKrav1 = 100000d;
        double refusjonsKrav2 = 280000d;
        double refusjonsKrav3 = 250000d;

        double fordelingRunde2 = seksG - refusjonsKrav2;
        double forventetRedusert1 = fordelingRunde2 * bruttoBG1 / (bruttoBG1 + bruttoBG3);
        double forventetRedusert2 = refusjonsKrav2;
        double forventetRedusert3 = fordelingRunde2 * bruttoBG3 / (bruttoBG1 + bruttoBG3);

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(3, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3),
                Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
                .getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
                Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 11: Alle arbeidsgivere har refusjonskrav mindre enn brutto beregningsgrunnlag for arbeidsgiveren
    public void skalBeregneNårRefusjonKravErNullForEnAvDeTreArbeidsgivere() {
        //Arrange
        double bruttoBG1 = 600000d;
        double bruttoBG2 = 750000d;
        double bruttoBG3 = 250000d;
        double refusjonsKrav1 = seksG;
        double refusjonsKrav2 = seksG;
        double refusjonsKrav3 = 0d;

        double fordelingRunde2 = seksG;
        double forventetRedusert1 = fordelingRunde2 * bruttoBG1 / (bruttoBG1 + bruttoBG2);
        double forventetRedusert2 = fordelingRunde2 * bruttoBG2 / (bruttoBG1 + bruttoBG2);
        double forventetRedusert3 = 0;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(3, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3),
                Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
                .getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
                Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 11A: Alle arbeidsgivere har refusjonskrav mindre enn brutto beregningsgrunnlag for arbeidsgiveren
    public void skalBeregneNårRefusjonKravLavereEnnBGForFireArbeidsforhold() {
        //Arrange
        double bruttoBG1 = 400000d;
        double bruttoBG2 = 500000d;
        double bruttoBG3 = 300000d;
        double bruttoBG4 = 100000d;
        double refusjonsKrav1 = 200000d;
        double refusjonsKrav2 = 150000d;
        double refusjonsKrav3 = 300000d;
        double refusjonsKrav4 = 100000d;

        double fordelingRunde2 = seksG - (refusjonsKrav1 + refusjonsKrav2);
        double forventetRedusert1 = refusjonsKrav1;
        double forventetRedusert2 = refusjonsKrav2;
        double forventetRedusert3 = fordelingRunde2 * bruttoBG3 / (bruttoBG3 + bruttoBG4);
        double forventetRedusert4 = fordelingRunde2 * bruttoBG4 / (bruttoBG3 + bruttoBG4);

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(4, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3, bruttoBG4),
                Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3, refusjonsKrav4))
                .getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(0d, 0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3, refusjonsKrav4),
                Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3, forventetRedusert4));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 12: Flere arbeidsgivere har refusjonskrav og totalt refusjonskrav større enn 6G
    public void skalBeregneNårMaksRefusjonKravErMerEnnFordeltRefusjonForToArbeidsgivere() {
        //Arrange
        double bruttoBG1 = 600000d;
        double bruttoBG2 = 750000d;
        double bruttoBG3 = 250000d;
        double refusjonsKrav1 = seksG;
        double refusjonsKrav2 = 200000d;
        double refusjonsKrav3 = 200000d;
        double fordelingRunde2 = seksG - refusjonsKrav2;
        double forventetRedusert1 = fordelingRunde2 * bruttoBG1 / (bruttoBG1 + bruttoBG3);
        double forventetRedusert2 = refusjonsKrav2;
        double forventetRedusert3 = fordelingRunde2 * bruttoBG3 / (bruttoBG1 + bruttoBG3);

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(3, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3),
                Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
                .getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
                Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 12: Flere arbeidsgivere har refusjonskrav og totalt refusjonskrav større enn 6G
    public void skalBeregneNårMaksRefusjonKravErMerEnnFordeltRefusjonForEttArbeidsgiver() {
        //Arrange
        double bruttoBG1 = 600000d;
        double bruttoBG2 = 750000d;
        double bruttoBG3 = 250000d;
        double refusjonsKrav1 = 200000d;
        double refusjonsKrav2 = 200000d;
        double refusjonsKrav3 = 200000d;
        double fordelingRunde2 = seksG - (refusjonsKrav1 + refusjonsKrav2);
        double forventetRedusert1 = refusjonsKrav1;
        double forventetRedusert2 = refusjonsKrav2;
        double forventetRedusert3 = fordelingRunde2;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(3, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3),
                Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
                .getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
                Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 12: Flere arbeidsgivere har refusjonskrav og totalt refusjonskrav større enn 6G
    public void skalBeregneNårMaksRefusjonKravEr6GForToAvDeTreArbeidsgiverne() {
        //Arrange
        double bruttoBG1 = 600000d;
        double bruttoBG2 = 750000d;
        double bruttoBG3 = 250000d;
        double refusjonsKrav1 = seksG;
        double refusjonsKrav2 = seksG;
        double refusjonsKrav3 = 50000d;

        double fordelingRunde2 = seksG - refusjonsKrav3;
        double forventetRedusert1 = fordelingRunde2 * bruttoBG1 / (bruttoBG1 + bruttoBG2);
        double forventetRedusert2 = fordelingRunde2 * bruttoBG2 / (bruttoBG1 + bruttoBG2);
        double forventetRedusert3 = refusjonsKrav3;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(3, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3),
                Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
                .getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
                Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }


    private Beregningsgrunnlag lagBeregningsgrunnlag(int antallArbeidsforhold, List<Double> bruttoBG, List<Double> refusjonsKrav) {

        assertThat(bruttoBG).hasSize(antallArbeidsforhold);
        assertThat(refusjonsKrav).hasSize(antallArbeidsforhold);
        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        BeregningsgrunnlagPrArbeidsforhold afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR1))
            .medAndelNr(1)
            .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(0)))
            .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(0)))
            .build();
        BeregningsgrunnlagPrArbeidsforhold afBuilder2 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
            .medAndelNr(2)
            .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(1)))
            .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(1)))
            .build();

        if (antallArbeidsforhold == 2) {
            BeregningsgrunnlagPrStatus bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .medArbeidsforhold(afBuilder1)
                    .medArbeidsforhold(afBuilder2)
                    .build();
            BeregningsgrunnlagPeriode periode = bgBuilder
                    .medBeregningsgrunnlagPrStatus(bgpsATFL)
                    .build();
            return opprettGrunnlag(periode);
        }

        BeregningsgrunnlagPrArbeidsforhold afBuilder3 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR3))
            .medAndelNr(3)
            .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(2)))
            .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(2)))
            .build();
        if (antallArbeidsforhold == 3) {
            BeregningsgrunnlagPrStatus bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(afBuilder1)
                .medArbeidsforhold(afBuilder2)
                .medArbeidsforhold(afBuilder3)
                .build();
            BeregningsgrunnlagPeriode periode = bgBuilder
                .medBeregningsgrunnlagPrStatus(bgpsATFL)
                .build();
            return opprettGrunnlag(periode);
        }
        BeregningsgrunnlagPrArbeidsforhold afBuilder4 = BeregningsgrunnlagPrArbeidsforhold.builder()
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR4))
                .medAndelNr(4)
                .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(3)))
                .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(3)))
                .build();
        BeregningsgrunnlagPrStatus bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(afBuilder1)
                .medArbeidsforhold(afBuilder2)
                .medArbeidsforhold(afBuilder3)
                .medArbeidsforhold(afBuilder4)
                .build();
            BeregningsgrunnlagPeriode periode = bgBuilder
                .medBeregningsgrunnlagPrStatus(bgpsATFL)
                .build();
            return opprettGrunnlag(periode);
    }

    private Beregningsgrunnlag opprettGrunnlag(BeregningsgrunnlagPeriode periode) {
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        arbeidsforhold.forEach(af -> {
            Periodeinntekt månedsinntekt = Periodeinntekt.builder()
                .medInntektskilde(Inntektskilde.INNTEKTSMELDING)
                .medArbeidsgiver(af.getArbeidsforhold())
                .medMåned(skjæringstidspunkt)
                .medInntekt(af.getBruttoPrÅr())
                .build();
            inntektsgrunnlag.leggTilPeriodeinntekt(månedsinntekt);
        });
        return Beregningsgrunnlag.builder()
                .medAktivitetStatuser(Arrays.asList(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, null)))
                .medBeregningsgrunnlagPeriode(periode)
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .medInntektsgrunnlag(inntektsgrunnlag)
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
                .medRedusertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
                .medGrunnbeløpSatser(GRUNNBELØPLISTE)
                .build();
    }

    private void verifiserBrukersAndel(BeregningsgrunnlagPeriode grunnlag, List<Double> beløp) {
        List<BigDecimal> brukersAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream().
            map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetBrukersAndelPrÅr).collect(Collectors.toList());
        assertThat(brukersAndel).hasSameSizeAs(beløp);
        for (int ix = 0; ix < beløp.size(); ix++) {
            assertThat(brukersAndel.get(ix).doubleValue()).isCloseTo(beløp.get(ix), within(0.01));
        }
    }

    private void verifiserArbeidsgiversAndel(BeregningsgrunnlagPeriode grunnlag, List<Double> maksimalRefusjon, List<Double> redusertRefusjon) {
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsgiversAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        assertThat(arbeidsgiversAndel).hasSameSizeAs(maksimalRefusjon);
        assertThat(arbeidsgiversAndel).hasSameSizeAs(redusertRefusjon);
        for (int ix = 0; ix < arbeidsgiversAndel.size(); ix++) {
            verifiserEnkeltAndel(arbeidsgiversAndel.get(ix), maksimalRefusjon.get(ix), redusertRefusjon.get(ix));
        }
    }

    private void verifiserEnkeltAndel(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, double maksimalrefusjon, double redusertrefusjon) {
        assertThat(arbeidsforhold.getMaksimalRefusjonPrÅr().doubleValue()).isEqualTo(maksimalrefusjon, within(0.01));
        assertThat(arbeidsforhold.getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(redusertrefusjon, within(0.01));
    }
}
