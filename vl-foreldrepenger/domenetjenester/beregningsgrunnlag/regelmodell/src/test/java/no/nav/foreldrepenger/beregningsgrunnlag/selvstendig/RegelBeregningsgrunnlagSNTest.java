package no.nav.foreldrepenger.beregningsgrunnlag.selvstendig;

import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2015;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2016;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekterForOppgittÅrene;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntektForOppgittÅrene;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.foreldrepenger.beregningsgrunnlag.RegelmodellOversetter.getRegelResultat;
import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserRegelmerknad;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class RegelBeregningsgrunnlagSNTest {

    private LocalDate skjæringstidspunkt;

    @Before
    public void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    }

    @Test
    public void skalBeregneGrunnlagSNForNormalInntektUnder6G() {
        // Arrange
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 4.0d * GRUNNBELØP_2017);
    }

    @Test
    public void skalBeregneGrunnlagSNForNormalInntektOver12G() {
        // Arrange
        //PGI >= 12Gsnitt: Bidrag til beregningsgrunnlaget = 8
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(14.0d, 15.0d, 16.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G = 8 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 8 * GRUNNBELØP_2017);
    }

    @Test
    public void skalBeregneGrunnlagSNForNormalInntektMellom6Gog12G() {
        // Arrange
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(8.0d, 9.0d, 10.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 655438);
    }

    @Test
    public void skalBeregneGrunnlagSNForSterktVarierendeInntekt() {
        // Arrange
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        //PGI >= 12Gsnitt: Bidrag til beregningsgrunnlaget = 8
        List<BigDecimal> årsinntekter = Arrays.asList(BigDecimal.valueOf(9 * GSNITT_2015), BigDecimal.valueOf(GSNITT_2016), BigDecimal.valueOf(2 * GSNITT_2017));
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt, årsinntekter, Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 312113.3333);
    }

    @Test
    public void skalBeregneAvvikVedVarigEndring() {
        // Arrange
        //PGI >= 12Gsnitt: Bidrag til beregningsgrunnlaget = 8
        List<BigDecimal> årsinntekter = Arrays.asList(BigDecimal.valueOf(12 * GSNITT_2015), BigDecimal.valueOf(12 * GSNITT_2016), BigDecimal.valueOf(12 * GSNITT_2017));
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt, årsinntekter, Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(GRUNNBELØP_2017 * 1.245)), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G = 8 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 8 * GRUNNBELØP_2017);
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        assertThat(sg).isNotNull();
        assertThat(sg.getAvvikPromille()).isEqualTo(868L);
    }

    @Test
    public void skalGiRegelmerknadVedAvvikStørreEnn25Prosent() {
        // Arrange
        List<BigDecimal> årsinntekter = Arrays.asList(BigDecimal.valueOf(7 * GSNITT_2015), BigDecimal.valueOf(8 * GSNITT_2016), BigDecimal.valueOf(9 * GSNITT_2017));
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt, årsinntekter, Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(GRUNNBELØP_2017)), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5039");
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 624226.6667);
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        assertThat(sg).isNotNull();
        assertThat(sg.getAvvikPromille()).isEqualTo(800L);
    }

    @Test
    public void skalBeregneGrunnlagSNNårBareToFerdiglignedeÅrForeligger() {
        // Arrange
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            Arrays.asList(
                BigDecimal.valueOf(2.0d * GSNITT_2016),
                BigDecimal.valueOf(4.0d * GSNITT_2017)
            ), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 2.0d * GRUNNBELØP_2017);
    }

    @Test
    public void skalBeregneGrunnlagSNiKombinasjonMedDagpenger() {
        // Arrange
        BigDecimal bruttoDP = BigDecimal.valueOf(155500);
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 6.0d, 7.0d),
            Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.SN, AktivitetStatus.DP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation);

        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        double actualBruttoSN = 540996.4434041 - bruttoDP.doubleValue() / 0.624;
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, actualBruttoSN, 540996.4434041);
    }

    @Test
    public void skalBeregneGrunnlagSNiKombinasjonMedAAP() {
        // Arrange
        BigDecimal bruttoAAP = BigDecimal.valueOf(158400);
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(30000)), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.SN, AktivitetStatus.AAP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP)).medBeregnetPrÅr(bruttoAAP).build();
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation);

        double oppjustertAAP = bruttoAAP.doubleValue() / 0.66;
        double actualBruttoSN = 4.0d * GRUNNBELØP_2017 - oppjustertAAP;
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(grunnlag.getSammenligningsGrunnlag()).isNotNull();
        int oppgittSN = 30000 * 12;
        assertThat(grunnlag.getSammenligningsGrunnlag().getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(oppgittSN+oppjustertAAP));
        assertThat(grunnlag.getSammenligningsGrunnlag().getAvvikPromille()).isEqualTo(602);
        verifiserRegelmerknad(regelResultat, "5039");
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, actualBruttoSN, 4.0d * GRUNNBELØP_2017);
    }

    @Test
    public void skalBeregneGrunnlagSNiKombinasjonMedDP() {
        // Arrange
        BigDecimal bruttoDP = BigDecimal.valueOf(118560);
        BigDecimal oppgittMånedInntektSN = BigDecimal.valueOf(20000);
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(oppgittMånedInntektSN), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.SN, AktivitetStatus.DP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation);

        double oppjustertDP = bruttoDP.doubleValue() / 0.624;
        double actualBruttoSN = 4.0d * GRUNNBELØP_2017 - oppjustertDP;
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getSammenligningsGrunnlag()).isNotNull();
        int oppgittSN = 20000 * 12;
        assertThat(grunnlag.getSammenligningsGrunnlag().getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(oppgittSN+oppjustertDP));
        assertThat(grunnlag.getSammenligningsGrunnlag().getAvvikPromille()).isEqualTo(148);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, actualBruttoSN, 4.0d * GRUNNBELØP_2017);
    }

    @Test
    public void skalBeregneGrunnlagSNmedSigrunInntekterSomEr0() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(0.0d, 0.0d, 0.0d), Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(BigDecimal.valueOf(10000)), Inntektskilde.SØKNAD, null);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat regelResultat = getRegelResultat(evaluation);


        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(grunnlag.getSammenligningsGrunnlag()).isNotNull();
        assertThat(grunnlag.getSammenligningsGrunnlag().getRapportertPrÅr().doubleValue()).isEqualTo(10000 * 12);
        assertThat(grunnlag.getSammenligningsGrunnlag().getAvvikPromille()).isEqualTo(1000);
        verifiserRegelmerknad(regelResultat, "5039");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 0, 0);
    }

    @Test
    public void skalGiRegelmerknadForSNSomErNyIArbeidslivet() {
        //Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(6d, 3d, 0.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN)).medErNyIArbeidslivet(true);

        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        RegelResultat regelResultat = getRegelResultat(evaluation);

        // Assert
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getBeregnetPrÅr()).isNull();
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getPgiListe()).hasSize(3);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getGjennomsnittligPGI()).isNotNull();
        verifiserRegelmerknad(regelResultat, "5049");
    }

    @Test
    public void skalBeregneGrunnlagNårToÅreneErFerdiglignet() {
        // Arrange
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekterForOppgittÅrene(
            årsinntektForOppgittÅrene(8d, 2017, 2015), Inntektskilde.SIGRUN, 2017, 2015);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getPgiListe()).anySatisfy(pgi -> assertThat(pgi).isEqualTo(BigDecimal.ZERO));
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 416151.111132);
    }

    @Test
    public void skalBeregneGrunnlagNårEttÅrErFerdiglignet() {
        // Arrange
        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekterForOppgittÅrene(
            årsinntektForOppgittÅrene(4d, 2016), Inntektskilde.SIGRUN, 2016);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt.minusYears(1), 3);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getPgiListe()).anySatisfy(pgi -> assertThat(pgi).isEqualTo(BigDecimal.ZERO));
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 124845.3333);
    }

    @Test
    public void skalBeregneGrunnlagNårDetFinnesIngenFerdiglignetÅr() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt.minusYears(1), 3);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getPgiListe()).anySatisfy(pgi -> assertThat(pgi).isEqualTo(BigDecimal.ZERO));
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 0d);
    }

    @Test
    public void skalBeregneGrunnlagNårDetFinnesIngenFerdiglignetÅrOgNyoppstartetSN() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.SØKNAD)
            .medMåned(skjæringstidspunkt.minusMonths(1))
            .medInntekt(BigDecimal.valueOf(250000))
            .build());
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt.minusYears(1), 3);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getPgiListe()).anySatisfy(pgi -> assertThat(pgi).isEqualTo(BigDecimal.ZERO));
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, 0d);
        assertThat(grunnlag.getSammenligningsGrunnlag().getAvvikPromille()).isEqualTo(1000);
    }

    @Test
    public void skalIkkeBeregneGrunnlagNårAlleredeFastsattAvSaksbehandler() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus status = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BeregningsgrunnlagPrStatus.builder(status).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(33333));
        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);
        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsa.getGjennomsnittligPGI()).isEqualByComparingTo(BigDecimal.valueOf(4.0d * GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getSammenligningsGrunnlag()).isNull();
        assertThat(bgpsa.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(33333));
    }
}
