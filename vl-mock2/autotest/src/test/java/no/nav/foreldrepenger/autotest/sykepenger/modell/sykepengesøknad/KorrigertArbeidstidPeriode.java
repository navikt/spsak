package no.nav.foreldrepenger.autotest.sykepenger.modell.sykepengesøknad;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KorrigertArbeidstidPeriode {

    @JsonProperty
    private LocalDate fom;

    @JsonProperty
    private LocalDate tom;

    @JsonProperty
    private Integer faktiskGrad;

    @JsonProperty
    private Integer faktiskTimer;

    @JsonProperty
    private Integer avtaltTimer;

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
