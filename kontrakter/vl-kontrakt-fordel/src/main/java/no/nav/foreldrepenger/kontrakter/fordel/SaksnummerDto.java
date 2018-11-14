package no.nav.foreldrepenger.kontrakter.fordel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class SaksnummerDto implements AbacDto{
    @NotNull
    @Size(max = 30)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String saksnummer;

    public SaksnummerDto(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public SaksnummerDto() { //For Jackson
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTilSaksnummer(saksnummer);
    }
}
