package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.AktivPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.AktivitetStatusModell;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.status.RegelFastsettStatusVedSkjæringstidspunkt;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class RegelFastsettStatusVedSkjæringtidspunktTest {
    private static final String ARBEIDSFORHOLD = "7654";
    private LocalDate skjæringstidspunktForBeregning;
    private AktivitetStatusModell regelmodell;

    @Before
    public void setup() {
        skjæringstidspunktForBeregning = LocalDate.of(2018, Month.JANUARY, 15);
        regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForBeregning(skjæringstidspunktForBeregning);
    }

    @Test
    public void skalFastsetteStatusDPNårAktivitetErDagpengerMottaker(){
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forAndre(Aktivitet.DAGPENGEMOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusWeeks(4), skjæringstidspunktForBeregning.plusWeeks(2)));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);

        // Act
        Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.DP);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.DP);
    }

    @Test
    public void skalFastsetteStatusATFLNårAktivitetErArbeidsinntektOgSykepengerOpphørtToDagerFørSP(){
        // Arrange
        AktivPeriode aktivPeriode = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)), Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ARBEIDSFORHOLD, null));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(2), skjæringstidspunktForBeregning.minusDays(2)));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.ATFL);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        BeregningsgrunnlagPrStatus bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatus.getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatus.getArbeidsforholdList().get(0).getOrgnr()).isEqualTo(ARBEIDSFORHOLD);
    }

    @Test
    public void skalFastsetteStatusTYNårAktivitetErArbeidsinntektOgSykepengerOpphørt1DagFørSP(){
        // Arrange
        AktivPeriode aktivPeriode = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)), Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ARBEIDSFORHOLD, null));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(2), skjæringstidspunktForBeregning.minusDays(1)));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.TY);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        BeregningsgrunnlagPrStatus bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatus.getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatus.getArbeidsforholdList().get(0).getOrgnr()).isEqualTo(ARBEIDSFORHOLD);
    }

    @Test
    public void skalFastsetteStatusTYNårAktivitetErSvangerskapspenger(){
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forAndre(Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(1), skjæringstidspunktForBeregning.plusWeeks(3)));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);

        // Act
        Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.TY);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).isEmpty();
    }

    @Test
    public void skalFastsetteStatusTYNårKombinasjonerAvAktivitetErArbeidsinntektOgSykepenger(){
        // Arrange
        AktivPeriode aktivPeriode = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)), Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ARBEIDSFORHOLD, null));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.SYKEPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(1), skjæringstidspunktForBeregning.plusWeeks(1)));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).hasSize(1);
        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.TY);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        BeregningsgrunnlagPrStatus bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatus.getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatus.getArbeidsforholdList().get(0).getOrgnr()).isEqualTo(ARBEIDSFORHOLD);
    }

    @Test
    public void skalFastsetteStatusTYOgAAPVedKombinasjonerAvDisseAktiviteter(){
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forAndre(Aktivitet.AAP_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.FORELDREPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusMonths(1), skjæringstidspunktForBeregning.plusWeeks(1)));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).hasSize(1);
        assertThat(regelmodell.getAktivitetStatuser()).containsExactlyInAnyOrder(AktivitetStatus.TY);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        BeregningsgrunnlagPrStatus bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.AAP);
    }

    @Test
    public void skalFastsetteStatusTYOgDPVedKombinasjonerAvDisseToStatuser(){
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forAndre(Aktivitet.DAGPENGEMOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusDays(1), skjæringstidspunktForBeregning.plusWeeks(3)));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.PLEIEPENGER_MOTTAKER, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusDays(3)));
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).hasSize(1);
        assertThat(regelmodell.getAktivitetStatuser()).containsExactlyInAnyOrder(AktivitetStatus.TY);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        BeregningsgrunnlagPrStatus bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.DP);
    }

    @Test
    public void skalFastsetteStatusATFL_SNVedKombinasjonerAvAktivitetFrilanserOgNæringsinntekt(){
        // Arrange
        AktivPeriode aktivPeriode = new AktivPeriode(Aktivitet.FRILANSINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusDays(3), skjæringstidspunktForBeregning.plusWeeks(2)), Arbeidsforhold.frilansArbeidsforhold());
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = new AktivPeriode(Aktivitet.NÆRINGSINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(1), skjæringstidspunktForBeregning.plusDays(3)), null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.ATFL_SN);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(2);
        List<BeregningsgrunnlagPrStatus> bgPrStatuser = regelmodell.getBeregningsgrunnlagPrStatusListe();
        assertThat(bgPrStatuser.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatuser.get(0).getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatuser.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
        assertThat(bgPrStatuser.get(1).getArbeidsforholdList()).isEmpty();
    }

    @Test
    public void skalFastsetteStatusATFL_SNogDPVedKombinasjonerAvAktivitetArbeidsinntektNæringsinntektogMilitær(){
        // Arrange
        AktivPeriode aktivPeriode = new AktivPeriode(Aktivitet.FRILANSINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusDays(3), skjæringstidspunktForBeregning.plusWeeks(2)), Arbeidsforhold.frilansArbeidsforhold());
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = new AktivPeriode(Aktivitet.NÆRINGSINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(1), skjæringstidspunktForBeregning.plusDays(3)), null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        aktivPeriode = new AktivPeriode(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForBeregning.minusWeeks(4), skjæringstidspunktForBeregning.plusDays(5)), null);
        regelmodell.leggTilAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(3);

        // Act
        Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).containsExactlyInAnyOrder(AktivitetStatus.ATFL_SN, AktivitetStatus.MS);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(3);
        List<BeregningsgrunnlagPrStatus> bgPrStatuser = regelmodell.getBeregningsgrunnlagPrStatusListe();
        assertThat(bgPrStatuser.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatuser.get(0).getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatuser.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
        assertThat(bgPrStatuser.get(1).getArbeidsforholdList()).isEmpty();
        assertThat(bgPrStatuser.get(2).getAktivitetStatus()).isEqualTo(AktivitetStatus.MS);
        assertThat(bgPrStatuser.get(2).getArbeidsforholdList()).isEmpty();
    }

}
