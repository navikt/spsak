package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher.EditDistanceLetter;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher.EditDistanceOperasjon;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher.WagnerFisher;

public class WagnerFisherTest {

    @Test
    public void skal_gi_0_som_kost_når_det_er_ingen_forskjell() {
        assertThat(WagnerFisher.finnKost(tilSekvens("abc"), tilSekvens("abc"))).isEqualTo(0);
        assertThat(WagnerFisher.finnKost(tilSekvens(""), tilSekvens(""))).isEqualTo(0);
    }

    @Test
    public void skal_finne_kost_ved_insert() {
        assertThat(WagnerFisher.finnKost(tilSekvens("abc"), tilSekvens("abcdef"))).isEqualTo(3 * 3);
    }

    @Test
    public void skal_finne_kost_ved_endring() {
        assertThat(WagnerFisher.finnKost(tilSekvens("aBc"), tilSekvens("abc"))).isEqualTo(1);
        assertThat(WagnerFisher.finnKost(tilSekvens("aBc"), tilSekvens("aDc"))).isEqualTo(2);
    }

    @Test
    public void skal_gi_tom_liste_når_det_ikke_er_noe_differanse() {
        List<EditDistanceOperasjon<TestLetter>> operasjoner = WagnerFisher.finnEnklesteSekvens(tilSekvens("abc"), tilSekvens("abc"));
        assertThat(operasjoner).isEmpty();
    }

    @Test
    public void skal_gi_liste_med_inserts_når_det_er_bare_nye_som_skal_legges_til() {
        List<EditDistanceOperasjon<TestLetter>> operasjoner = WagnerFisher.finnEnklesteSekvens(tilSekvens("abc"), tilSekvens("abcdef"));
        assertThat(operasjoner).containsExactly(
            settInn('d'),
            settInn('e'),
            settInn('f')
        );
    }

    @Test
    public void skal_gi_liste_med_deletes_når_det_bare_skal_slettes() {
        List<EditDistanceOperasjon<TestLetter>> operasjoner = WagnerFisher.finnEnklesteSekvens(tilSekvens("abcdef"), tilSekvens("acf"));

        assertThat(operasjoner).containsExactly(
            slette('b'),
            slette('d'),
            slette('e')
        );
    }

    @Test
    public void skal_populere_kostmatrise() throws Exception {
        int[][] kostnadsmatrise = WagnerFisher.calculateEditDistanceCost(tilSekvens("abcdef"), tilSekvens("acf"));
        int[][] forventet = {
            {0, 3, 6, 9},
            {2, 0, 3, 6},
            {4, 2, 2, 5},
            {6, 4, 2, 4},
            {8, 6, 4, 4},
            {10, 8, 6, 6},
            {12, 10, 8, 6}};
        assertThat(kostnadsmatrise).isEqualTo(forventet);
    }

    @Test
    public void skal_gi_liste_med_endringer() {
        List<EditDistanceOperasjon<TestLetter>> operasjoner = WagnerFisher.finnEnklesteSekvens(
            tilSekvens("foobar baz"),
            tilSekvens("foobaz baa"));

        assertThat(operasjoner).containsExactly(
            endre('r', 'z'),
            endre('z', 'a')
        );

    }

    @Test
    public void skal_velge_billigste_endring() {
        List<EditDistanceOperasjon<TestLetter>> operasjoner = WagnerFisher.finnEnklesteSekvens(
            tilSekvens("kitten"),
            tilSekvens("sitting"));

        assertThat(operasjoner).containsExactly(
            endre('k', 's'),
            endre('e', 'i'),
            settInn('g')
        );
    }

    static class TestLetter implements EditDistanceLetter {

        private char bokstav;

        public TestLetter(char bokstav) {
            this.bokstav = bokstav;
        }

        @Override
        public int kostnadSettInn() {
            return 3;
        }

        @Override
        public int kostnadSlette() {
            return 2;
        }

        @Override
        public boolean lik(EditDistanceLetter annen) {
            char b = ((TestLetter) annen).bokstav;
            return bokstav == b;
        }

        @Override
        public int kostnadEndre(EditDistanceLetter annen) {
            if (lik(annen)) {
                return 0;
            }
            if (Character.toUpperCase(bokstav) == Character.toUpperCase(((TestLetter) annen).bokstav)) {
                return 1;
            }
            return 2;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TestLetter)) {
                return false;
            }
            TestLetter t = (TestLetter) o;
            return bokstav == t.bokstav;
        }

        @Override
        public int hashCode() {
            return bokstav;
        }

        @Override
        public String toString() {
            return "{" +
                "bokstav=" + bokstav +
                '}';
        }
    }

    private static List<TestLetter> tilSekvens(String streng) {
        List<TestLetter> sekvens = new ArrayList<>();
        for (char c : streng.toCharArray()) {
            sekvens.add(new TestLetter(c));
        }
        return sekvens;
    }

    private static EditDistanceOperasjon<TestLetter> settInn(char c) {
        return new EditDistanceOperasjon<>(null, new TestLetter(c));
    }

    private static EditDistanceOperasjon<TestLetter> slette(char c) {
        return new EditDistanceOperasjon<>(new TestLetter(c), null);
    }

    private static EditDistanceOperasjon<TestLetter> endre(char from, char to) {
        return new EditDistanceOperasjon<>(new TestLetter(from), new TestLetter(to));
    }
}
