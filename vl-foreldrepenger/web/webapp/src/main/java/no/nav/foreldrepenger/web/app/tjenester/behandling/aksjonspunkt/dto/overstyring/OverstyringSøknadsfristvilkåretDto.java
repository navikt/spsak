package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
@JsonTypeName(OverstyringSøknadsfristvilkåretDto.AKSJONSPUNKT_KODE)
public class OverstyringSøknadsfristvilkåretDto extends OverstyringAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "6006";

    @JsonProperty("erVilkarOk")
    private boolean erVilkarOk;

    @SuppressWarnings("unused") // NOSONAR
    private OverstyringSøknadsfristvilkåretDto() {
        super();
        // For Jackson
    }

    public OverstyringSøknadsfristvilkåretDto(boolean erVilkarOk, String begrunnelse) { // NOSONAR
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
