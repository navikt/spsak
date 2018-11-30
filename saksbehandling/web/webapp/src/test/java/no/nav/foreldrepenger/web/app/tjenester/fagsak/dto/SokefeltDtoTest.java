package no.nav.foreldrepenger.web.app.tjenester.fagsak.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;

public class SokefeltDtoTest {

    @Test
    public void skal_ha_spesial_abac_type_når_det_er_et_fødslelsnummer_siden_alle_sakene_kan_være_knyttet_til_andre_parter() throws Exception {
        String fnr = "07078518434";
        SokefeltDto dto = new SokefeltDto(fnr);

        assertThat(dto.abacAttributter()).isEqualTo(AbacDataAttributter.opprett()
            .leggTilFødselsnummer(fnr)
            .leggTilFnrForSøkeEtterSaker(fnr)
        );
    }

    @Test
    public void skal_ha_normal_saksnummer_abac_type_når_det_ikke_er_et_fødslelsnummer() throws Exception {
        Saksnummer saksnummer  = new Saksnummer("123123123123");
        SokefeltDto dto = new SokefeltDto(saksnummer);

        assertThat(dto.abacAttributter()).isEqualTo(AbacDataAttributter.opprett().leggTilSaksnummer(saksnummer.getVerdi()));
    }
}
