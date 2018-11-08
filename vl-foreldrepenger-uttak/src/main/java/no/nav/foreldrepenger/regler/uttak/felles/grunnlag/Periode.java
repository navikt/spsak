package no.nav.foreldrepenger.regler.uttak.felles.grunnlag;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.fpsak.tidsserie.LocalDateInterval;

public class Periode {

    private final LocalDate fom;
    private final LocalDate tom;

    @JsonIgnore
    private LocalDateInterval datoIntervall;

    public Periode(LocalDate fom, LocalDate tom) {
        if (fom != null && tom != null && tom.isBefore(fom)) {
            throw new IllegalArgumentException("Til og med dato før fra og med dato: " + fom + ">" + tom);
        }
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public LocalDateInterval tilDatoIntervall() {
        if (datoIntervall == null) {
            datoIntervall = new LocalDateInterval(fom, tom);
        }
        return datoIntervall;
    }

    public boolean overlapper(LocalDate dato) {
        return tilDatoIntervall().encloses(dato);
    }

    public boolean overlapper(LukketPeriode periode) {
        return tilDatoIntervall().overlaps(periode.tilDatoIntervall());
    }

    public boolean erOmsluttetAv(LukketPeriode periode) {
        return periode.tilDatoIntervall().contains(tilDatoIntervall());
    }


}
