package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;

import org.junit.Test;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Aktivitet;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.OpptjeningsvilkårResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;
import no.nav.spsak.tidsserie.LocalDateInterval;
import no.nav.spsak.tidsserie.LocalDateTimeline;

public class OpptjeningVilkårBeregnetOpptjeningTest {

    private final String ARBEID = Opptjeningsvilkår.ARBEID;
    private final Aktivitet aktivitet = new Aktivitet(ARBEID, "BigCorp");

    @Test
    public void skal_få_ikke_godkjent_for_beregnet_opptjening_med_mellomliggende_periode_og_for_kort_varighet() throws Exception {
        int maksMellomliggendeDager = 14;
        int minForegåendeDager = 4 * 7;

        LocalDate dt1 = LocalDate.of(2017, 11, 1);
        LocalDate dt2 = LocalDate.of(2017, 11, 20);
        LocalDate endOfInntekt = dt1.plusMonths(2).minusDays(1);


        // matcher antatt godkjent kun for dt3-dt4
        LocalDate behandlingstidspunkt = LocalDate.of(2018, 01, 18);
        Opptjeningsgrunnlag grunnlag = new Opptjeningsgrunnlag(behandlingstidspunkt, dt1, dt2);
        grunnlag.setMaksMellomliggendePeriodeForArbeidsforhold(Period.ofDays(maksMellomliggendeDager));
        grunnlag.setMinForegåendeForMellomliggendePeriodeForArbeidsforhold(Period.ofDays(minForegåendeDager));


        // arbeid aktivitet
        grunnlag.leggTil(new LocalDateInterval(dt1, dt2), aktivitet);

        // sikre inntekt i oktober, og november
        grunnlag.leggTilRapportertInntekt(new LocalDateInterval(dt1, endOfInntekt), aktivitet, 1L);

        // Act
        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        Evaluation evaluation = new Opptjeningsvilkår().evaluer(grunnlag, output);

        // Assert

        // sjekk underkjente perioder og antatt godkjent er tomme
        assertThat(output.getUnderkjentePerioder()).isEmpty();

        assertThat(output.getResultatTidslinje()).isEqualTo(new LocalDateTimeline<>(dt1, dt2, Boolean.TRUE));
        assertThat(output.getResultatOpptjent()).isEqualTo(Period.parse("P20D"));

        Resultat forventet = Resultat.NEI;
        assertForventetResultat(evaluation, forventet);
    }

    private void assertForventetResultat(Evaluation evaluation, Resultat forventet) {
        EvaluationSummary summary = new EvaluationSummary(evaluation);
        Collection<Evaluation> total = summary.leafEvaluations();
        assertThat(total).hasSize(1);
        assertThat(total.stream().map(Evaluation::result)).containsOnly(forventet);
    }

    @Test
    public void skal_få_oppfylt_når_bekreftet_opptjening_er_mer_enn_28_dager() throws Exception {
        int maksMellomliggendeDager = 14;
        int minForegåendeDager = 4 * 7;

        LocalDate behandlingstidspunkt = LocalDate.of(2018, 01, 18);
        LocalDate dt1 = LocalDate.of(2017, 7, 1);
        LocalDate dt2 = LocalDate.of(2017, 7, 30);

        LocalDate endOfInntekt = behandlingstidspunkt.minusMonths(1);


        // matcher antatt godkjent kun for dt3-dt4
        Opptjeningsgrunnlag grunnlag = new Opptjeningsgrunnlag(behandlingstidspunkt, dt1, behandlingstidspunkt);
        grunnlag.setMaksMellomliggendePeriodeForArbeidsforhold(Period.ofDays(maksMellomliggendeDager));
        grunnlag.setMinForegåendeForMellomliggendePeriodeForArbeidsforhold(Period.ofDays(minForegåendeDager));


        // arbeid aktivitet
        grunnlag.leggTil(new LocalDateInterval(dt1, dt2), aktivitet);

        // sikre inntekt i oktober, og november
        grunnlag.leggTilRapportertInntekt(new LocalDateInterval(dt1, endOfInntekt), aktivitet, 1L);

        // Act
        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        Evaluation evaluation = new Opptjeningsvilkår().evaluer(grunnlag, output);

        Resultat forventet = Resultat.JA;
        assertForventetResultat(evaluation, forventet);

        // Assert

        // sjekk underkjente perioder og antatt godkjent er tomme
        assertThat(output.getUnderkjentePerioder()).isEmpty();
        assertThat(output.getBekreftetGodkjentePerioder()).hasSize(1).containsEntry(aktivitet, new LocalDateTimeline<>(dt1, dt2, Boolean.TRUE));

        assertThat(output.getResultatOpptjent()).isEqualTo(Period.parse("P1M4D"));
    }

    @Test
    public void skal_få_ikke_oppfylt_når_bekreftet_opptjening_er_mindre_enn_28_dager() throws Exception {
        int maksMellomliggendeDager = 14;
        int minForegåendeDager = 4 * 7;

        LocalDate behandlingstidspunkt = LocalDate.of(2018, 01, 18);
        // datoer tunet for å gi akkurat 4 måneder opptjening
        LocalDate dt1 = LocalDate.of(2017, 11, 7);
        LocalDate dt2 = LocalDate.of(2017, 11, 20);
        LocalDate dt3 = dt2.plusDays(maksMellomliggendeDager / 2);
        LocalDate dt4 = dt3.plusDays(5);

        // dato tunet for å sikre at en måned kan settes som antatt opptjent (men fortsatt ikke nok til å gi Ok)
        LocalDate endOfInntekt = behandlingstidspunkt.minusMonths(1);


        // matcher antatt godkjent kun for dt3-dt4
        Opptjeningsgrunnlag grunnlag = new Opptjeningsgrunnlag(behandlingstidspunkt, dt1, behandlingstidspunkt);
        grunnlag.setMaksMellomliggendePeriodeForArbeidsforhold(Period.ofDays(maksMellomliggendeDager));
        grunnlag.setMinForegåendeForMellomliggendePeriodeForArbeidsforhold(Period.ofDays(minForegåendeDager));


        // arbeid aktivitet
        grunnlag.leggTil(new LocalDateInterval(dt1, dt2), aktivitet);
        grunnlag.leggTil(new LocalDateInterval(dt3, dt4), aktivitet);

        // sikre inntekt i oktober, og november
        grunnlag.leggTilRapportertInntekt(new LocalDateInterval(dt1, endOfInntekt), aktivitet, 1L);

        // Act
        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        Evaluation evaluation = new Opptjeningsvilkår().evaluer(grunnlag, output);

        Resultat forventet = Resultat.NEI;
        assertForventetResultat(evaluation, forventet);

        // Assert

        // sjekk underkjente perioder og antatt godkjent er tomme
        assertThat(output.getUnderkjentePerioder()).isEmpty();
        //assertThat(output.getUnderkjentePerioder()).containsEntry(aktivitet, new LocalDateTimeline<>(behandlingstidspunkt.plusDays(1), dt4, Boolean.TRUE));

        assertThat(output.getAntattGodkjentePerioder()).hasSize(0);


        assertThat(output.getAkseptertMellomliggendePerioder()).hasSize(0);

        assertThat(output.getResultatOpptjent()).isEqualTo(Period.ofDays(20));

    }

}
