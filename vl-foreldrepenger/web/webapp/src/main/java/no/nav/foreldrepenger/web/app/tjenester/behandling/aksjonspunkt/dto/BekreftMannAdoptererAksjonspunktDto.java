package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(BekreftMannAdoptererAksjonspunktDto.AKSJONSPUNKT_KODE)
public class BekreftMannAdoptererAksjonspunktDto extends BekreftetAksjonspunktDto {

    public static final String AKSJONSPUNKT_KODE = "5006";

    @NotNull
    private Boolean mannAdoptererAlene;

    BekreftMannAdoptererAksjonspunktDto() { // NOSONAR
        //For Jackson
    }

    public BekreftMannAdoptererAksjonspunktDto(String begrunnelse, Boolean mannAdoptererAlene) { // NOSONAR
        super(begrunnelse);
        this.mannAdoptererAlene = mannAdoptererAlene;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public Boolean getMannAdoptererAlene() {
        return mannAdoptererAlene;
    }

}
