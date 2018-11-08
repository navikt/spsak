package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;

public final class TrekkdagerUtregningUtil {

    private TrekkdagerUtregningUtil() {
    }

    public static int trekkdagerFor(Periode periode,
                                    boolean gradert,
                                    boolean trekkDager,
                                    BigDecimal gradertArbeidstidsprosent,
                                    boolean manuellBehandling) {
        int trekkdagerUtenGradering = Virkedager.beregnAntallVirkedager(periode);
        if (manuellBehandling) {
            return trekkdagerUtenGradering;
        }
        if (trekkDager) {
            if (gradert) {
                return trekkdagerMedGradering(trekkdagerUtenGradering, gradertArbeidstidsprosent);
            } else {
                return trekkdagerUtenGradering;
            }
        } else {
            return 0;
        }
    }

    private static int trekkdagerMedGradering(int trekkdagerUtenGradering, BigDecimal gradertArbeidstidsprosent) {
        if (gradertArbeidstidsprosent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return 0;
        }
        return (BigDecimal.valueOf(trekkdagerUtenGradering).multiply(BigDecimal.valueOf(100).subtract(gradertArbeidstidsprosent)))
                .divide(BigDecimal.valueOf(100), 0, BigDecimal.ROUND_FLOOR)
                .intValue();
    }

}
