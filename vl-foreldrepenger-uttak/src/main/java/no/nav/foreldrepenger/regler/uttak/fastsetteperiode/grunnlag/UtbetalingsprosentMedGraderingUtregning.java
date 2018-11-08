package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

class UtbetalingsprosentMedGraderingUtregning implements UtbetalingsprosentUtregning {
    private final Arbeidsprosenter arbeidsprosenter;
    private final AktivitetIdentifikator aktivitet;
    private final LukketPeriode periode;

    UtbetalingsprosentMedGraderingUtregning(Arbeidsprosenter arbeidsprosenter,
                                            AktivitetIdentifikator aktivitet,
                                            LukketPeriode periode) {
        this.arbeidsprosenter = arbeidsprosenter;
        this.aktivitet = aktivitet;
        this.periode = periode;
    }

    @Override
    public BigDecimal resultat() {
        BigDecimal arbeidsprosent = arbeidsprosenter.getArbeidsprosent(aktivitet, periode);
        if (arbeidsprosent == null) {
            throw new IllegalArgumentException("arbeidstidsprosent kan ikke være null");
        }
        return new BigDecimal("100.00").subtract(arbeidsprosent);
    }
}
