package no.nav.foreldrepenger.feriedager;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class BevegeligeHelligdagerUtilTest {

    @Test
    public void skal_gi_helligdagene_for_2017() {
        // 1.nyttårsdag, palmesømdag og 1.påskedag fjernes fordi de er søndager
        LocalDate skjærTorsdag = LocalDate.of(2017, 4, 13);
        LocalDate langFredag = LocalDate.of(2017, 4, 14);
        LocalDate andrePåskedag = LocalDate.of(2017, 4, 17);
        LocalDate førsteMai = LocalDate.of(2017, 5, 1);
        LocalDate syttendeMai = LocalDate.of(2017, 5, 17);
        LocalDate kristiHimmelfart = LocalDate.of(2017, 5, 25);
        LocalDate andrePinsedag = LocalDate.of(2017, 6, 5);
        LocalDate førsteJuledag = LocalDate.of(2017, 12, 25);
        LocalDate andreJuledag = LocalDate.of(2017, 12, 26);

        List<LocalDate> helligdager2018 = BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(
                new LukketPeriode(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 1)));

        assertThat(helligdager2018).hasSize(9);

        assertThat(helligdager2018.get(0)).isEqualTo(skjærTorsdag);
        assertThat(helligdager2018.get(1)).isEqualTo(langFredag);
        assertThat(helligdager2018.get(2)).isEqualTo(andrePåskedag);
        assertThat(helligdager2018.get(3)).isEqualTo(førsteMai);
        assertThat(helligdager2018.get(4)).isEqualTo(syttendeMai);
        assertThat(helligdager2018.get(5)).isEqualTo(kristiHimmelfart);
        assertThat(helligdager2018.get(6)).isEqualTo(andrePinsedag);
        assertThat(helligdager2018.get(7)).isEqualTo(førsteJuledag);
        assertThat(helligdager2018.get(8)).isEqualTo(andreJuledag);
    }

    @Test
    public void skal_gi_helligdagene_for_2018() {
        // palmesømdag og 1.påskedag fjernes fordi de er søndager

        LocalDate førsteNyttårsdag = LocalDate.of(2018, 1, 1);
        LocalDate skjærTorsdag = LocalDate.of(2018, 3, 29);
        LocalDate langFredag = LocalDate.of(2018, 3, 30);
        LocalDate andrePåskedag = LocalDate.of(2018, 4, 2);
        LocalDate førsteMai = LocalDate.of(2018, 5, 1);
        LocalDate kristiHimmelfart = LocalDate.of(2018, 5, 10);
        LocalDate syttendeMai = LocalDate.of(2018, 5, 17);
        LocalDate andrePinsedag = LocalDate.of(2018, 5, 21);
        LocalDate førsteJuledag = LocalDate.of(2018, 12, 25);
        LocalDate andreJuledag = LocalDate.of(2018, 12, 26);

        List<LocalDate> helligdager2018 = BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(
                new LukketPeriode(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1)));

        assertThat(helligdager2018).hasSize(10);

        assertThat(helligdager2018.get(0)).isEqualTo(førsteNyttårsdag);
        assertThat(helligdager2018.get(1)).isEqualTo(skjærTorsdag);
        assertThat(helligdager2018.get(2)).isEqualTo(langFredag);
        assertThat(helligdager2018.get(3)).isEqualTo(andrePåskedag);
        assertThat(helligdager2018.get(4)).isEqualTo(førsteMai);
        assertThat(helligdager2018.get(5)).isEqualTo(kristiHimmelfart);
        assertThat(helligdager2018.get(6)).isEqualTo(syttendeMai);
        assertThat(helligdager2018.get(7)).isEqualTo(andrePinsedag);
        assertThat(helligdager2018.get(8)).isEqualTo(førsteJuledag);
        assertThat(helligdager2018.get(9)).isEqualTo(andreJuledag);
    }

    @Test
    public void skal_gi_helligdagene_for_2019() {
        // palmesømdag og 1.påskedag fjernes fordi de er søndager
        LocalDate førsteNyttårsdag = LocalDate.of(2019, 1, 1);
        LocalDate skjærTorsdag = LocalDate.of(2019, 4, 18);
        LocalDate langFredag = LocalDate.of(2019, 4, 19);
        LocalDate andrePåskedag = LocalDate.of(2019, 4, 22);
        LocalDate førsteMai = LocalDate.of(2019, 5, 1);
        LocalDate syttendeMai = LocalDate.of(2019, 5, 17);
        LocalDate kristiHimmelfart = LocalDate.of(2019, 5, 30);
        LocalDate andrePinsedag = LocalDate.of(2019, 6, 10);
        LocalDate førsteJuledag = LocalDate.of(2019, 12, 25);
        LocalDate andreJuledag = LocalDate.of(2019, 12, 26);

        List<LocalDate> helligdager2018 = BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(
                new LukketPeriode(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1)));

        assertThat(helligdager2018).hasSize(10);

        assertThat(helligdager2018.get(0)).isEqualTo(førsteNyttårsdag);
        assertThat(helligdager2018.get(1)).isEqualTo(skjærTorsdag);
        assertThat(helligdager2018.get(2)).isEqualTo(langFredag);
        assertThat(helligdager2018.get(3)).isEqualTo(andrePåskedag);
        assertThat(helligdager2018.get(4)).isEqualTo(førsteMai);
        assertThat(helligdager2018.get(5)).isEqualTo(syttendeMai);
        assertThat(helligdager2018.get(6)).isEqualTo(kristiHimmelfart);
        assertThat(helligdager2018.get(7)).isEqualTo(andrePinsedag);
        assertThat(helligdager2018.get(8)).isEqualTo(førsteJuledag);
        assertThat(helligdager2018.get(9)).isEqualTo(andreJuledag);
    }
}