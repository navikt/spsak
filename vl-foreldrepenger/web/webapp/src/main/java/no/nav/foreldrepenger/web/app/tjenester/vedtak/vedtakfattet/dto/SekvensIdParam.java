package no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class SekvensIdParam implements AbacDto{

    @NotNull
    @Digits(integer = 1000, fraction = 0)
    @Min(0)
    private final String sekvensId;

    public SekvensIdParam(String sekvensId) {
        this.sekvensId = sekvensId;
    }
    
    public Long get() {
       return Long.valueOf(sekvensId);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett(); //tom, i praksis rollebasert tilgang på JSON-feed
    }
}
