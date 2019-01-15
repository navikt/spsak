package no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(BekreftVedtakUtenTotrinnskontrollDto.AKSJONSPUNKT_KODE)
public class BekreftVedtakUtenTotrinnskontrollDto extends VedtaksbrevOverstyringDto {

    static final String AKSJONSPUNKT_KODE = "5018";

    BekreftVedtakUtenTotrinnskontrollDto() {
        // For Jackson
    }

    public BekreftVedtakUtenTotrinnskontrollDto(String begrunnelse, String overskrift, String fritekstBrev,
                                                boolean skalBrukeOverstyrendeFritekstBrev) { // NOSONAR
        super(begrunnelse, overskrift, fritekstBrev, skalBrukeOverstyrendeFritekstBrev);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

}
