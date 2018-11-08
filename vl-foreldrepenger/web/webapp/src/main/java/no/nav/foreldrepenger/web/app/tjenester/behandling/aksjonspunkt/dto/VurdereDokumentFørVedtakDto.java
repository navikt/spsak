package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(VurdereDokumentFørVedtakDto.AKSJONSPUNKT_KODE)
public class VurdereDokumentFørVedtakDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5034";

    VurdereDokumentFørVedtakDto() {
        // For Jackson
    }

    public VurdereDokumentFørVedtakDto(String begrunnelse) { // NOSONAR
        super(begrunnelse);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
