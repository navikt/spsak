package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class InntektPrAndelDto {

    @Min(0)
    @Max(100 * 1000 * 1000)
    private Integer inntekt;

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long andelsnr;


    InntektPrAndelDto() {
        // For Jackson
    }

    public InntektPrAndelDto(Integer inntekt, Long andelsnr) {
        this.inntekt = inntekt;
        this.andelsnr = andelsnr;
    }

    public Integer getInntekt() {
        return inntekt;
    }

    public void setInntekt(Integer inntekt) {
        this.inntekt = inntekt;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }
}
