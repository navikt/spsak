package no.nav.foreldrepenger.perioder;

import static no.nav.foreldrepenger.perioder.PerioderUtenHelgUtil.likNårHelgIgnoreres;
import static no.nav.foreldrepenger.perioder.PerioderUtenHelgUtil.periodeUtenHelgOmslutter;
import static no.nav.foreldrepenger.perioder.PerioderUtenHelgUtil.perioderUtenHelgOverlapper;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class PerioderUtenHelgUtilTest {


    private static final LocalDate SØNDAG_FØR_UKE_1 = LocalDate.of(2017, 1, 1);

    private LocalDate fredagUke1 = dag(DayOfWeek.FRIDAY, 1);
    private LocalDate lørdagUke1 = dag(DayOfWeek.SATURDAY, 1);
    private LocalDate søndagUke1 = dag(DayOfWeek.SUNDAY, 1);
    private LocalDate mandagUke2 = dag(DayOfWeek.MONDAY, 2);
    private LocalDate tirsdagUke2 = dag(DayOfWeek.TUESDAY, 2);
    private LocalDate fredagUke2 = dag(DayOfWeek.FRIDAY, 2);
    private LocalDate lørdagUke2 = dag(DayOfWeek.SATURDAY, 2);
    private LocalDate søndagUke2 = dag(DayOfWeek.SUNDAY, 2);

    @Test
    public void skal_si_at_perioder_er_like() throws Exception {
        assertThat(likNårHelgIgnoreres(tirsdagUke2, fredagUke2, tirsdagUke2, lørdagUke2)).isTrue();
        assertThat(likNårHelgIgnoreres(tirsdagUke2, fredagUke2, tirsdagUke2, søndagUke2)).isTrue();
        assertThat(likNårHelgIgnoreres(søndagUke1, tirsdagUke2, mandagUke2, tirsdagUke2)).isTrue();
        assertThat(likNårHelgIgnoreres(lørdagUke1, tirsdagUke2, mandagUke2, tirsdagUke2)).isTrue();
        assertThat(likNårHelgIgnoreres(mandagUke2, tirsdagUke2, mandagUke2, tirsdagUke2)).isTrue();

        assertThat(likNårHelgIgnoreres(lørdagUke1, lørdagUke1, søndagUke1, søndagUke1)).isTrue();
        assertThat(likNårHelgIgnoreres(lørdagUke1, søndagUke1, lørdagUke1, søndagUke1)).isTrue();
        assertThat(likNårHelgIgnoreres(lørdagUke1, søndagUke2, mandagUke2, fredagUke2)).isTrue();
    }

    @Test
    public void skal_si_at_perioder_er_ulike() throws Exception {
        assertThat(likNårHelgIgnoreres(fredagUke1, fredagUke2, lørdagUke2, fredagUke2)).isFalse();
        assertThat(likNårHelgIgnoreres(fredagUke1, mandagUke2, fredagUke1, fredagUke1)).isFalse();

        assertThat(likNårHelgIgnoreres(lørdagUke1, søndagUke1, lørdagUke2, søndagUke2)).isFalse();
    }

    @Test
    public void skal_si_at_periode_omslutter_den_andre() throws Exception {
        assertThat(periodeUtenHelgOmslutter(new LukketPeriode(mandagUke2, fredagUke2), new LukketPeriode(lørdagUke1, søndagUke2))).isTrue();
        assertThat(periodeUtenHelgOmslutter(new LukketPeriode(mandagUke2, fredagUke2), new LukketPeriode(lørdagUke2, søndagUke2))).isTrue();
        assertThat(periodeUtenHelgOmslutter(new LukketPeriode(mandagUke2, fredagUke2), new LukketPeriode(lørdagUke1, søndagUke1))).isTrue();
        assertThat(periodeUtenHelgOmslutter(new LukketPeriode(lørdagUke1, søndagUke1), new LukketPeriode(lørdagUke1, søndagUke1))).isTrue();
    }

    @Test
    public void skal_si_at_periode_ikke_omslutter_den_andre_nå_andre_periode_er_lenger() throws Exception {
        assertThat(periodeUtenHelgOmslutter(new LukketPeriode(mandagUke2, fredagUke2), new LukketPeriode(fredagUke1, søndagUke2))).isFalse();
        assertThat(periodeUtenHelgOmslutter(new LukketPeriode(mandagUke2, tirsdagUke2), new LukketPeriode(mandagUke2, søndagUke2))).isFalse();
    }

    @Test
    public void skal_si_at_helg1_ikke_omslutter_helg2() throws Exception {
        assertThat(periodeUtenHelgOmslutter(new LukketPeriode(lørdagUke1, søndagUke1), new LukketPeriode(lørdagUke2, søndagUke2))).isFalse();
    }

    @Test
    public void skal_si_at_perioder_overlapper() throws Exception {
        assertThat(perioderUtenHelgOverlapper(new LukketPeriode(mandagUke2, fredagUke2), new LukketPeriode(fredagUke1, søndagUke2))).isTrue();
    }

    @Test
    public void skal_si_at_helg_ikke_overlapper() throws Exception {
        assertThat(perioderUtenHelgOverlapper(new LukketPeriode(fredagUke1, søndagUke1), new LukketPeriode(lørdagUke1, søndagUke2))).isFalse();
        assertThat(perioderUtenHelgOverlapper(new LukketPeriode(fredagUke1, søndagUke1), new LukketPeriode(lørdagUke1, søndagUke1))).isFalse();
    }

    private LocalDate dag(DayOfWeek dag, int ukenr) {
        return SØNDAG_FØR_UKE_1.plusWeeks(ukenr - 1).plusDays(dag.getValue());
    }

}