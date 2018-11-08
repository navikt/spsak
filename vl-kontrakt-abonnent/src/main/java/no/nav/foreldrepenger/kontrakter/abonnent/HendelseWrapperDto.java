package no.nav.foreldrepenger.kontrakter.abonnent;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class HendelseWrapperDto implements AbacDto {

    @NotNull @Valid
    private HendelseDto hendelse;

    public static HendelseWrapperDto lagDto(@Valid HendelseDto hendelse) {
        HendelseWrapperDto dto = new HendelseWrapperDto();
        dto.hendelse = hendelse;
        return dto;
    }

    public HendelseDto getHendelse() {
        return hendelse;
    }

    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett(); //rolle-basert tilgangskontroll hvor denne er brukt
    }
}
