package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsettePeriodeGrunnlagImplTest {

    private LocalDate jan_01 = LocalDate.of(2018, 1, 1);
    private LocalDate jan_02 = LocalDate.of(2018, 1, 2);
    private LocalDate jan_03 = LocalDate.of(2018, 1, 3);
    private LocalDate jan_04 = LocalDate.of(2018, 1, 4);
    private UttakPeriode periode1 = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, jan_01, jan_01, false, false);
    private UttakPeriode periode2 = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, jan_02, jan_02, false, false);
    private UttakPeriode periode3 = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, jan_03, jan_03, false, false);
    private UttakPeriode periode4 = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, jan_04, jan_04, false, false);
    private UttakPeriode periode1_4 = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, jan_01, jan_04, false, false);
    private UttakPeriode periode1_3 = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, jan_01, jan_03, false, false);
    private UttakPeriode periode2_3 = new StønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, jan_02, jan_03, false, false);

    @Test
    public void skal_knekke_perioder() {
        List<UttakPeriode> knekte1 = FastsettePeriodeGrunnlagImpl.knekkPerioder(asList(periode1_4), asList(jan_01, jan_02, jan_03, jan_04));
        assertSammePerioder(knekte1, asList(periode1, periode2, periode3, periode4));

        List<UttakPeriode> knekte2 = FastsettePeriodeGrunnlagImpl.knekkPerioder(asList(periode1_4), asList(jan_04));
        assertSammePerioder(knekte2, asList(periode1_3, periode4));
    }

    @Test
    public void skal_ikke_knekke_periode_når_knekkdatoer_bare_er_før_eller_på_startdato_og_etter_sluttdato() {
        List<UttakPeriode> knekte = FastsettePeriodeGrunnlagImpl.knekkPerioder(asList(periode2_3), asList(jan_01, jan_02, jan_04));
        assertSammePerioder(knekte, Collections.singletonList(periode2_3));
    }

    @Test
    public void skal_bare_knekke_en_gang_når_samme_knekkdato_blir_med_flere_ganger() {
        List<UttakPeriode> knekte2 = FastsettePeriodeGrunnlagImpl.knekkPerioder(asList(periode1_4), asList(jan_04, jan_04, jan_04, jan_04));
        assertSammePerioder(knekte2, asList(periode1_3, periode4));
    }

    private void assertSammePerioder(List<UttakPeriode> actual, List<UttakPeriode> expected) {
        assertThat(actual.size()).isEqualTo(expected.size());
        for (int i = 0; i < actual.size(); i++) {
            UttakPeriode a = actual.get(i);
            UttakPeriode e = expected.get(i);
            assertThat(a.getFom()).isEqualTo(e.getFom());
            assertThat(a.getTom()).isEqualTo(e.getTom());
            assertThat(a.getPerioderesultattype()).isEqualTo(e.getPerioderesultattype());
            assertThat(a.getStønadskontotype()).isEqualTo(e.getStønadskontotype());
        }
    }

}
