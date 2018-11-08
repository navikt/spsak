package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
@JsonTypeName(SoknadsfristAksjonspunktDto.AKSJONSPUNKT_KODE)
public class SoknadsfristAksjonspunktDto extends BekreftetAksjonspunktDto implements AvslagbartAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5007";
    
    @JsonProperty("erVilkarOk")
    @NotNull
    private Boolean erVilkarOk;

    SoknadsfristAksjonspunktDto() { // NOSONAR
        //For Jackson
    }

    public SoknadsfristAksjonspunktDto(String begrunnelse, Boolean erVilkarOk) { // NOSONAR
        super(begrunnelse);
        this.erVilkarOk = erVilkarOk;
    }

    @Override
    public Boolean getErVilkarOk() {
        return erVilkarOk;
    }

    @JsonIgnore
    @Override
    public String getAvslagskode() {
        // Ikke supportert
        return null;
    }
    
    @JsonGetter
    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

}
