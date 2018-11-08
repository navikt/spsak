package no.nav.foreldrepenger.web.app.tjenester.behandling.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;

public class HenleggBehandlingDtoTest {

    @Test
    public void skal_ha_med_behandlingId_til_abac() throws Exception {
        HenleggBehandlingDto dto = new HenleggBehandlingDto();
        dto.setBehandlingId(1234L);

        assertThat(dto.abacAttributter()).isEqualTo(AbacDataAttributter.opprett().leggTilBehandlingsId(1234L));
    }

}
