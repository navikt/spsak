package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import java.math.BigDecimal;

public enum InntektPeriodeType {
    DAGLIG(BigDecimal.valueOf(260)),
    UKENTLIG(BigDecimal.valueOf(52)),
    BIUKENTLIG(BigDecimal.valueOf(26)),
    MÅNEDLIG(BigDecimal.valueOf(12)),
    ÅRLIG(BigDecimal.ONE),
    FASTSETT25PAVVIK(BigDecimal.ONE),
    PREMIEGRUNNLAG(BigDecimal.ONE);

    private BigDecimal antallPrÅr;

    InntektPeriodeType(BigDecimal antallPrÅr) {
        this.antallPrÅr = antallPrÅr;
    }

    public BigDecimal getAntallPrÅr() {
        return antallPrÅr;
    }
}
