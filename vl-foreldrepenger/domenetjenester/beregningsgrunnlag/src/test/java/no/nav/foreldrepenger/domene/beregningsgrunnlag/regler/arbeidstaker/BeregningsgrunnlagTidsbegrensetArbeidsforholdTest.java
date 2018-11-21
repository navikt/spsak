package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlag;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.RegelmodellOversetter.getRegelResultat;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.VerifiserBeregningsgrunnlag.verifiserRegelmerknad;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class BeregningsgrunnlagTidsbegrensetArbeidsforholdTest {

    private final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

    @Test
    public void skalBeregneGrunnlagUtenInntektsmeldingN1N3MedTidsbegrensetArbeidsforhold() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(25000);
        LocalDate skjæringstidspunkt2 = LocalDate.of(2018, Month.APRIL, 26);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt2, månedsinntekt, null, true, 1, Arrays.asList(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET), false, false);

        Inntektsgrunnlag inntektsgrunnlag = beregningsgrunnlag.getInntektsgrunnlag();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt2.minusMonths(2), Arrays.asList(månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt2, månedsinntekt);

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5047");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue() * 2 / 3);
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalBeregneGrunnlagUtenInntektsmeldingN1N3MedTidsbegrensetArbeidsforholdSammenfallerMedBortfaltNaturalytelse() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(25000);
        LocalDate skjæringstidspunkt2 = LocalDate.of(2018, Month.APRIL, 26);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt2, månedsinntekt, null, true, 1, Arrays.asList(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET, PeriodeÅrsak.NATURALYTELSE_BORTFALT), false, false);

        Inntektsgrunnlag inntektsgrunnlag = beregningsgrunnlag.getInntektsgrunnlag();
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt2.minusMonths(2), Arrays.asList(månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt2, månedsinntekt);

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5047");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue() * 2 / 3);
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalGiRegelmerknadVedNullFrilansInntektSisteTreMånederOgTidsbegrensetArbeidsforhold() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.ZERO;
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal månedsinntektSammenligning = BigDecimal.valueOf(5000);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, Collections.singletonList(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektSammenligning);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5047");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalGiRegelmerknadVedNullFrilansInntektSisteTreMånederOgTidsbegrensetArbeidsforholdSammenfallendeMedNaturalytelse() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.ZERO;
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal månedsinntektSammenligning = BigDecimal.valueOf(5000);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, Arrays.asList(PeriodeÅrsak.NATURALYTELSE_BORTFALT, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektSammenligning);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation);
        assertThat(regelResultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        verifiserRegelmerknad(regelResultat, "5047");
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalGiRegelmerknadVedAvvikVedLønnsøkningOgTidsbegrensetArbeidsforhold() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(25000);
        BigDecimal refusjonskrav = månedsinntekt;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false, Collections.singletonList(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        BigDecimal månedsinntektFraSaksbehandler = BigDecimal.valueOf(35000);  // 40% avvik, dvs > 25% avvik
        opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
        BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(månedsinntektFraSaksbehandler.multiply(BigDecimal.valueOf(12)))
            .build();

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        verifiserRegelmerknad(regelResultat, "5047");

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }

    @Test
    public void skalGiRegelmerknadVedAvvikVedLønnsøkningOgTidsbegrensetArbeidsforholdSammenfallerMedBortfaltNaturalytelse() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(25000);
        BigDecimal refusjonskrav = månedsinntekt;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false, Arrays.asList(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET, PeriodeÅrsak.NATURALYTELSE_BORTFALT));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        BigDecimal månedsinntektFraSaksbehandler = BigDecimal.valueOf(35000);  // 40% avvik, dvs > 25% avvik
        opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
        BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(månedsinntektFraSaksbehandler.multiply(BigDecimal.valueOf(12)))
            .build();

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation);
        verifiserRegelmerknad(regelResultat, "5047");

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        Periode beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        BeregningsgrunnlagPrArbeidsforhold af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsperiode(af, beregningsperiode);
    }
}
