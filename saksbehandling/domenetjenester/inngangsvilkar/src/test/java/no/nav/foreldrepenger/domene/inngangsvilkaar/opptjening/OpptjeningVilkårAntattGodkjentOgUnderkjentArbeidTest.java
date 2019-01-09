package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map.Entry;

import org.junit.Test;

import no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening.Opptjeningsvilkår;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Aktivitet;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.OpptjeningsvilkårResultat;
import no.nav.spsak.tidsserie.LocalDateInterval;
import no.nav.spsak.tidsserie.LocalDateTimeline;

public class OpptjeningVilkårAntattGodkjentOgUnderkjentArbeidTest {

    private final String ARBEID = Opptjeningsvilkår.ARBEID;

    private final Aktivitet bigCorp=new Aktivitet(ARBEID, "BigCorp");
    private final Aktivitet smallCorp = new Aktivitet(ARBEID, "SmallCorp");
    private final Aktivitet noCorp = new Aktivitet(ARBEID, "NoCorp");

    @Test
    public void skal_beregne_underkjente_perioder_med_arbeid_ved_sammenligning_med_inntekt_grunnlag() throws Exception {
        LocalDate dt1 = LocalDate.of(2017, 9, 02);
        LocalDate dt2 = LocalDate.of(2017, 9, 07);
        LocalDate dt3 = LocalDate.of(2017, 10, 10);
        LocalDate dt4 = LocalDate.of(2017, 10, 15);

        LocalDate o1 = LocalDate.of(2017, 9, 03);
        LocalDate o2 = LocalDate.of(2017, 9, 11);

        // unngå antatt godkjent
        LocalDate behandlingstidspunkt = LocalDate.of(2018, 01, 18);
        Opptjeningsgrunnlag grunnlag = new Opptjeningsgrunnlag(behandlingstidspunkt, dt1, behandlingstidspunkt);

        // arbeid aktivitet
        grunnlag.leggTil(new LocalDateInterval(dt1, dt2), bigCorp);
        grunnlag.leggTil(new LocalDateInterval(dt3, dt4), bigCorp);
        grunnlag.leggTil(new LocalDateInterval(dt2, dt4), noCorp);

        // inntekt
        grunnlag.leggTilRapportertInntekt(new LocalDateInterval(dt3, dt4), bigCorp, 1L);
        grunnlag.leggTilRapportertInntekt(new LocalDateInterval(o1, o2), bigCorp, 0L);

        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        new Opptjeningsvilkår().evaluer(grunnlag, output);

        assertThat(output.getAntattGodkjentePerioder()).isEmpty();

        assertThat(output.getUnderkjentePerioder())
                .containsEntry(bigCorp, new LocalDateTimeline<>(dt1, dt2, Boolean.TRUE))
                .containsEntry(noCorp, new LocalDateTimeline<>(dt2, dt4, Boolean.TRUE));

    }

    @Test
    public void skal_beregne_antatt_godkjent_arbeid() throws Exception {
        LocalDate dt1 = LocalDate.of(2017, 11, 02);
        LocalDate dt2 = LocalDate.of(2017, 11, 07);
        LocalDate dt3 = LocalDate.of(2017, 12, 10);
        LocalDate dt4 = LocalDate.of(2017, 12, 15);

        // matcher antatt godkjent kun for dt3-dt4
        LocalDate behandlingstidspunkt = LocalDate.of(2018, 01, 01);
        Opptjeningsgrunnlag grunnlag = new Opptjeningsgrunnlag(behandlingstidspunkt, dt1, dt4);

        // arbeid aktivitet
        grunnlag.leggTil(new LocalDateInterval(dt1, dt2), bigCorp);
        grunnlag.leggTil(new LocalDateInterval(dt3, dt4), bigCorp);

        // skal også med som antatt selv om ingen inntekter er rapportert
        LocalDate førsteArbeidsdagSmallCorp = dt3.withDayOfMonth(1);
        LocalDate sisteArbeidsdagSmallCorp = dt4;
        grunnlag.leggTil(new LocalDateInterval(førsteArbeidsdagSmallCorp, sisteArbeidsdagSmallCorp), smallCorp);

        grunnlag.leggTilRapportertInntekt(new LocalDateInterval(dt1, dt3), bigCorp, 1L);

        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        new Opptjeningsvilkår().evaluer(grunnlag, output);

        assertThat(output.getUnderkjentePerioder()).isEmpty();

        assertThat(output.getAntattGodkjentePerioder())
                .containsEntry(bigCorp,
                        new LocalDateTimeline<>(dt3.plusDays(1), dt4, Boolean.TRUE))
                .containsEntry(smallCorp,
                        new LocalDateTimeline<>(førsteArbeidsdagSmallCorp, sisteArbeidsdagSmallCorp, Boolean.TRUE));

    }

    @Test
    public void skal_beregne_antatt_godkjent_over_underkjent_arbeid_der_de_overlapper() throws Exception {
        LocalDate dt1 = LocalDate.of(2017, 10, 02);
        LocalDate dt2 = LocalDate.of(2017, 10, 07);
        LocalDate dt3 = LocalDate.of(2017, 12, 10);
        LocalDate dt4 = LocalDate.of(2017, 12, 15);

        // matcher antatt godkjent kun for dt3-dt4
        LocalDate behandlingstidspunkt = LocalDate.of(2018, 01, 18);
        Opptjeningsgrunnlag grunnlag = new Opptjeningsgrunnlag(behandlingstidspunkt, dt1, behandlingstidspunkt);

        // arbeid aktivitet
        grunnlag.leggTil(new LocalDateInterval(dt1, dt2), bigCorp);
        grunnlag.leggTil(new LocalDateInterval(dt3, dt4), bigCorp);

        // skal også med som antatt selv om ingen inntekter er rapportert
        LocalDate førsteArbeidsdagSmallCorp = dt2;
        LocalDate sisteArbeidsdagSmallCorp = dt4;
        grunnlag.leggTil(new LocalDateInterval(førsteArbeidsdagSmallCorp, sisteArbeidsdagSmallCorp), smallCorp);

        // Act
        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        new Opptjeningsvilkår().evaluer(grunnlag, output);

        LocalDate førsteAntattGodkjenteDag = behandlingstidspunkt.plusMonths(1).minus(Period.ofMonths(2)).withDayOfMonth(1);

        // Assert

        // sjekk underkjente perioder
        assertThat(output.getUnderkjentePerioder())
                .containsEntry(bigCorp, new LocalDateTimeline<>(dt1, dt2, Boolean.TRUE))
                .containsEntry(smallCorp, new LocalDateTimeline<>(dt2, førsteAntattGodkjenteDag.minusDays(1), Boolean.TRUE))
                ;

        // sjekk antatt godkjente perioder
        assertThat(output.getAntattGodkjentePerioder())
                .containsEntry(bigCorp, new LocalDateTimeline<>(dt3, dt4, Boolean.TRUE))
                .containsEntry(smallCorp,
                        new LocalDateTimeline<>(førsteAntattGodkjenteDag, dt4, Boolean.TRUE));

        // sjekk at antatt og underkjent arbeid aldri overlapper
        for (Entry<Aktivitet, LocalDateTimeline<Boolean>> entry : output.getUnderkjentePerioder().entrySet()) {
            LocalDateTimeline<Boolean> other = output.getAntattGodkjentePerioder().get(entry.getKey());
            assertThat(entry.getValue().intersects(other)).as("Skal ikke intersecte for " + entry.getKey()).isFalse();
        }

    }
}
