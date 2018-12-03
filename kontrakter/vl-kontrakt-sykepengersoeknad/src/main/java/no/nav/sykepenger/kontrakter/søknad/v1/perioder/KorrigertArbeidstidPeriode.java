package no.nav.sykepenger.kontrakter.søknad.v1.perioder;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KorrigertArbeidstidPeriode {

    @JsonProperty("fom")
    private LocalDate fom;

    @JsonProperty("tom")
    private LocalDate tom;

    @JsonProperty("faktiskGrad")
    private Integer faktiskGrad;

    @JsonProperty("faktiskTimer")
    private Integer faktiskTimer;

    @JsonProperty("avtaltTimer")
    private Integer avtaltTimer;

    public KorrigertArbeidstidPeriode() {
    }

    public KorrigertArbeidstidPeriode(LocalDate fom, LocalDate tom, Integer faktiskGrad, Integer faktiskTimer, Integer avtaltTimer) {
        this.fom = fom;
        this.tom = tom;
        this.faktiskGrad = faktiskGrad;
        this.faktiskTimer = faktiskTimer;
        this.avtaltTimer = avtaltTimer;
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

    public Integer getFaktiskGrad() {
        return faktiskGrad;
    }

    public void setFaktiskGrad(Integer faktiskGrad) {
        this.faktiskGrad = faktiskGrad;
    }

    public Integer getFaktiskTimer() {
        return faktiskTimer;
    }

    public void setFaktiskTimer(Integer faktiskTimer) {
        this.faktiskTimer = faktiskTimer;
    }

    public Integer getAvtaltTimer() {
        return avtaltTimer;
    }

    public void setAvtaltTimer(Integer avtaltTimer) {
        this.avtaltTimer = avtaltTimer;
    }
}
