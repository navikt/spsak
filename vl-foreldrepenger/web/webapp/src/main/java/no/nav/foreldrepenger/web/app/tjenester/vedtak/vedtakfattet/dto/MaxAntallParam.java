package no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class MaxAntallParam implements AbacDto{

    @Digits(integer = 1000, fraction = 0)
    @Min(1)
    @Max(1000)
    private final String maxAntall;

    public MaxAntallParam(String maxAntall) {
        this.maxAntall = maxAntall;
    }
    
    public Long get() {
        if (maxAntall.isEmpty()) {
            return 100L;
        } else {
            return Long.valueOf(maxAntall);
        }
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett(); //tom, i praksis rollebasert tilgang på JSON-feed
    }
}
