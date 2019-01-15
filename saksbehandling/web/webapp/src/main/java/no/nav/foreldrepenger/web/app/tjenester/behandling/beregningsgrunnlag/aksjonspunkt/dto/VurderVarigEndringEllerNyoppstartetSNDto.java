package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

@JsonTypeName(VurderVarigEndringEllerNyoppstartetSNDto.AKSJONSPUNKT_KODE)
public class VurderVarigEndringEllerNyoppstartetSNDto extends BekreftetAksjonspunktDto {

    public static final String AKSJONSPUNKT_KODE = "5039";
    private boolean erVarigEndretNaering;

    VurderVarigEndringEllerNyoppstartetSNDto() {
        // For Jackson
    }

    public VurderVarigEndringEllerNyoppstartetSNDto(String begrunnelse, boolean erVarigEndretNaering) {
        super(begrunnelse);
        this.erVarigEndretNaering = erVarigEndretNaering;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public boolean getErVarigEndretNaering() {
        return erVarigEndretNaering;
    }
}
