package no.nav.foreldrepenger.inngangsvilkaar.opptjening;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;

import org.junit.Test;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Aktivitet;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsvilkårResultat;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class OpptjeningVilkårMellomliggendePerioderTest {

    private final String ARBEID = Opptjeningsvilkår.ARBEID;
    private final Aktivitet aktivitet = new Aktivitet(ARBEID, "BigCorp");

    @Test
    public void skal_anse_mellomliggende_periode_mindre_enn_angitt_maks_med_foregående_periode_lenger_enn_anngitt_min_for_godtatt() throws Exception {
        int maksMellomliggendeDager = 14;
        int minForegåendeDager = 4*7;
        
        LocalDate dt1 = LocalDate.of(2017, 10, 02);
        LocalDate dt2 = LocalDate.of(2017, 11, 07);
        LocalDate dt3 = dt2.plusDays(maksMellomliggendeDager).plusDays(1); // pluss 1 vil fortsatt gi for kort mellomliggende pga fom/tom 
        LocalDate dt4 = dt3.plusDays(1);


        // matcher antatt godkjent kun for dt3-dt4
        LocalDate behandlingstidspunkt = LocalDate.of(2018, 01, 18);
        Opptjeningsgrunnlag grunnlag = new Opptjeningsgrunnlag(behandlingstidspunkt, dt1, dt4);
        grunnlag.setMaksMellomliggendePeriodeForArbeidsforhold(Period.ofDays(maksMellomliggendeDager));
        grunnlag.setMinForegåendeForMellomliggendePeriodeForArbeidsforhold(Period.ofDays(minForegåendeDager));


        // arbeid aktivitet
        grunnlag.leggTil(new LocalDateInterval(dt1, dt2), aktivitet);
        grunnlag.leggTil(new LocalDateInterval(dt3, dt4), aktivitet);

        // sikre inntekt
        grunnlag.leggTilRapportertInntekt(new LocalDateInterval(dt1, dt4), aktivitet, 1L);

        // Act
        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        new Opptjeningsvilkår().evaluer(grunnlag, output);

        // Assert
        
        // sjekk underkjente perioder og antatt godkjent er tomme
        assertThat(output.getUnderkjentePerioder()).isEmpty();
        assertThat(output.getAntattGodkjentePerioder()).isEmpty();
        
        assertThat(output.getAkseptertMellomliggendePerioder()).containsEntry(aktivitet, new LocalDateTimeline<>(dt2.plusDays(1), dt3.minusDays(1), Boolean.TRUE));

    }

    @Test
    public void skal_anse_mellomliggende_periode_over_maks_mellomliggende_dager_med_foregående_periode_lenger_enn_min_forgående_dager_for_ikke_medregnet() throws Exception {
        int maksMellomliggendeDager = 14;
        int minForegåendeDager = 4*7;
        
        LocalDate dt1 = LocalDate.of(2017, 10, 02);
        LocalDate dt2 = LocalDate.of(2017, 11, 07);
        LocalDate dt3 = dt2.plusDays(maksMellomliggendeDager).plusDays(2); // pluss 2 kompenserer for fom/tom og gir mellomliggende 15 dager
        LocalDate dt4 = dt3.plusDays(1);

        // matcher antatt godkjent kun for dt3-dt4
        LocalDate behandlingstidspunkt = LocalDate.of(2018, 01, 18);
        Opptjeningsgrunnlag grunnlag = new Opptjeningsgrunnlag(behandlingstidspunkt, dt1, dt4);
        grunnlag.setMaksMellomliggendePeriodeForArbeidsforhold(Period.ofDays(maksMellomliggendeDager));
        grunnlag.setMinForegåendeForMellomliggendePeriodeForArbeidsforhold(Period.ofDays(minForegåendeDager));

        // arbeid aktivitet
        grunnlag.leggTil(new LocalDateInterval(dt1, dt2), aktivitet);
        grunnlag.leggTil(new LocalDateInterval(dt3, dt4), aktivitet);

        // sikre inntekt
        grunnlag.leggTilRapportertInntekt(new LocalDateInterval(dt1, dt4), aktivitet, 1L);

        // Act
        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        new Opptjeningsvilkår().evaluer(grunnlag, output);

        // Assert
        
        // sjekk underkjente perioder og antatt godkjent er tomme
        assertThat(output.getUnderkjentePerioder()).isEmpty();
        assertThat(output.getAntattGodkjentePerioder()).isEmpty();
        
        assertThat(output.getAkseptertMellomliggendePerioder()).isEmpty();

    }
}
