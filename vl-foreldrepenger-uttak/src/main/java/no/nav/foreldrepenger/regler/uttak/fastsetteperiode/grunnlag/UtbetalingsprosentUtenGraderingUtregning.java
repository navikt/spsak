package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

class UtbetalingsprosentUtenGraderingUtregning implements UtbetalingsprosentUtregning {

    private static final BigDecimal HUNDRE = BigDecimal.valueOf(100L);

    private final Arbeidsprosenter arbeidsprosenter;
    private final AktivitetIdentifikator aktivitet;
    private final LukketPeriode periode;

    UtbetalingsprosentUtenGraderingUtregning(Arbeidsprosenter arbeidsprosenter,
                                             AktivitetIdentifikator aktivitet,
                                             LukketPeriode periode) {
        this.arbeidsprosenter = arbeidsprosenter;
        this.aktivitet = aktivitet;
        this.periode = periode;
    }

    @Override
    public BigDecimal resultat() {

        Optional<BigDecimal> permisjonsprosent = arbeidsprosenter.getPermisjonsprosent(aktivitet, periode);
        if (permisjonsprosent.isPresent() && permisjonsprosent.get().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal resultat = permisjonsprosent.get();
            //Sørg for at utbetalingsprosent aldri er mer en 100%.
            if (resultat.compareTo(HUNDRE) > 0) {
                return HUNDRE;
            }
            return resultat;
        }

        BigDecimal arbeidsprosent = arbeidsprosenter.getArbeidsprosent(aktivitet, periode);
        BigDecimal stillingsprosent = arbeidsprosenter.getStillingsprosent(aktivitet, periode);
        if (stillingsprosent == null) {
            throw new IllegalArgumentException("Stillingsprosent kan ikke være null");
        }
        if (arbeidsprosent == null) {
            throw new IllegalArgumentException("arbeidstidsprosent kan ikke være null");
        }
        if (stillingsprosent.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Stillingsprosent kan ikke være 0");
        }
        if (arbeidsprosent.compareTo(stillingsprosent) >= 0) {
            return BigDecimal.ZERO;
        }
        // Utbetalingsgrad (i %) = (stillingsprosent – arbeidsprosent) x 100 / stillingsprosent
        return stillingsprosent.subtract(arbeidsprosent).multiply(BigDecimal.valueOf(100)).divide(stillingsprosent, 2, RoundingMode.HALF_UP);
    }
}
