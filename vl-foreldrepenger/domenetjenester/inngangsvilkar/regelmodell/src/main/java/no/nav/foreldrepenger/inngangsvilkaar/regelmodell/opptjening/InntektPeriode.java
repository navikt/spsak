package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening;

import java.util.Currency;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.fpsak.tidsserie.LocalDateInterval;

public class InntektPeriode {

    private static final Currency CURRENCY_NOK = Currency.getInstance("NOK");

    @JsonProperty("datoIntervall")
    private LocalDateInterval datoIntervall;

    @JsonProperty("aktivitet")
    private Aktivitet aktivitet;

    @JsonProperty("inntektBelop")
    private Long inntektBeløp;

    @JsonProperty("inntektValuta")
    private Currency inntektValuta = CURRENCY_NOK;

    @JsonCreator
    InntektPeriode() {
        // for JSON
    }
    
    public InntektPeriode(LocalDateInterval datoInterval, Aktivitet aktivitet, Long kronerInntekt) {
        this.datoIntervall = datoInterval;
        this.aktivitet = aktivitet;
        this.inntektBeløp = kronerInntekt;
    }

    public LocalDateInterval getDatoInterval() {
        return datoIntervall;
    }

    public Long getInntektBeløp() {
        return inntektBeløp;
    }

    public Currency getInntektValuta() {
        return inntektValuta;
    }

    public Aktivitet getAktivitet() {
        return aktivitet;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        InntektPeriode other = (InntektPeriode) obj;
        return Objects.equals(aktivitet, other.aktivitet)
                && Objects.equals(getDatoInterval(), other.getDatoInterval());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatoInterval(), aktivitet);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<datoIntervall=" + getDatoInterval() + ", aktivitetType=" + aktivitet + ">";
    }

}
