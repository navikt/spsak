package no.nav.foreldrepenger.domene.beregning.regelmodell;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import no.nav.spsak.tidsserie.LocalDateInterval;

public class UttakResultatPeriode {
    private LocalDateInterval periode;
    private List<UttakAktivitet> uttakAktiviteter;


    public UttakResultatPeriode(LocalDate fom, LocalDate tom, List<UttakAktivitet> uttakAktiviteter) {
        if (uttakAktiviteter == null || uttakAktiviteter.isEmpty()) {
            throw new IllegalStateException("Kan ikke opprette en uttakResultatperiode uten uttakAktiviteter");
        }
        this.periode = new LocalDateInterval(fom, tom);
        this.uttakAktiviteter = Collections.unmodifiableList(uttakAktiviteter);
    }

    public LocalDate getFom() {
        return periode.getFomDato();
    }

    public LocalDate getTom() {
        return periode.getTomDato();
    }

    public List<UttakAktivitet> getUttakAktiviteter() {
        return uttakAktiviteter;
    }

    public boolean inneholder(LocalDate dato) {
        return periode.encloses(dato);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof UttakResultatPeriode)) {
            return false;
        }
        UttakResultatPeriode that = (UttakResultatPeriode) obj;

        return Objects.equals(this.periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }
}
