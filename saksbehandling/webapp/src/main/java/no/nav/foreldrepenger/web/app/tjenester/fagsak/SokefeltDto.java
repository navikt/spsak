package no.nav.foreldrepenger.web.app.tjenester.fagsak;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class SokefeltDto implements AbacDto {

    @NotNull
    @Digits(integer = 18, fraction = 0)
    private String searchString;

    @SuppressWarnings("unused")
    private SokefeltDto() { // NOSONAR
    }

    public SokefeltDto(String searchString) {
        this.searchString = searchString;
    }
    
    public SokefeltDto(Saksnummer saksnummer) {
        this.searchString = saksnummer.getVerdi();
    }

    public String getSearchString() {
        return searchString;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter attributter = AbacDataAttributter.opprett();
        if (searchString.length() == 11 /* guess - fødselsnummer */) {
            attributter
                    .leggTilFødselsnummer(searchString)
                    .leggTilFnrForSøkeEtterSaker(searchString);
        } else {
            attributter.leggTilSaksnummer(searchString);
        }
        return attributter;
    }

}
