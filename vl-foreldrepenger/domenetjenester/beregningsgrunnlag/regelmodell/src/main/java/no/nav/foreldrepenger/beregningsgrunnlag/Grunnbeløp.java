package no.nav.foreldrepenger.beregningsgrunnlag;

import java.time.LocalDate;

public class Grunnbeløp {
    private LocalDate fom;
    private LocalDate tom;
    private Long gSnitt;
    private Long gVerdi;

    public Grunnbeløp(LocalDate fom, LocalDate tom, Long gVerdi, Long gSnitt) {
        this.fom = fom;
        this.tom = tom;
        this.gVerdi = gVerdi;
        this.gSnitt = gSnitt;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public Long getGSnitt() {
        return gSnitt;
    }

    public Long getGVerdi() {
        return gVerdi;
    }
}
