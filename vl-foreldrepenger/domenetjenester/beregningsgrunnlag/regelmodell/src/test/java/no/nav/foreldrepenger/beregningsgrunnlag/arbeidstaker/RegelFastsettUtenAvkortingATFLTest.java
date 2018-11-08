package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagAvkortetPrÅr;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class RegelFastsettUtenAvkortingATFLTest {

    LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

    private static final String ARBEIDSFORHOLDID1 = "123";
    private static final String ARBEIDSFORHOLDID2 = "456";

    @Test
    public void skalFastsetteNårBruttoBGLikRefusjon() {
        //Arrange
        BigDecimal bruttoBG = BigDecimal.valueOf(448000d);
        BigDecimal refusjonsKrav = BigDecimal.valueOf(448000d);

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlagPeriode(1, bruttoBG, null, refusjonsKrav, null);

        //Act
        Evaluation evaluation = new RegelFastsettUtenAvkortingATFL().evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, 0d, null);
        verifiserArbeidsgiversAndel(grunnlag,448000d, null);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, 448000d);
    }


    @Test
    public void skalFastsetteNårRefusjonErDelerAvInntekten() {
        //Arrange
        BigDecimal bruttoBG = BigDecimal.valueOf(448000d);
        BigDecimal refusjonsKrav = BigDecimal.valueOf(300000d);

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlagPeriode(1, bruttoBG, null, refusjonsKrav, null);

        //Act
        Evaluation evaluation = new RegelFastsettUtenAvkortingATFL().evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, 148000d, null);
        verifiserArbeidsgiversAndel(grunnlag,300000d, null);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, 448000d);
    }

    @Test
    public void skalFastsetteNårRefusjonKravErNull() {
        //Arrange
        BigDecimal bruttoBG1 = BigDecimal.valueOf(168000d);
        BigDecimal bruttoBG2 = BigDecimal.valueOf(336000d);
        BigDecimal refusjonsKrav = BigDecimal.valueOf(0d);

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlagPeriode(2, bruttoBG1, bruttoBG2, refusjonsKrav, refusjonsKrav);

        //Act
        Evaluation evaluation = new RegelFastsettUtenAvkortingATFL().evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, 168000d, 336000d);
        verifiserArbeidsgiversAndel(grunnlag,0d, 0d);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, 504000d);
    }

    @Test
    public void skalFastsetteNårRefusjonErDelerAvInntektenMedFlereArbeidsforhold() {
        //Arrange
        BigDecimal bruttoBG1 = BigDecimal.valueOf(168000d);
        BigDecimal bruttoBG2 = BigDecimal.valueOf(336000d);
        BigDecimal refusjonsKrav1 = BigDecimal.valueOf(100000d);
        BigDecimal refusjonsKrav2 = BigDecimal.valueOf(300000d);

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlagPeriode(2, bruttoBG1, bruttoBG2, refusjonsKrav1, refusjonsKrav2);

        //Act
        Evaluation evaluation = new RegelFastsettUtenAvkortingATFL().evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, 68000d, 36000d);
        verifiserArbeidsgiversAndel(grunnlag,100000d, 300000d);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, 504000d);
    }

    private BeregningsgrunnlagPeriode lagBeregningsgrunnlagPeriode(int antallArbeidsgiver, BigDecimal bruttoBG1, BigDecimal bruttoBG2,
            BigDecimal refusjonsKrav1, BigDecimal refusjonsKrav2) {

        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));

        BeregningsgrunnlagPrStatus bgpsATFL = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .build();

        BeregningsgrunnlagPrArbeidsforhold afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ARBEIDSFORHOLDID1))
            .medAndelNr(1)
            .medBeregnetPrÅr(bruttoBG1)
            .medMaksimalRefusjonPrÅr(refusjonsKrav1)
            .build();

        bgpsATFL.getArbeidsforhold().add(afBuilder1);

        if (antallArbeidsgiver == 1) {
            return bgBuilder.medBeregningsgrunnlagPrStatus(bgpsATFL).build();
        }

        BeregningsgrunnlagPrArbeidsforhold afBuilder2 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ARBEIDSFORHOLDID2))
            .medAndelNr(2)
            .medBeregnetPrÅr(bruttoBG2)
            .medMaksimalRefusjonPrÅr(refusjonsKrav2)
            .build();

        bgpsATFL.getArbeidsforhold().add(afBuilder2);

        return bgBuilder.medBeregningsgrunnlagPrStatus(bgpsATFL).build();
    }


    private void verifiserBrukersAndel(BeregningsgrunnlagPeriode grunnlag, Double beløp1, Double beløp2) {
        List<BigDecimal> brukersAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream().
            map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetBrukersAndelPrÅr).collect(Collectors.toList());

        if(brukersAndel.size() == 1) {
            assertThat(brukersAndel.get(0).doubleValue()).isCloseTo(beløp1, within(0.01));
        }
        else {
            assertThat(brukersAndel.get(0).doubleValue()).isCloseTo(beløp1, within(0.01));
            assertThat(brukersAndel.get(1).doubleValue()).isCloseTo(beløp2, within(0.01));
        }
    }

    private void verifiserArbeidsgiversAndel(BeregningsgrunnlagPeriode grunnlag, Double beløp1, Double beløp2) {
        List<BigDecimal> arbeidsgiversAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream().
            map(BeregningsgrunnlagPrArbeidsforhold::getMaksimalRefusjonPrÅr).collect(Collectors.toList());

        if(arbeidsgiversAndel.size() == 1) {
            assertThat(arbeidsgiversAndel.get(0).doubleValue()).isCloseTo(beløp1, within(0.01));
        }
        else {
            assertThat(arbeidsgiversAndel.get(0).doubleValue()).isCloseTo(beløp1, within(0.01));
            assertThat(arbeidsgiversAndel.get(1).doubleValue()).isCloseTo(beløp2, within(0.01));
        }
    }

}
