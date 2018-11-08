package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(BekreftEktefelleAksjonspunktDto.AKSJONSPUNKT_KODE)
public class BekreftEktefelleAksjonspunktDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5005";

    @NotNull
    private Boolean ektefellesBarn;

    BekreftEktefelleAksjonspunktDto() { // NOSONAR
        //For Jackson
    }

    public BekreftEktefelleAksjonspunktDto(String begrunnelse, Boolean ektefellesBarn) { // NOSONAR
        super(begrunnelse);
        this.ektefellesBarn = ektefellesBarn;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public Boolean getEktefellesBarn() {
        return ektefellesBarn;
    }

}
