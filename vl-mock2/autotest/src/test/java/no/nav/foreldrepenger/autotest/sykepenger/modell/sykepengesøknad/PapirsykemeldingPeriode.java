package no.nav.foreldrepenger.autotest.sykepenger.modell.sykepengesøknad;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PapirsykemeldingPeriode {

    @JsonProperty
    private LocalDate fom;

    @JsonProperty
    private LocalDate tom;

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
