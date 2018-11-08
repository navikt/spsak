package no.nav.foreldrepenger.beregningsgrunnlag.kombinasjon;

import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.foreldrepenger.beregningsgrunnlag.RegelmodellOversetter.getRegelResultat;
import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNTest {

    private LocalDate skjæringstidspunkt;
    private Arbeidsforhold arbeidsforhold;
    private Arbeidsforhold arbeidsforhold2;
    private BigDecimal Gverdi;
    private static final BigDecimal TOLV = new BigDecimal("12");

    @Before
    public void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
        arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123");
        arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("frilans");
        Gverdi = BigDecimal.valueOf(GRUNNBELØP_2017);
    }

    @Test
    public void skalBeregneBeregningsgrunnlagForKombinasjonUtenVarigEndringOppgitt() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 3, 4),
            Inntektskilde.SIGRUN);

        BigDecimal inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
        Evaluation evaluation = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);

        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        BigDecimal beløp = Gverdi.multiply(new BigDecimal("4"));
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, inntektATFL.multiply(TOLV), beløp.subtract(TOLV.multiply(inntektATFL)));
    }

    @Test
    public void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringOppgittUnder25ProsentAvvik() {

        // Arrange
        BigDecimal sigrun = new BigDecimal("5").multiply(Gverdi);
        BigDecimal inntektATFL = new BigDecimal("18000");
        BigDecimal soknadInntekt = new BigDecimal("30760");
        BigDecimal sammenligningsgrunnlag = inntektATFL.add(soknadInntekt).multiply(TOLV);
        //Avvik = 24.98%
        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();


        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
        Evaluation evaluation = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, inntektATFL.multiply(TOLV), sigrun.subtract(TOLV.multiply(inntektATFL)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

    }

    @Test
    public void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringOppgittOver25ProsentAvvik() {

        // Arrange
        BigDecimal sigrun = new BigDecimal("5").multiply(Gverdi);
        BigDecimal inntektATFL = new BigDecimal("80000");
        BigDecimal soknadInntekt = new BigDecimal("25770");
        BigDecimal sammenligningsgrunnlag = inntektATFL.add(soknadInntekt).multiply(TOLV);
        //Avvik = 25.01%
        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();


        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.ZERO));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
        Evaluation evaluation = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);

        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5039");


        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.01)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * inntektATFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 5d * GRUNNBELØP_2017);
    }

    @Test
    public void skalBeregneBeregningsgrunnlagForKombinasjonMedNaturalYtelserBortfalt() {
        // Arrange
        BigDecimal sigrun = new BigDecimal("5").multiply(Gverdi);
        BigDecimal inntektATFL = new BigDecimal("80000");
        BigDecimal soknadInntekt = new BigDecimal("25770");
        BigDecimal naturalytelseBeløp = new BigDecimal("5000");
        BigDecimal sammenligningsgrunnlag = inntektATFL.add(soknadInntekt).add(naturalytelseBeløp).multiply(TOLV);

        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        List<NaturalYtelse> naturalYtelser = Collections.singletonList(new NaturalYtelse(naturalytelseBeløp, null, skjæringstidspunkt));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medMåned(skjæringstidspunkt)
            .medInntekt(inntektATFL)
            .medNaturalYtelser(naturalYtelser)
            .build());

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.ZERO));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
        Evaluation evaluation = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);

        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5039");


        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isCloseTo(naturalytelseBeløp.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * inntektATFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 5d * GRUNNBELØP_2017);
    }

    @Test
    public void skalBeregneBeregningsgrunnlagForKombinasjonMedNaturalYtelserBortfaltMedFlerePerioder() {
        // Arrange
        BigDecimal sigrun = new BigDecimal("5").multiply(Gverdi);
        BigDecimal inntektATFL = new BigDecimal("80000");
        BigDecimal soknadInntekt = new BigDecimal("25770");
        BigDecimal naturalytelseBeløp = new BigDecimal("5000");
        BigDecimal sammenligningsgrunnlag = inntektATFL.add(soknadInntekt).add(naturalytelseBeløp).multiply(TOLV);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt.plusMonths(4);

        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        List<NaturalYtelse> naturalYtelser = Arrays.asList(new NaturalYtelse(naturalytelseBeløp, null, skjæringstidspunkt),
            new NaturalYtelse(naturalytelseBeløp, null, skjæringstidspunkt.plusMonths(4)));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medMåned(skjæringstidspunkt)
            .medInntekt(inntektATFL)
            .medNaturalYtelser(naturalYtelser)
            .build());

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.ZERO));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        BeregningsgrunnlagPeriode andrePeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(naturalytelseOpphørFom, null))
            .build();

        kopierBeregningsgrunnlagPeriode(grunnlag, andrePeriode);

        Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medBeregningsgrunnlagPeriode(andrePeriode)
            .build();

        // Act
        Evaluation evaluation1 = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);
        Evaluation evaluation2 = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(andrePeriode).evaluer(andrePeriode);

        // Assert

        RegelResultat regelResultat1 = getRegelResultat(evaluation1);
        RegelResultat regelResultat2 = getRegelResultat(evaluation2);
        assertThat(regelResultat1.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat1.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5039");


        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isCloseTo(naturalytelseBeløp.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * inntektATFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 5d * GRUNNBELØP_2017);

        //Verifiser andre periode
        assertThat(andrePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isCloseTo(naturalytelseBeløp.multiply(TOLV).multiply(BigDecimal.valueOf(2)), within(BigDecimal.valueOf(0.001)));
        verifiserSammenligningsgrunnlag(andrePeriode, sammenligningsgrunnlag, avvik);
        assertThat(regelResultat2.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
    }

    @Test
    public void skalBeregneBeregningsgrunnlagForKombinasjonMedDagpenger() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 3, 4),
            Inntektskilde.SIGRUN);
        BigDecimal inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL_SN, AktivitetStatus.DP),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BigDecimal bruttoDP = BigDecimal.valueOf(100000);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();

        // Act
        Evaluation evaluation = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        BigDecimal gjennomsnittligPGI = Gverdi.multiply(new BigDecimal("4"));
        BigDecimal beløpATFL = inntektATFL.multiply(TOLV);
        BigDecimal oppjusterBruttoDP = BigDecimal.valueOf(100000 / 0.624);
        BigDecimal beløpSN = gjennomsnittligPGI.subtract(beløpATFL).subtract(oppjusterBruttoDP);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, beløpATFL, beløpSN);
    }

    @Test
    public void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringMedAAP() {
        // Arrange
        BigDecimal sigrun = BigDecimal.valueOf(540996.444434);
        BigDecimal inntektATFL = new BigDecimal("18000");
        BigDecimal soknadInntekt = new BigDecimal("50000");
        BigDecimal bruttoAAP = BigDecimal.valueOf(49500);
        BigDecimal oppjustertAAP = BigDecimal.valueOf(49500/0.66);
        BigDecimal sammenligningsgrunnlag = inntektATFL.add(soknadInntekt).multiply(TOLV).add(oppjustertAAP);
        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 6, 7),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL_SN, AktivitetStatus.AAP),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017).multiply(BigDecimal.valueOf(12)))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP)).medBeregnetPrÅr(bruttoAAP).build();

        // Act
        Evaluation evaluation = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        BigDecimal beløpATFL = inntektATFL.multiply(TOLV);
        BigDecimal oppjusterBruttoAAP = bruttoAAP.divide(BigDecimal.valueOf(0.66), 10, RoundingMode.HALF_UP);
        BigDecimal beløpSN = sigrun.subtract(beløpATFL).subtract(oppjusterBruttoAAP);
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, beløpATFL, beløpSN);
    }

    @Test
    public void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringMedDP() {
        // Arrange
        BigDecimal sigrun = BigDecimal.valueOf(655438);
        BigDecimal inntektATFL = new BigDecimal("18000");
        BigDecimal soknadInntekt = new BigDecimal("35000");
        BigDecimal bruttoDP = BigDecimal.valueOf(74880);
        BigDecimal oppjustertDP = BigDecimal.valueOf(74880/0.624);
        BigDecimal sammenligningsgrunnlag = inntektATFL.add(soknadInntekt).multiply(TOLV).add(oppjustertDP);
        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(8, 9, 10),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL_SN, AktivitetStatus.DP),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017).multiply(BigDecimal.valueOf(12)))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();

        // Act
        Evaluation evaluation = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        BigDecimal beløpATFL = inntektATFL.multiply(TOLV);
        BigDecimal beløpSN = sigrun.subtract(beløpATFL).subtract(oppjustertDP);
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, beløpATFL, beløpSN);
    }

    @Test
    public void skalGiRegelmerknadForSNSomErNyIArbeidslivet() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(6, 3, 0),
            Inntektskilde.SIGRUN);
        BigDecimal inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Collections.singletonList(AktivitetStatus.ATFL_SN), Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017).multiply(BigDecimal.valueOf(12)))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN)).medErNyIArbeidslivet(true);

        // Act
        Evaluation evaluation = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.getMerknader().get(0).getMerknadKode()).isEqualTo("5049");
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.01)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getGjennomsnittligPGI()).isNull();
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getPgiListe()).hasSize(3);
    }

    @Test
    public void skalIkkeBeregneSNNårFastsattAvSaksbehandler() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt, årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);
        BigDecimal inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(inntektATFL), Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold statusAT = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrStatus statusSN = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BeregningsgrunnlagPrArbeidsforhold.builder(statusAT).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(232323));
        BeregningsgrunnlagPrStatus.builder(statusSN).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(323232));

        // Act
        Evaluation evaluation = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, BigDecimal.valueOf(232323), BigDecimal.valueOf(323232));
    }


    private void verifiserBeregningsgrunnlagBruttoATFL_SN(BeregningsgrunnlagPeriode grunnlag, BigDecimal beløpATFL, BigDecimal beløpSN) {
        BeregningsgrunnlagPrStatus bgpsaATFL = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        assertThat(bgpsaATFL).isNotNull();
        assertThat(bgpsaATFL.getBeregnetPrÅr()).isCloseTo(beløpATFL, within(BigDecimal.valueOf(0.01)));

        BeregningsgrunnlagPrStatus bgpsaSN = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsaSN.getBeregnetPrÅr()).isCloseTo(beløpSN, within(BigDecimal.valueOf(0.01)));
    }

    private void verifiserSammenligningsgrunnlag(BeregningsgrunnlagPeriode grunnlag, BigDecimal beløp, BigDecimal prosent) {
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        assertThat(sg).isNotNull();
        assertThat(sg.getRapportertPrÅr()).isCloseTo(beløp, within(BigDecimal.valueOf(0.01)));
        assertThat(sg.getAvvikProsent()).isCloseTo(prosent, within(BigDecimal.valueOf(0.01)));
        assertThat(sg.getAvvikPromille()).isEqualTo(prosent.movePointRight(1).setScale(0, RoundingMode.HALF_UP).longValue());
    }

    private void kopierBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagPeriode kopi) {
        for (BeregningsgrunnlagPrStatus forrigeStatus : grunnlag.getBeregningsgrunnlagPrStatus()) {
            if (forrigeStatus.erArbeidstakerEllerFrilanser()) {
                BeregningsgrunnlagPrStatus ny = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(forrigeStatus.getAktivitetStatus())
                    .medBeregningsgrunnlagPeriode(kopi)
                    .build();
                for (BeregningsgrunnlagPrArbeidsforhold kopierFraArbeidsforhold : forrigeStatus.getArbeidsforhold()) {
                    BeregningsgrunnlagPrArbeidsforhold kopiertArbeidsforhold = BeregningsgrunnlagPrArbeidsforhold.builder()
                        .medArbeidsforhold(kopierFraArbeidsforhold.getArbeidsforhold())
                        .medAndelNr(kopierFraArbeidsforhold.getAndelNr())
                        .build();
                    ny.getArbeidsforhold().add(kopiertArbeidsforhold);
                }
            } else {
                BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(forrigeStatus.getAktivitetStatus())
                    .medBeregnetPrÅr(forrigeStatus.getBeregnetPrÅr())
                    .medBeregningsgrunnlagPeriode(kopi)
                    .medAndelNr(forrigeStatus.getAndelNr())
                    .build();
            }
        }
    }
}
