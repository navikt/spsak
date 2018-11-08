package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(VurdereAnnenYteleseFørVedtakDto.AKSJONSPUNKT_KODE)
public class VurdereAnnenYteleseFørVedtakDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5033";

    VurdereAnnenYteleseFørVedtakDto() {
        // For Jackson
    }

    public VurdereAnnenYteleseFørVedtakDto(String begrunnelse) { // NOSONAR
        super(begrunnelse);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
