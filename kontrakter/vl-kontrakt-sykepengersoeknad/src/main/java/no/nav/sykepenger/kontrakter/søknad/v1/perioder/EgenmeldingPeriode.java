package no.nav.sykepenger.kontrakter.søknad.v1.perioder;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EgenmeldingPeriode {

    @JsonProperty("fom")
    private LocalDate fom;

    @JsonProperty("tom")
    private LocalDate tom;

    public EgenmeldingPeriode() {
    }

    public EgenmeldingPeriode(LocalDate fom, LocalDate tom) {
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

}
