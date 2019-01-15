package no.nav.foreldrepenger.behandling.rest;

import no.nav.foreldrepenger.behandling.rest.SaksnummerDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SaksnummerDtoTest {

    @Test
    public void skal_ha_med_saksnummer_til_abac() throws Exception {
        SaksnummerDto dto = new SaksnummerDto("1234");

        assertThat(dto.abacAttributter()).isEqualTo(AbacDataAttributter.opprett().leggTilSaksnummer("1234"));
    }
}
