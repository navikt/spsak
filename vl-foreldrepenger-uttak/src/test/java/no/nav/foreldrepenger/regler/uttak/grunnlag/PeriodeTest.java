package no.nav.foreldrepenger.regler.uttak.grunnlag;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;


public class PeriodeTest {

    @Test(expected = NullPointerException.class)
    public void input_lik_null_er_ikke_lov() {
        Periode testPeriode = new Periode(LocalDate.of(2016, 1, 1), LocalDate.of(2018, 1, 1));

        testPeriode.overlapper((LocalDate) null);
    }

    @Test
    public void periode_med_start_og_slutt_og_dato_utenfor_skal_ikke_overlappe() {
        Periode testPeriode = new Periode(LocalDate.of(2016, 1, 1), LocalDate.of(2018, 1, 1));

        assertThat(testPeriode.overlapper(LocalDate.of(2019, 1, 1))).isFalse();
    }


    @Test
    public void periode_uten_start_og_slutt_skal_overlappe() {
        Periode testPeriode = new Periode(null, null);

        assertThat(testPeriode.overlapper(LocalDate.of(2017, 1, 1))).isTrue();
    }


    @Test
    public void periode_med_start_og_slutt_skal_overlappe() {
        Periode testPeriode = new Periode(LocalDate.of(2016, 1, 1), LocalDate.of(2018, 1, 1));

        assertThat(testPeriode.overlapper(LocalDate.of(2017, 1, 1))).isTrue();
    }

    @Test
    public void periode_med_bare_start_skal_overlappe() {
        Periode testPeriode = new Periode(LocalDate.of(2016, 1, 1), null);

        assertThat(testPeriode.overlapper(LocalDate.of(2017, 1, 1))).isTrue();
    }

    @Test
    public void periode_med_bare_start_og_dato_før_start_skal_ikke_overlappe() {
        Periode testPeriode = new Periode(LocalDate.of(2016, 1, 1), null);

        assertThat(testPeriode.overlapper(LocalDate.of(2015, 1, 1))).isFalse();
    }

    @Test
    public void helePeriodenOverlapper() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(2);
        LukketPeriode periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(fom.minusDays(1), tom.plusDays(1)))).isTrue();
        assertThat(periode.overlapper(new LukketPeriode(fom.plusDays(1), tom.minusDays(1)))).isTrue();
        assertThat(periode.overlapper(periode)).isTrue();
    }

    @Test
    public void begynnelsenAvPeriodenOverlapper() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(2);
        LukketPeriode periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(fom.minusDays(1), tom.minusDays(1)))).isTrue();
        assertThat(periode.overlapper(new LukketPeriode(fom, fom))).isTrue();
        assertThat(periode.overlapper(new LukketPeriode(fom.minusDays(1), fom))).isTrue();
    }

    @Test
    public void sluttenAvPeriodenOverlapper() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(2);
        LukketPeriode periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(fom.plusDays(1), tom.plusDays(1)))).isTrue();
        assertThat(periode.overlapper(new LukketPeriode(tom, tom))).isTrue();
        assertThat(periode.overlapper(new LukketPeriode(tom, tom.plusDays(1)))).isTrue();
    }

    @Test
    public void periodenRettFørOverlapperIkke() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(2);
        LukketPeriode periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(fom.minusDays(10), fom.minusDays(1)))).isFalse();
    }

    @Test
    public void periodenRettEtterOverlapperIkke() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(2);
        LukketPeriode periode = new LukketPeriode(fom, tom);

        assertThat(periode.overlapper(new LukketPeriode(tom.plusDays(1), tom.plusDays(5)))).isFalse();
    }


}
