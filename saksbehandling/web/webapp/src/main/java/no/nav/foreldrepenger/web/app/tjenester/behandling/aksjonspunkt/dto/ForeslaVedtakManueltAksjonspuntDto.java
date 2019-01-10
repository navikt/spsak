package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.BekreftetAksjonspunktDto;

@JsonTypeName(ForeslaVedtakManueltAksjonspuntDto.AKSJONSPUNKT_KODE)
public class ForeslaVedtakManueltAksjonspuntDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5028";

    ForeslaVedtakManueltAksjonspuntDto() {
        // for jackson
    }

    public ForeslaVedtakManueltAksjonspuntDto(String begrunnelse) {
        super(begrunnelse);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
