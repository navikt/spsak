package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.util.InputValideringRegex;

abstract class VedtaksbrevOverstyringDto extends BekreftetAksjonspunktDto {

    @Size(max = 200)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String overskrift;

    @Size(max = 5000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekstBrev;

    private boolean skalBrukeOverstyrendeFritekstBrev;

    VedtaksbrevOverstyringDto() {
        // For Jackson
    }

    VedtaksbrevOverstyringDto(String begrunnelse, String overskrift, String fritekstBrev,
                              boolean skalBrukeOverstyrendeFritekstBrev) {
        super(begrunnelse);
        this.overskrift = overskrift;
        this.fritekstBrev = fritekstBrev;
        this.skalBrukeOverstyrendeFritekstBrev = skalBrukeOverstyrendeFritekstBrev;
    }

    public String getOverskrift() {
        return overskrift;
    }

    public String getFritekstBrev() {
        return fritekstBrev;
    }

    public boolean isSkalBrukeOverstyrendeFritekstBrev() {
        return skalBrukeOverstyrendeFritekstBrev;
    }
}
