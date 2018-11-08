package no.nav.foreldrepenger.beregning.regler;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatAndel;
import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatFP;
import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatRegelmodell;
import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatRegelmodellMellomregning;
import no.nav.foreldrepenger.beregning.regelmodell.UttakAktivitet;
import no.nav.foreldrepenger.beregning.regelmodell.UttakResultat;
import no.nav.foreldrepenger.beregning.regelmodell.UttakResultatPeriode;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Periode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class FinnOverlappendeBeregningsgrunnlagOgUttaksPerioderTest {

    private final String orgnr = "123";
    private BeregningsgrunnlagPrArbeidsforhold prArbeidsforhold;
    private Arbeidsforhold arbeidsforhold;

    /*
    For eksempler brukt i testene under se https://confluence.adeo.no/display/MODNAV/27b.+Beregne+tilkjent+ytelse
     */

    @Test
    public void skal_gradere_deltiddstilling_eksempel_1() {
        // Arrange
        int redBrukersAndelPrÅr = 0;
        int redRefusjonPrÅr = 10000;
        BigDecimal stillingsgrad = BigDecimal.valueOf(50);
        int nyArbeidstidProsent = 40;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        long faktiskDagsatBruker = getDagsats(4000.0);
        long faktiskDagsatArbeidsgiver = getDagsats(2000.0);

        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(faktiskDagsatBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(faktiskDagsatArbeidsgiver);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_gradere_deltiddstilling_eksempel_2() {
        // Arrange
        int redBrukersAndelPrÅr = 1000;
        int redRefusjonPrÅr = 9000;
        BigDecimal stillingsgrad = BigDecimal.valueOf(50);
        int nyArbeidstidProsent = 40;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        long faktiskDagsatBruker = getDagsats(4200.0);
        long faktiskDagsatArbeidsgiver = getDagsats(1800.0);

        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(faktiskDagsatBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(faktiskDagsatArbeidsgiver);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_gradere_deltiddstilling_eksempel_3() {
        // Arrange
        int redBrukersAndelPrÅr = 0;
        int redRefusjonPrÅr = 100000;
        BigDecimal stillingsgrad = BigDecimal.valueOf(50);
        int nyArbeidstidProsent = 50;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        long faktiskDagsatBruker = getDagsats(50000.0);

        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(faktiskDagsatBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(0L);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_gradere_deltiddstilling_eksempel_4() {
        // Arrange
        int redBrukersAndelPrÅr = 10000;
        int redRefusjonPrÅr = 90000;
        BigDecimal stillingsgrad = BigDecimal.valueOf(50);
        int nyArbeidstidProsent = 50;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        long faktiskDagsatBruker = getDagsats(50000.0);

        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(faktiskDagsatBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(0L);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_gradere_deltiddstilling_eksempel_5() {
        // Arrange
        int redBrukersAndelPrÅr = 0;
        int redRefusjonPrÅr = 100000;
        BigDecimal stillingsgrad = BigDecimal.valueOf(50);
        int nyArbeidstidProsent = 60;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        long faktiskDagsatBruker = getDagsats(40000.0);

        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(faktiskDagsatBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(0L);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_gradere_deltiddstilling_eksempel_6() {
        // Arrange
        int redBrukersAndelPrÅr = 10000;
        int redRefusjonPrÅr = 90000;
        BigDecimal stillingsgrad = BigDecimal.valueOf(50);
        int nyArbeidstidProsent = 60;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        long faktiskDagsatBruker = getDagsats(40000.0);


        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(faktiskDagsatBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(0L);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_gradere_heltiddstilling_eksempel_7() {
        // Arrange
        int redBrukersAndelPrÅr = 0;
        int redRefusjonPrÅr = 100000;
        BigDecimal stillingsgrad = BigDecimal.valueOf(100);
        int nyArbeidstidProsent = 50;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        long faktiskDagsatArbeidsgiver = getDagsats(50000.0);


        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(0L);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(faktiskDagsatArbeidsgiver);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_gradere_heltiddstilling_eksempel_8() {
        // Arrange
        int redBrukersAndelPrÅr = 10000;
        int redRefusjonPrÅr = 90000;
        BigDecimal stillingsgrad = BigDecimal.valueOf(100);
        int nyArbeidstidProsent = 50;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        long faktiskDagsatBruker = getDagsats(5000.0);
        long faktiskDagsatArbeidsgiver = getDagsats(45000.0);


        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(faktiskDagsatBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(faktiskDagsatArbeidsgiver);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_gradere_deltiddstilling_eksempel_9() {
        // Arrange
        int redBrukersAndelPrÅr = 100000;
        int redRefusjonPrÅr = 500000;
        BigDecimal stillingsgrad = BigDecimal.valueOf(50);
        int nyArbeidstidProsent = 40;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        long faktiskDagsatBruker = getDagsats(260000.0);
        long faktiskDagsatArbeidsgiver = getDagsats(100000.0);


        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(faktiskDagsatBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(faktiskDagsatArbeidsgiver);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_ikke_gradere_fulltidsstilling_med_full_permisjon() {
        // Arrange
        int redBrukersAndelPrÅr = 100000;
        int redRefusjonPrÅr = 0;
        BigDecimal stillingsgrad = BigDecimal.valueOf(100);
        int nyArbeidstidProsent = 0;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr, stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, false);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert

        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(1);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
    }

    @Test
    public void skal_gradere_status_SN() {
        // Arrange
        BigDecimal redusertBrukersAndel = BigDecimal.valueOf(100000);
        BigDecimal stillingsgrad = BigDecimal.valueOf(100);
        int utbetalingsgrad = 50;
        long dagsatsBruker = redusertBrukersAndel.divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
        long redDagsatsBruker = getDagsats(0.50 * redusertBrukersAndel.doubleValue());
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenarioForAndreStatuser(redusertBrukersAndel, stillingsgrad, utbetalingsgrad, AktivitetStatus.SN, true);
        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(1);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(redDagsatsBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(0).getUtbetalingsgrad()).isEqualByComparingTo(BigDecimal.valueOf(utbetalingsgrad));
        assertThat(andel.get(0).getStillingsprosent()).isEqualByComparingTo(stillingsgrad);
    }

    @Test
    public void skal_gradere_status_DP() {
        // Arrange
        BigDecimal redusertBrukersAndel = BigDecimal.valueOf(100000);
        BigDecimal stillingsgrad = BigDecimal.valueOf(100);
        int utbetalingsgrad = 66;
        long dagsatsBruker = redusertBrukersAndel.divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
        long redDagsatsBruker = getDagsats(0.66 * redusertBrukersAndel.doubleValue());
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenarioForAndreStatuser(redusertBrukersAndel, stillingsgrad, utbetalingsgrad, AktivitetStatus.DP, true);
        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(1);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(redDagsatsBruker);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
    }

    @Test
    public void gradering_når_gammel_stillingsprosent_er_0() {
        // Arrange
        int redBrukersAndelPrÅr = 260000;
        int redRefusjonPrÅr = 26000;
        BigDecimal stillingsgrad = BigDecimal.ZERO;
        int nyArbeidstidProsent = 0;
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr,
            stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, true);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(dagsatsBruker+dagsatsArbeidsgiver);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(0);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }

    @Test
    public void skal_bruke_utbetalingsgrad_når_ikke_gradering() {
        // Arrange
        int redBrukersAndelPrÅr = 260000;
        int redRefusjonPrÅr = 130000;
        BigDecimal stillingsgrad = BigDecimal.ZERO;
        int nyArbeidstidProsent = 50; //Gir 50% utbetalingsgrad
        long dagsatsBruker = getDagsats(redBrukersAndelPrÅr);
        long dagsatsArbeidsgiver = getDagsats(redRefusjonPrÅr);
        BeregningsresultatRegelmodellMellomregning mellomregning = settOppGraderingScenario(redBrukersAndelPrÅr, redRefusjonPrÅr,
            stillingsgrad, nyArbeidstidProsent, dagsatsBruker, dagsatsArbeidsgiver, false);

        // Act
        FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder regel = new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder();
        Evaluation evaluation = regel.evaluate(mellomregning);
        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotNull();

        // Assert
        List<BeregningsresultatAndel> andel = mellomregning.getOutput().getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList();
        assertThat(andel).hasSize(2);
        assertThat(andel.get(0).erBrukerMottaker()).isTrue();
        assertThat(andel.get(0).getDagsats()).isEqualTo(dagsatsBruker/2);
        assertThat(andel.get(0).getDagsatsFraBg()).isEqualTo(dagsatsBruker);
        assertThat(andel.get(1).erBrukerMottaker()).isFalse();
        assertThat(andel.get(1).getDagsats()).isEqualTo(dagsatsArbeidsgiver/2);
        assertThat(andel.get(1).getDagsatsFraBg()).isEqualTo(dagsatsArbeidsgiver);
    }


    private BeregningsresultatRegelmodellMellomregning lagMellomregning(AktivitetStatus aktivitetStatus, BigDecimal stillingsgrad, BigDecimal utbetalingsgrad, BigDecimal redusertBrukersAndel, boolean erGradering) {
        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusDays(14);

        Beregningsgrunnlag grunnlag = lagBeregningsgrunnlag(fom, tom, aktivitetStatus, redusertBrukersAndel);
        UttakResultat uttakResultat = new UttakResultat(lagUttakResultatPeriode(fom, tom, stillingsgrad, utbetalingsgrad, aktivitetStatus, erGradering));
        BeregningsresultatRegelmodell input = new BeregningsresultatRegelmodell(grunnlag, uttakResultat);
        BeregningsresultatFP output = new BeregningsresultatFP();
        return new BeregningsresultatRegelmodellMellomregning(input, output);
    }

    private Set<UttakResultatPeriode> lagUttakResultatPeriode(LocalDate fom, LocalDate tom, BigDecimal stillingsgrad, BigDecimal utbetalingsgrad, AktivitetStatus aktivitetStatus, boolean erGradering) {

        List<UttakAktivitet> uttakAktiviter = Collections.singletonList(new UttakAktivitet(stillingsgrad, utbetalingsgrad, arbeidsforhold, aktivitetStatus, erGradering));
        UttakResultatPeriode periode = new UttakResultatPeriode(fom, tom, uttakAktiviter);
        Set<UttakResultatPeriode> periodeSet = new HashSet<>();
        periodeSet.add(periode);
        return periodeSet;
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(LocalDate fom, LocalDate tom, AktivitetStatus aktivitetStatus, BigDecimal redusertBrukersAndel) {

        BeregningsgrunnlagPeriode periode1 = lagPeriode(fom, tom, aktivitetStatus, redusertBrukersAndel);

        return Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(LocalDate.now())
            .medAktivitetStatuser(Collections.singletonList(aktivitetStatus))
            .medBeregningsgrunnlagPeriode(periode1)
            .build();

    }

    private BeregningsgrunnlagPeriode lagPeriode(LocalDate fom, LocalDate tom, AktivitetStatus aktivitetStatus, BigDecimal redusertBrukersAndel) {
        Periode periode = new Periode(fom, tom);
        BeregningsgrunnlagPrStatus.Builder builder = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medRedusertBrukersAndelPrÅr(redusertBrukersAndel);
        if (AktivitetStatus.ATFL.equals(aktivitetStatus)) {
            builder.medArbeidsforhold(prArbeidsforhold);
        }

        BeregningsgrunnlagPrStatus bgPrStatus = builder.build();
        return BeregningsgrunnlagPeriode.builder().medPeriode(periode).medBeregningsgrunnlagPrStatus(bgPrStatus).build();
    }

    private BeregningsresultatRegelmodellMellomregning settOppGraderingScenario(int redBrukersAndelPrÅr, int redRefusjonPrÅr, BigDecimal stillingsgrad,
                                                                                int nyArbeidstidProsent, long dagsatsBruker, Long dagsatsArbeidsgiver, boolean erGradering) {
        arbeidsforhold = Arbeidsforhold.nyttArbeidsforhold(orgnr);
        prArbeidsforhold = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(arbeidsforhold)
            .medRedusertBrukersAndelPrÅr(BigDecimal.valueOf(redBrukersAndelPrÅr))
            .medRedusertRefusjonPrÅr(BigDecimal.valueOf(redRefusjonPrÅr))
            .medDagsatsBruker(dagsatsBruker)
            .medDagsatsArbeidsgiver(dagsatsArbeidsgiver)
            .build();
        BigDecimal utbetalingsgrad = BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(nyArbeidstidProsent));
        return lagMellomregning(AktivitetStatus.ATFL, stillingsgrad, utbetalingsgrad,
            BigDecimal.valueOf(redBrukersAndelPrÅr), erGradering);
    }

    private static long getDagsats(int årsbeløp) {
        return Math.round(årsbeløp / 260.0);
    }


    private static long getDagsats(double årsbeløp) {
        return Math.round(årsbeløp / 260.0);
    }

    private BeregningsresultatRegelmodellMellomregning settOppGraderingScenarioForAndreStatuser(BigDecimal redusertBrukersAndel, BigDecimal stillingsgrad, int utbetalingsgrad,
                                                                                                AktivitetStatus aktivitetStatus, boolean erGradering) {
        return lagMellomregning(aktivitetStatus, stillingsgrad, BigDecimal.valueOf(utbetalingsgrad), redusertBrukersAndel, erGradering);
    }


}
