package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import java.time.LocalDate;

public class SoknadSykepengerDto extends SoknadDto {
    private LocalDate utstedtdato;

    public SoknadSykepengerDto() {
        super();
    }

    public LocalDate getUtstedtdato() {
        return utstedtdato;
    }

    public void setUtstedtdato(LocalDate utstedtdato) {
        this.utstedtdato = utstedtdato;
    }

}
