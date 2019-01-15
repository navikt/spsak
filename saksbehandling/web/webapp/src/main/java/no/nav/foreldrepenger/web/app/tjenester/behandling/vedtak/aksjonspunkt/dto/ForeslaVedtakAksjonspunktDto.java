package no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(ForeslaVedtakAksjonspunktDto.AKSJONSPUNKT_KODE)
public class ForeslaVedtakAksjonspunktDto extends VedtaksbrevOverstyringDto {

    public static final String AKSJONSPUNKT_KODE = "5015";

    ForeslaVedtakAksjonspunktDto() {
        // for jackson
    }

    public ForeslaVedtakAksjonspunktDto(String begrunnelse, String overskrift, String fritekst,
                                        boolean skalBrukeOverstyrendeFritekstBrev) {
        super(begrunnelse, overskrift, fritekst, skalBrukeOverstyrendeFritekstBrev);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
