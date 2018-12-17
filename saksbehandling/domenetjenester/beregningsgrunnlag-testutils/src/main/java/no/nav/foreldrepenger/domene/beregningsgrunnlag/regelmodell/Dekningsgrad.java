package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell;

import java.math.BigDecimal;

public enum Dekningsgrad {
    DEKNINGSGRAD_65(new BigDecimal("0.65")),
    DEKNINGSGRAD_80(new BigDecimal("0.8")),
    DEKNINGSGRAD_100(BigDecimal.ONE);

    private BigDecimal verdi;

    Dekningsgrad(BigDecimal verdi) {
        this.verdi = verdi;
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    public static Dekningsgrad fraBigDecimal(BigDecimal verdi) {
        if (verdi.compareTo(BigDecimal.valueOf(65)) == 0) {
            return DEKNINGSGRAD_65;
        }
        if (verdi.compareTo(BigDecimal.valueOf(80)) == 0) {
            return DEKNINGSGRAD_80;
        }
        if (verdi.compareTo(BigDecimal.valueOf(100)) == 0) {
            return DEKNINGSGRAD_100;
        }
        return null;
    }

    public Long tilLong() {
        return verdi.scaleByPowerOfTen(2).longValue();
    }

    public BigDecimal tilProsentVerdi() {
        return verdi.scaleByPowerOfTen(2);
    }
}
