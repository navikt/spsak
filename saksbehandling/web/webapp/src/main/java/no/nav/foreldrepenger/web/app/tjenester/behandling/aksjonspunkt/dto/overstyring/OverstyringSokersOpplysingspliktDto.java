package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
@JsonTypeName(OverstyringSokersOpplysingspliktDto.AKSJONSPUNKT_KODE)
public class OverstyringSokersOpplysingspliktDto extends OverstyringAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "6002";

    @JsonProperty("erVilkarOk")
    private boolean erVilkarOk;

    @SuppressWarnings("unused") // NOSONAR
    private OverstyringSokersOpplysingspliktDto() {
        super();
        // For Jackson
    }

    public OverstyringSokersOpplysingspliktDto(boolean erVilkarOk, String begrunnelse) { // NOSONAR
        super(begrunnelse);
        this.erVilkarOk = erVilkarOk;
    }

    @JsonGetter
    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    @JsonIgnore
    @Override
    public String getAvslagskode() {
        return null;
    }

    @Override
    public boolean getErVilkarOk() {
        return erVilkarOk;
    }

}
