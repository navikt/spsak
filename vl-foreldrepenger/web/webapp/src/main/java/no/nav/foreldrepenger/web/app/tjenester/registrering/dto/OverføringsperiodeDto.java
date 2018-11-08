package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

public class OverføringsperiodeDto {

    @NotNull
    private LocalDate fomDato;

    @NotNull
    private LocalDate tomDato;

    @NotNull
    @ValidKodeverk
    private OverføringÅrsak overforingArsak;

    public OverføringsperiodeDto() {
    }

    public LocalDate getFomDato() {
        return fomDato;
    }

    public void setFomDato(LocalDate fomDato) {
        this.fomDato = fomDato;
    }

    public LocalDate getTomDato() {
        return tomDato;
    }

    public void setTomDato(LocalDate tomDato) {
        this.tomDato = tomDato;
    }

    public OverføringÅrsak getOverforingArsak() {
        return overforingArsak;
    }

    public void setOverforingArsak(OverføringÅrsak overforingArsak) {
        this.overforingArsak = overforingArsak;
    }
}
