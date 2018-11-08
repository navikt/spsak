package no.nav.foreldrepenger.behandlingslager.aktør;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class NavBrukerKjønnTest {

    @Test
    public void testFraKodeKvinne() {
        final String input = "K";
        NavBrukerKjønn navBrukerKjønn = NavBrukerKjønn.fraKode(input);
        assertThat(navBrukerKjønn).isEqualTo(NavBrukerKjønn.KVINNE);
    }

    @Test
    public void testFraKodeMann() {
        final String input = "M";
        NavBrukerKjønn navBrukerKjønn = NavBrukerKjønn.fraKode(input);
        assertThat(navBrukerKjønn).isEqualTo(NavBrukerKjønn.MANN);
    }

    @Test
    public void testUgyldigFraKode() {
        final String input = "MANN";
        NavBrukerKjønn navBrukerKjønn = NavBrukerKjønn.fraKode(input);
        assertThat(navBrukerKjønn).isEqualTo(NavBrukerKjønn.UDEFINERT);
    }
}
