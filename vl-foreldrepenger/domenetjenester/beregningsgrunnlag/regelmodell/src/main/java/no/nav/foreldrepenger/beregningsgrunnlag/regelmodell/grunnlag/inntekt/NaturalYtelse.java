package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class NaturalYtelse {

    private BigDecimal beløp;
    private LocalDate fom;
    private LocalDate tom;

    public NaturalYtelse(BigDecimal beløp, LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(beløp, "Beløp må være satt for naturalytelse");
        if (fom == null && tom == null) {
            throw new IllegalArgumentException("Enten fom eller tom må være satt for naturalytelse");
        }
        this.beløp = beløp;
        this.fom = fom;
        this.tom = tom;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }
}
