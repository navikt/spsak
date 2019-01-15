package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class VurderLønnsendringAndelDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long andelsnr;

    @NotNull
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer arbeidsinntekt;

    VurderLønnsendringAndelDto() { //NOSONAR
        // For Jackson
    }

    public VurderLønnsendringAndelDto(Long andelsnr, Integer arbeidsinntekt) { // NOSONAR
        this.andelsnr = andelsnr;
        this.arbeidsinntekt = arbeidsinntekt;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Integer getArbeidsinntekt() {
        return arbeidsinntekt;
    }
}
