package no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class HendelseTypeParam implements AbacDto {

    @Size(min = 0, max = 100)
    @Pattern(regexp = InputValideringRegex.FRITEKST) // TODO (humle) validering på type i kontrakten
    private final String type;

    public HendelseTypeParam(String type) {
        this.type = type;
    }

    public String get() {
        if (type.isEmpty()) {
            return null;
        }
        return type;
    }    

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett(); // tom, i praksis rollebasert tilgang på JSON-feed
    }
}
