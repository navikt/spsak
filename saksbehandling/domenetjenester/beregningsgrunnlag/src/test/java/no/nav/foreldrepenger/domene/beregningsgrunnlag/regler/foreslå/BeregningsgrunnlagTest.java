package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.foreslå;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlag;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.settoppMånedsinntekter;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.RegelmodellOversetter.getRegelResultat;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.RegelmodellOversetter;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Periodeinntekt;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class BeregningsgrunnlagTest {

    private LocalDate skjæringstidspunkt;
    private String orgnr;
    private Arbeidsforhold arbeidsforhold;

    @Before
    public void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
        orgnr = "987654321";
        arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
    }

    @Test
    public void skalBeregneGrunnlagAGVedSammeFrilansInntektSisteTreMåneder() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, true, false);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedSammeInntektSisteTreMåneder() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false, true, false);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedKombinasjonATFLogSN() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Arrays.asList(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL_SN),
            Arrays.asList(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);
        double beløpSN = ((4.0d * GRUNNBELØP_2017) - (12 * månedsinntekt.doubleValue())); // Differanse siden SN > ATFL: SN - ATFL
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, beløpSN, 4.0d * GRUNNBELØP_2017);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, beløpSN + 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedKombinasjonATFLogSNHvorATFLStørreEnnSNMedAvkorting() {
        // ATFL > 6G, SN < ATFL: ATFL blir avkortet til 6G og SN blir satt til 0.
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 1.5);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 1.5);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
                årsinntekterFor3SisteÅr(5, 4, 6), Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Arrays.asList(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL_SN),
            Arrays.asList(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        double forventetPGI = 5.0d * GRUNNBELØP_2017;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0, forventetPGI);
    }

    @Test
    public void BeregningsgrunnlagKombinasjonATFLStørreEnnSNMedAvkorting() {
        // SN > 6G, SN > ATFL: Både ATFL og SN blir avkortet.
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 3);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 3);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
                årsinntekterFor3SisteÅr(7, 8, 6), Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Arrays.asList(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL_SN),
            Arrays.asList(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        double forventetATFL = 12 * månedsinntekt.doubleValue();
        double forventetPGI = 593015.333333;
        double forventetSN = forventetPGI - forventetATFL;
        verifiserBeregningsgrunnlagBeregnet(grunnlag, forventetATFL + forventetSN);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, forventetATFL);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, forventetSN, forventetPGI);
    }

    @Test
    public void skalBeregneGrunnlagMedInntektsmeldingMedNaturalYtelser() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(40000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(10000);
        BigDecimal naturalytelse = BigDecimal.valueOf(2000);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt, refusjonskrav, naturalytelse, naturalytelseOpphørFom);
        opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(30000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat resultat = RegelmodellOversetter.getRegelResultat(evaluation);


        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isEqualTo(BigDecimal.valueOf(24000));
        assertThat(grunnlag.getSammenligningsGrunnlag().getAvvikPromille()).isEqualTo(400);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagForTilstøtendeYtelseDagpenger() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(15500);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medMåned(skjæringstidspunkt)
            .medInntekt(månedsinntekt)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .build());
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.DP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7_8_49, AktivitetStatus.DP, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagForKombinasjonSNOgDagpenger() {
        // Arrange
        BigDecimal utbetalingsgrad = new BigDecimal("150");
        BigDecimal månedsinntekt = BigDecimal.valueOf(20000);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
                årsinntekterFor3SisteÅr(5, 5, 5), Inntektskilde.SIGRUN);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medMåned(skjæringstidspunkt)
            .medInntekt(månedsinntekt)
            .medUtbetalingsgrad(utbetalingsgrad)
            .build());
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.SN, AktivitetStatus.DP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        double expectedbruttoDP = månedsinntekt.doubleValue() * 9;
        double expectedPGIsnitt = 5.0 * GRUNNBELØP_2017;
        double expectedBruttoSN = expectedPGIsnitt - expectedbruttoDP / 0.624;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7_8_49, AktivitetStatus.DP, expectedbruttoDP);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, expectedBruttoSN, expectedPGIsnitt);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoDP + expectedBruttoSN);
    }


    @Test
    public void skalBeregneGrunnlagForKombinasjonATFL_SNOgAAP() {
        // Arrange
        BigDecimal utbetalingsgrad = new BigDecimal("100");
        BigDecimal månedsinntektAAP = BigDecimal.valueOf(15000);
        BigDecimal månedsinntektATFL = BigDecimal.valueOf(20000);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
                årsinntekterFor3SisteÅr(6, 6, 6), Inntektskilde.SIGRUN);

        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.INNTEKTSMELDING).medArbeidsgiver(arbeidsforhold)
            .medInntekt(månedsinntektATFL).medMåned(skjæringstidspunkt).build());
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP).medUtbetalingsgrad(utbetalingsgrad)
            .medInntekt(månedsinntektAAP).medMåned(skjæringstidspunkt).build());
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Arrays.asList(AktivitetStatus.ATFL_SN, AktivitetStatus.AAP), Collections.singletonList(arbeidsforhold),
            Collections.singletonList(månedsinntektATFL.multiply(BigDecimal.valueOf(12))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        double expectedbruttoAAP = månedsinntektAAP.doubleValue() * 6;
        double expectedPGIsnitt = 6.0 * GRUNNBELØP_2017;
        double expectedBruttoATFL = 12 * månedsinntektATFL.doubleValue();
        double expectedBruttoSN = expectedPGIsnitt - expectedbruttoAAP / 0.66 - expectedBruttoATFL;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.AAP, expectedbruttoAAP);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, expectedBruttoATFL);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, expectedBruttoSN, expectedPGIsnitt);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoAAP + expectedBruttoSN + expectedBruttoATFL);
    }


    @Test
    public void skalTesteNyoppstartetFrilanser() {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        opprettSammenligningsgrunnlag(inntektsgrunnlag, skjæringstidspunkt, BigDecimal.valueOf(25000));
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(Arbeidsforhold.frilansArbeidsforhold()));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(BigDecimal.valueOf(300000))
            .build();

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        LocalDate fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        assertThat(af.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
        assertThat(af.getBeregnetPrÅr().compareTo(BigDecimal.valueOf(300000))).isEqualTo(0);
    }

    @Test
    public void skalTesteArbeidsforholdInntektSattAvSaksbehandlerNårIkkeInntektsmelding() {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(arbeidsforhold));
        opprettSammenligningsgrunnlag(inntektsgrunnlag, skjæringstidspunkt, BigDecimal.valueOf(18000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(BigDecimal.valueOf(200000))
            .build();

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        LocalDate fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        assertThat(af.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
        assertThat(af.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(200000));
        assertThat(beregningsgrunnlag.getSammenligningsGrunnlag()).isNotNull();
    }

    @Test
    public void skalTesteKjøringAvTilstøtendeYtelse() {
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.TY), Collections.singletonList(arbeidsforhold));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus prStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BeregningsgrunnlagPrArbeidsforhold.builder(prStatus.getArbeidsforhold().get(0))
            .medBeregnetPrÅr(BigDecimal.valueOf(100000))
            .build();

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100000));
    }



    @Test
    public void skalTåleUkjentStatustype() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, true, false, false);
        leggtilStatus(beregningsgrunnlag, AktivitetStatus.MS);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.MS, BeregningsgrunnlagHjemmel.F_14_7);
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }


    @Test
    public void skalSetteHjemmelForTYInaktivMedSykepenger() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, true,true, true);
        leggtilStatus(beregningsgrunnlag, AktivitetStatus.TY);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.TY, BeregningsgrunnlagHjemmel.F_14_7_8_47);
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalSetteHjemmelForTYInaktivUtenSykepenger() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, true,true, false);
        leggtilStatus(beregningsgrunnlag, AktivitetStatus.TY);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.TY, BeregningsgrunnlagHjemmel.F_14_7);
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalSetteHjemmelForTYIkkeSykepenger() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, true,false, false);
        leggtilStatus(beregningsgrunnlag, AktivitetStatus.TY);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.TY, BeregningsgrunnlagHjemmel.F_14_7);
        verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalFastsetteBeregningsperiondenUtenInntektDeTreSisteMånederAT(){
        // arbeidstaker uten inntektsmelding OG det finnes ikke inntekt i de tre siste månedene
        // før skjæringstidspunktet (beregningsperioden)
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt.minusMonths(3),Arrays.asList(månedsinntekt, månedsinntekt, månedsinntekt),
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,Arrays.asList(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO),
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING,arbeidsforhold);
        Beregningsgrunnlag beregningsgrunnlag =  settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL),
            Arrays.asList(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12))));

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotBlank();
        //SÅ skal brutto beregningsgrunnlag i beregningsperioden settes til 0
        assertThat(grunnlag.getBruttoPrÅr().compareTo(BigDecimal.ZERO)).isZero();
        // skal beregningsperioden settes til de tre siste månedene før skjæringstidspunktet for beregning
        LocalDate fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0)
            .getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
    }

    private void verifiserBeregningsgrunnlagHjemmel(BeregningsgrunnlagPeriode grunnlag, AktivitetStatus aktivitetStatus,
            BeregningsgrunnlagHjemmel hjemmel) {
        assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(aktivitetStatus).getHjemmel()).isEqualTo(hjemmel);
    }

    private void leggtilStatus(Beregningsgrunnlag beregningsgrunnlag, AktivitetStatus aktivitetStatus) {
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPeriode.builder(periode)
            .medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(aktivitetStatus)
                .medAndelNr(periode.getBeregningsgrunnlagPrStatus().size() + 1L)
                .build())
            .build();
        Beregningsgrunnlag.builder(beregningsgrunnlag).medAktivitetStatuser(Arrays.asList(new AktivitetStatusMedHjemmel(aktivitetStatus, null))).build();
    }
}
