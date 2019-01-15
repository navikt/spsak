package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class FastsettMånedsinntektFLDto {

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer maanedsinntekt;

    FastsettMånedsinntektFLDto() {
        // For Jackson
    }

    public FastsettMånedsinntektFLDto(Integer maanedsInntekt) { // NOSONAR
        this.maanedsinntekt = maanedsInntekt;
    }

    public void setMaanedsinntekt(Integer maanedsinntekt) {
        this.maanedsinntekt = maanedsinntekt;
    }

    public Integer getMaanedsinntekt() {
        return maanedsinntekt;
    }
}
