package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto;

import java.math.BigDecimal;

public class ATogFLISammeOrganisasjonDto extends FaktaOmBeregningAndelDto {

    private BigDecimal inntektPrMnd;

    public BigDecimal getInntektPrMnd() {
        return inntektPrMnd;
    }

    public void setInntektPrMnd(BigDecimal inntektPrMnd) {
        this.inntektPrMnd = inntektPrMnd;
    }
}
