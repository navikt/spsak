package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.AktivPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.AktivitetStatusModell;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.RegelFastsettSkjæringstidspunkt;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class RegelFastsettSkjæringstidspunktTest {
    private static final String ARBEIDSFORHOLD = "5678";
    private final LocalDate skjæringstidspunktForOpptjening = LocalDate.of(2017, Month.DECEMBER, 5);
    private AktivitetStatusModell regelmodell;

    @Before
    public void setup() {
        regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForOpptjening(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalFastsetteSisteAktivitetsdag() throws Exception {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusWeeks(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(6), sisteAktivitetsdag.plusDays(10)), ARBEIDSFORHOLD, null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(7), sisteAktivitetsdag.minusDays(10)), ARBEIDSFORHOLD, null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(3);

        // Act
        regelmodell.justerAktiveperioder();
        LocalDate sisteDag = regelmodell.sisteAktivitetsdato();
        // Assert
        assertThat(regelmodell.getAktivePerioder()).hasSize(3);
        assertThat(sisteDag).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikOpptjening() throws Exception {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusDays(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        regelmodell.justerAktiveperioder();

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikOpptjeningForVedvarendeAktivitet() throws Exception {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), null), ARBEIDSFORHOLD, null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        regelmodell.justerAktiveperioder();

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikDagenEtterAktivitet() throws Exception {
        // Arrange
        LocalDate sisteAktivitetsdag = LocalDate.of(2017, Month.OCTOBER, 14);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        regelmodell.justerAktiveperioder();

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(sisteAktivitetsdag.plusDays(1));
    }

    @Test
    public void skalSeBortFraKortvarigMilitærAktivitet() throws Exception {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusWeeks(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForOpptjening.minusWeeks(2), skjæringstidspunktForOpptjening));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        regelmodell.justerAktiveperioder();
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(sisteAktivitetsdag.plusDays(1));
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);
        assertThat(regelmodell.getAktivePerioder().get(0).getAktivitet()).isEqualTo(Aktivitet.ARBEIDSTAKERINNTEKT);
    }

    @Test
    public void skalIkkeSeBortFraLangvarigMilitærAktivitet() throws Exception {
        // Arrange
        LocalDate sisteArbeidsdag = skjæringstidspunktForOpptjening.minusWeeks(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteArbeidsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForOpptjening.minusWeeks(8), skjæringstidspunktForOpptjening));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        regelmodell.justerAktiveperioder();
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);
    }
}
