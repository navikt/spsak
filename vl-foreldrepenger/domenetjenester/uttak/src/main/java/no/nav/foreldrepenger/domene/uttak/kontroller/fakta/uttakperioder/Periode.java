package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder;

import java.time.LocalDate;

public class Periode {

    private final LocalDate fom;
    private final LocalDate tom;

    public Periode(LocalDate fom, LocalDate tom) {
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public boolean begynnerFør(Periode otherPeriode) {
        return fom.isBefore(otherPeriode.tom);
    }
}
