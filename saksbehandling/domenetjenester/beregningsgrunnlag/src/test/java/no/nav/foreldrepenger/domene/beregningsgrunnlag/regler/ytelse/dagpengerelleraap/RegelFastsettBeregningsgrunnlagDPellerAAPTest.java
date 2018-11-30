package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse.dagpengerelleraap;


import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.RegelmodellOversetter.getRegelResultat;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;

import org.assertj.core.data.Offset;
import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Periodeinntekt;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse.dagpengerelleraap.RegelFastsettBeregningsgrunnlagDPellerAAP;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class RegelFastsettBeregningsgrunnlagDPellerAAPTest {

    private LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

    @Test
    public void skalForeslåBeregningsgrunnlagForDagpenger() {
        //Arrange
        BigDecimal dagsats = new BigDecimal("1142");
        Inntektsgrunnlag inntektsgrunnlag = lagInntektsgrunnlag(dagsats, skjæringstidspunkt, 150);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Collections.singletonList(AktivitetStatus.DP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

        Periode periode = new Periode(skjæringstidspunkt, null);
        assertThat(grunnlag.getBeregningsgrunnlagPeriode()).isEqualTo(periode);

        BigDecimal brutto = BigDecimal.valueOf(296920).stripTrailingZeros();
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7_8_49, AktivitetStatus.DP, brutto.doubleValue());
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP).getBeregnetPrÅr().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()).isEqualTo(brutto);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP).getÅrsbeløpFraTilstøtendeYtelse().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()).isEqualTo(brutto);
    }

    @Test
    public void skalForeslåBeregningsgrunnlagForAAP() {
        //Arrange
        BigDecimal dagsats = new BigDecimal("1611");
        Inntektsgrunnlag inntektsgrunnlag = lagInntektsgrunnlag(dagsats, skjæringstidspunkt, 150);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.AAP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

        Periode periode = new Periode(skjæringstidspunkt, null);
        assertThat(grunnlag.getBeregningsgrunnlagPeriode()).isEqualTo(periode);

        BigDecimal brutto = BigDecimal.valueOf(418860).stripTrailingZeros();
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.AAP, brutto.doubleValue());
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP).getBeregnetPrÅr().stripTrailingZeros()).isEqualTo(brutto);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP).getÅrsbeløpFraTilstøtendeYtelse().stripTrailingZeros()).isEqualTo(brutto);
    }

    @Test
    public void skalForeslåBeregningsgrunnlagForAAPMedKombinasjonsStatus() {
        //Arrange
        BigDecimal dagsats = new BigDecimal("1400");
        Inntektsgrunnlag inntektsgrunnlag = lagInntektsgrunnlag(dagsats, skjæringstidspunkt, 150);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.AAP, AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

        Periode periode = new Periode(skjæringstidspunkt, null);
        assertThat(grunnlag.getBeregningsgrunnlagPeriode()).isEqualTo(periode);

        BigDecimal brutto = BigDecimal.valueOf(273000);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.AAP, brutto.doubleValue());
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP).getBeregnetPrÅr()).isCloseTo(brutto, Offset.offset(BigDecimal.valueOf(0.001)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP).getÅrsbeløpFraTilstøtendeYtelse()).isCloseTo(brutto, Offset.offset(BigDecimal.valueOf(0.001)));
    }

    @Test
    public void skalForeslåBeregningsgrunnlagForDagpengerMedBesteberegningFødendeKvinne() {
        //Arrange
        BigDecimal beregnetDagsats = BigDecimal.valueOf(600);
        BigDecimal brutto = BigDecimal.valueOf(260000);
        Inntektsgrunnlag inntektsgrunnlag = lagInntektsgrunnlag(beregnetDagsats, skjæringstidspunkt, 150);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.DP));
        BeregningsgrunnlagPrStatus bgPrStatus = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(p -> p.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).findFirst().get();//NOSONAR
        BeregningsgrunnlagPrStatus.builder(bgPrStatus).medFastsattAvSaksbehandler(true).medBesteberegningPrÅr(brutto).build();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        assertThat(bgps.getBruttoPrÅr()).isEqualByComparingTo(brutto);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(brutto);
        assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.DP).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    public void skalForeslåBeregningsgrunnlagForDagpengerIKombinasjonSNOgMedBesteberegningFødendeKvinne() {
        //Arrange
        BigDecimal beregnetDagsats = BigDecimal.valueOf(720);
        BigDecimal brutto = BigDecimal.valueOf(240000);
        Inntektsgrunnlag inntektsgrunnlag = lagInntektsgrunnlag(beregnetDagsats, skjæringstidspunkt, 100);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.DP, AktivitetStatus.SN));
        BeregningsgrunnlagPrStatus bgPrStatus = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(p -> p.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).findFirst().get();//NOSONAR
        BeregningsgrunnlagPrStatus.builder(bgPrStatus).medFastsattAvSaksbehandler(true).medBesteberegningPrÅr(brutto).build();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        assertThat(bgps.getBruttoPrÅr()).isEqualByComparingTo(brutto);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(brutto);
        assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.DP).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    public void skalForeslåBeregningsgrunnlagForDagpengerIKombinasjonATOgMedBesteberegningFødendeKvinne() {
        //Arrange
        BigDecimal fastsattPrÅr = BigDecimal.valueOf(120000);
        BigDecimal beregnetDagsats = BigDecimal.valueOf(720);
        BigDecimal besteberegning = BigDecimal.valueOf(240000);
        Inntektsgrunnlag inntektsgrunnlag = lagInntektsgrunnlag(beregnetDagsats, skjæringstidspunkt, 100);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Arrays.asList(AktivitetStatus.DP, AktivitetStatus.ATFL), Collections.singletonList(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345")));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus dp = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus.builder(dp).medFastsattAvSaksbehandler(true).medBesteberegningPrÅr(besteberegning).build();
        BeregningsgrunnlagPrStatus atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        atfl.getArbeidsforhold().forEach(af -> BeregningsgrunnlagPrArbeidsforhold.builder(af)
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(fastsattPrÅr)
            .build());

        //Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        assertThat(bgps.getBeregnetPrÅr()).isEqualByComparingTo(besteberegning);
        assertThat(bgps.getBruttoPrÅr()).isEqualByComparingTo(besteberegning);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(besteberegning);
        assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.DP).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);

        bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        assertThat(bgps.getBeregnetPrÅr()).isEqualByComparingTo(fastsattPrÅr);
        assertThat(bgps.getBruttoPrÅr()).isEqualByComparingTo(fastsattPrÅr);
        assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }


    private Inntektsgrunnlag lagInntektsgrunnlag(BigDecimal dagsats, LocalDate skjæringstidspunkt, int utbetalingsgrad) {

        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        final BigDecimal toHundreSeksti = new BigDecimal("260");
        final BigDecimal tolv = new BigDecimal("12");
        BigDecimal månedsinntekt = dagsats.multiply(toHundreSeksti).divide(tolv, 10, RoundingMode.HALF_UP);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medMåned(skjæringstidspunkt)
            .medInntekt(månedsinntekt)
            .medUtbetalingsgrad(BigDecimal.valueOf(utbetalingsgrad))
            .build());
        return inntektsgrunnlag;
    }

}
