package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;

import org.junit.Test;

import no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening.Opptjeningsvilkår;
import no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening.OpptjeningsvilkårMellomregning;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Aktivitet;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.spsak.tidsserie.LocalDateInterval;

public class OpptjeningsvilkårMellomregningTest {

    @Test
    public void skal_håndtere_overlappende_perioder() {
        final Opptjeningsgrunnlag grunnlag = new Opptjeningsgrunnlag(LocalDate.now(), LocalDate.now().minusMonths(10), LocalDate.now());
        final Aktivitet aktivitet = new Aktivitet(Opptjeningsvilkår.ARBEID, "123123123", Aktivitet.ReferanseType.ORGNR);

        grunnlag.leggTil(LocalDateInterval.withPeriodAfterDate(LocalDate.now().minusMonths(8), Period.ofWeeks(6)), aktivitet);
        grunnlag.leggTil(LocalDateInterval.withPeriodAfterDate(LocalDate.now().minusMonths(7), Period.ofMonths(6)), aktivitet);
        grunnlag.leggTil(LocalDateInterval.withPeriodAfterDate(LocalDate.now().minusMonths(2), Period.ofWeeks(4)), aktivitet);

        final OpptjeningsvilkårMellomregning mellomregning = new OpptjeningsvilkårMellomregning(grunnlag);

        assertThat(mellomregning.getAktivitetTidslinjer(true, true)).isNotEmpty();
    }
}
