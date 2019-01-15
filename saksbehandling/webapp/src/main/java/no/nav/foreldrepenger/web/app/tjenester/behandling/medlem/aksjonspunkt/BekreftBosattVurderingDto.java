package no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.aksjonspunkt;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

@JsonTypeName(BekreftBosattVurderingDto.AKSJONSPUNKT_KODE)
public class BekreftBosattVurderingDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5020";

    @NotNull
    private Boolean bosattVurdering;


    BekreftBosattVurderingDto() { // NOSONAR
        // For Jackson
    }

    public BekreftBosattVurderingDto(String begrunnelse, Boolean bosattVurdering) { // NOSONAR
        super(begrunnelse);
        this.bosattVurdering = bosattVurdering;
    }


    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public Boolean getBosattVurdering() {
        return bosattVurdering;
    }
}
