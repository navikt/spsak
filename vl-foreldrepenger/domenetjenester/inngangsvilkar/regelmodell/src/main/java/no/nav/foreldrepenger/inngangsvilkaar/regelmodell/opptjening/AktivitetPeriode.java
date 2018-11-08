package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.fpsak.tidsserie.LocalDateInterval;

/** Beskriver aktivitet for en angitt periode. */
public class AktivitetPeriode implements Comparable<AktivitetPeriode> {

    @JsonProperty("datoIntervall")
    private LocalDateInterval datoIntervall;
    
    @JsonProperty("aktivitet")
    private Aktivitet opptjeningAktivitet;

    @JsonProperty("vurderingsStatus")
    private VurderingsStatus vurderingsStatus;

    @JsonCreator
    protected AktivitetPeriode() {
    }
    
    public AktivitetPeriode(LocalDateInterval datoIntervall, Aktivitet opptjeningAktivitet, VurderingsStatus vurderingsStatus) {
        this.datoIntervall = datoIntervall;
        this.opptjeningAktivitet = opptjeningAktivitet;
        this.vurderingsStatus = vurderingsStatus;
    }

    /** Returner dag intervall. */
    public LocalDateInterval getDatoInterval() {
        return datoIntervall;
    }

    public Aktivitet getOpptjeningAktivitet() {
        return opptjeningAktivitet;
    }

    public VurderingsStatus getVurderingsStatus() {
        return vurderingsStatus;
    }

    public boolean dekkerHeleMåneder() {
        LocalDate månedStart = getFomDato().withDayOfMonth(1);
        LocalDate månedSlutt = getTomDato().withDayOfMonth(getTomDato().lengthOfMonth());
        return Objects.equals(månedStart, getFomDato()) && Objects.equals(månedSlutt, getTomDato());
    }

    public LocalDate getTomDato() {
        return datoIntervall.getTomDato();
    }

    public LocalDate getFomDato() {
        return getDatoInterval().getFomDato();
    }

    @Override
    public int compareTo(AktivitetPeriode o) {
        return this.getDatoInterval().compareTo(o.getDatoInterval());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        AktivitetPeriode other = (AktivitetPeriode) obj;
        return Objects.equals(getOpptjeningAktivitet(), other.getOpptjeningAktivitet())
                && Objects.equals(getDatoInterval(), other.getDatoInterval());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDatoInterval(), getOpptjeningAktivitet());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<datoIntervall=" + datoIntervall + ", aktivitet=" + getOpptjeningAktivitet() + ">";
    }

     public enum VurderingsStatus {
        TIL_VURDERING,
        VURDERT_GODKJENT,
        VURDERT_UNDERKJENT
    }
}