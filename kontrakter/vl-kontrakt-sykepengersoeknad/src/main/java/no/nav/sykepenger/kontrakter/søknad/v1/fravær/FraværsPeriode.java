package no.nav.sykepenger.kontrakter.søknad.v1.fravær;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FraværsPeriode {

    @JsonProperty("fom")
    private LocalDate fom;

    @JsonProperty("tom")
    private LocalDate tom;

    @JsonProperty("type")
    private FraværType type;

    public FraværsPeriode() {
    }

    public FraværsPeriode(LocalDate fom, LocalDate tom, FraværType type) {
        this.fom = fom;
        this.tom = tom;
        this.type = type;
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

    public FraværType getType() {
        return type;
    }

    public void setType(FraværType type) {
        this.type = type;
    }
}
