package no.nav.foreldrepenger.regler.uttak.konfig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;

import org.junit.Test;

public class StandardKonfigurasjonTest {

    @Test
    public void test_standard_konfiguration() {
        assertThat(StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, LocalDate.of(2017, 12, 5))).isEqualTo(75);
        assertThat(StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT, LocalDate.of(2017, 12, 5))).isEqualTo(75);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hent_parameter_utenfor_periode_skal_gi_exception() {
        assertThat(StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, LocalDate.of(1970, 12, 5))).isEqualTo(75);
    }

    @Test(expected = IllegalArgumentException.class)
    public void skal_få_exception_ved_bruk_av_feil_typer() throws Exception {
        StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, String.class, LocalDate.now());
    }
}
