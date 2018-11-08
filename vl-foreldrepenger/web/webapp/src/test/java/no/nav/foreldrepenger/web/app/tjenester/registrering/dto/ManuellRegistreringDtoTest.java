package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;

public class ManuellRegistreringDtoTest {

    @Test
    public void skal_sende_behandlingId_og_FagsakId_til_abac() throws Exception {
        ManuellRegistreringDto dto = new ManuellRegistreringEngangsstonadDto();

        assertThat(dto.abacAttributter()).isEqualTo(AbacDataAttributter.opprett()
            .leggTilAksjonspunktKode(ManuellRegistreringEngangsstonadDto.AKSJONSPUNKT_KODE));
    }
}
