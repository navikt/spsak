package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad.aksjonspunkt;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
@JsonTypeName(SoknadsfristAksjonspunktDto.AKSJONSPUNKT_KODE)
public class SoknadsfristAksjonspunktDto extends BekreftetAksjonspunktDto {

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

    public Boolean getErVilkarOk() {
        return erVilkarOk;
    }

    @JsonGetter
    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

}
