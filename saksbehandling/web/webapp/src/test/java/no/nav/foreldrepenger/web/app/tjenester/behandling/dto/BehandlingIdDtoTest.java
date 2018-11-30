package no.nav.foreldrepenger.web.app.tjenester.behandling.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;

public class BehandlingIdDtoTest {

    @Test
    public void skal_sende_id_til_abac() throws Exception {
        BehandlingIdDto dto = new BehandlingIdDto(1337L);

        assertThat(dto.abacAttributter()).isEqualTo(AbacDataAttributter.opprett().leggTilBehandlingsId(1337L));
    }
}
