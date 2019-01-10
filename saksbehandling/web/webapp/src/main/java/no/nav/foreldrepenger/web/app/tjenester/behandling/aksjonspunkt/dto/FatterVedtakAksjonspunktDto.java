package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.BekreftetAksjonspunktDto;

@JsonTypeName(FatterVedtakAksjonspunktDto.AKSJONSPUNKT_KODE)
public class FatterVedtakAksjonspunktDto extends BekreftetAksjonspunktDto {
    static final String AKSJONSPUNKT_KODE = "5016";

    @Valid
    @Size(max = 10)
    private Collection<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtos;

    FatterVedtakAksjonspunktDto() {
        // For Jackson
    }

    public FatterVedtakAksjonspunktDto(String begrunnelse, Collection<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtos) {
        super(begrunnelse);
        this.aksjonspunktGodkjenningDtos = aksjonspunktGodkjenningDtos;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public Collection<AksjonspunktGodkjenningDto> getAksjonspunktGodkjenningDtos() {
        return aksjonspunktGodkjenningDtos;
    }
}
