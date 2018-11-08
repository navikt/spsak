package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class FastsatteAndelerTidsbegrensetDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long andelsnr;
    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Integer bruttoFastsattInntekt;

    FastsatteAndelerTidsbegrensetDto() { // NOSONAR
        // Jackson
    }

    public FastsatteAndelerTidsbegrensetDto(Long andelsnr,
                                            Integer bruttoFastsattInntekt) {
        this.andelsnr = andelsnr;
        this.bruttoFastsattInntekt = bruttoFastsattInntekt;
    }
    public Long getAndelsnr() { return andelsnr; }

    public Integer getBruttoFastsattInntekt() {
        return bruttoFastsattInntekt;
    }

}
