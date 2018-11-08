package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag;

import java.time.LocalDate;

public class BekreftetAdopsjonBarn {

    private LocalDate foedselsdato;

    BekreftetAdopsjonBarn() {
    }

    public BekreftetAdopsjonBarn(LocalDate foedselsdato) {
        this.foedselsdato = foedselsdato;
    }

    public LocalDate getFoedselsdato() {
        return foedselsdato;
    }
}
