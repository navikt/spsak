package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Refusjonskrav {

    private LocalDate opphørsdato;
    private BigDecimal beløp;

    public Refusjonskrav(BigDecimal beløp, LocalDate opphørsdato) {
        this.opphørsdato = opphørsdato;
        this.beløp = beløp;
    }

    public LocalDate getOpphørsdato() {
        return opphørsdato;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }
}
