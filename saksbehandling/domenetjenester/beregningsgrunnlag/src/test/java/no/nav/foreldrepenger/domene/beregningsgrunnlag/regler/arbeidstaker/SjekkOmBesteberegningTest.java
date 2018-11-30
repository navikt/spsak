package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker.SjekkOmBesteberegning;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmBesteberegningTest {

    private Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345");

    @Test
    public void skalReturnereNeiNårIkkeDagpengerStatus() {
        //Arrange
        Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(), Arrays.asList(AktivitetStatus.ATFL), Arrays.asList(arbeidsforhold));
        BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        Evaluation resultat = new SjekkOmBesteberegning().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalReturnereNeiNårDagpengerStatusMenIkkeBesteberegning() {
        //Arrange
        Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(), Arrays.asList(AktivitetStatus.ATFL, AktivitetStatus.DP), Arrays.asList(arbeidsforhold));
        BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        Evaluation resultat = new SjekkOmBesteberegning().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalReturnereJaNårDagpengerStatusOgBesteberegning() {
        //Arrange
        Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(), Arrays.asList(AktivitetStatus.ATFL, AktivitetStatus.DP), Arrays.asList(arbeidsforhold));
        BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus dagpengerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus.builder(dagpengerStatus).medFastsattAvSaksbehandler(true);
        BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medFastsattAvSaksbehandler(true);
        //Act
        Evaluation resultat = new SjekkOmBesteberegning().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
        assertThat(grunnlag.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    public void skalReturnereNeiNårFastsattDagpengerStatusOgIkkeFastsattATFLSTatus() { //Skal vanligvis ikke skje
        //Arrange
        Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(), Arrays.asList(AktivitetStatus.ATFL, AktivitetStatus.DP), Arrays.asList(arbeidsforhold));
        BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus dagpengerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus.builder(dagpengerStatus).medFastsattAvSaksbehandler(true);
        BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medFastsattAvSaksbehandler(false);
        //Act
        Evaluation resultat = new SjekkOmBesteberegning().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

}
