package no.nav.foreldrepenger.behandlingslager.geografisk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LandkoderTest {

    @Test
    public void skal_sjekke_for_norge() {
        boolean swe = Landkoder.erNorge("SWE");
        boolean nor = Landkoder.erNorge("NOR");

        assertThat(swe).isFalse();
        assertThat(nor).isTrue();
    }
}
