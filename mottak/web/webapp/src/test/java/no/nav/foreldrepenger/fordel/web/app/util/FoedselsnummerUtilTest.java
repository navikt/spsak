package no.nav.foreldrepenger.fordel.web.app.util;

import org.junit.Test;

import no.nav.foreldrepenger.fordel.web.app.util.FoedselsnummerUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class FoedselsnummerUtilTest {

    @Test
    public void gyldigFoedselsnummer(){
        String foedselsnummer = "07078518434";
        boolean gyldig = FoedselsnummerUtil.gyldigFoedselsnummer(foedselsnummer);
        assertThat(gyldig).isEqualTo(true);
    }

    @Test
    public void ugyldigFoedselsnummer() {
        String foedselsnummer = "31048518434";
        boolean gyldig = FoedselsnummerUtil.gyldigFoedselsnummer(foedselsnummer);
        assertThat(gyldig).isEqualTo(false);

        foedselsnummer = "9999999999";
        gyldig = FoedselsnummerUtil.gyldigFoedselsnummer(foedselsnummer);
        assertThat(gyldig).isEqualTo(false);
    }
}
