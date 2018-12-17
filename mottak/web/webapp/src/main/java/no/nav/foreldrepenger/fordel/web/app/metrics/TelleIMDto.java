package no.nav.foreldrepenger.fordel.web.app.metrics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Resultatet av IM-telling")
public class TelleIMDto {

    @ApiModelProperty(value = "Antall inntektsmeldinger til Gosys")
    private Long gosys;
    @ApiModelProperty(value = "Antall inntektsmeldinger til Fpsak")
    private Long fpsak;


    public TelleIMDto() { // NOSONAR Input-dto, ingen behov for initialisering
    }

    public TelleIMDto(Long gosys, Long fpsak) {
        this.gosys = gosys;
        this.fpsak = fpsak;
    }

    public Long getGosys() {
        return gosys;
    }

    public void setGosys(Long gosys) {
        this.gosys = gosys;
    }

    public Long getFpsak() {
        return fpsak;
    }

    public void setFpsak(Long fpsak) {
        this.fpsak = fpsak;
    }
}