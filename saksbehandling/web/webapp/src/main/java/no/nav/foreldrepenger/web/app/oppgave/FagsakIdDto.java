package no.nav.foreldrepenger.web.app.oppgave;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

import javax.validation.constraints.Digits;

public class FagsakIdDto implements AbacDto {

    @Digits(integer = 18, fraction = 0)
    private String verdi;

    public FagsakIdDto(String verdi) {
        this.verdi = verdi;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilFagsakId(getVerdi());
    }

    public Long getVerdi() {
        return Long.parseLong(verdi);
    }
}
