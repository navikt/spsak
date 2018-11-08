package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.vedtak.util.InputValideringRegex;

@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE, setterVisibility= JsonAutoDetect.Visibility.NONE, fieldVisibility= JsonAutoDetect.Visibility.ANY)
@JsonTypeName(OverstyringFødselvilkåretFarMedmorDto.AKSJONSPUNKT_KODE)
public class OverstyringFødselvilkåretFarMedmorDto extends OverstyringAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "6009";

    @JsonProperty("avslagskode")
    @Size(min = 4, max = 4)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String avslagskode;

    @JsonProperty("erVilkarOk")
    private boolean erVilkarOk;

    @SuppressWarnings("unused") // NOSONAR
    private OverstyringFødselvilkåretFarMedmorDto() {
        super();
        // For Jackson
    }

    public OverstyringFødselvilkåretFarMedmorDto(boolean erVilkarOk, String begrunnelse, String avslagskode) { // NOSONAR
        super(begrunnelse);
        this.erVilkarOk = erVilkarOk;
        this.avslagskode = avslagskode;
    }
    @Override
    public String getAvslagskode() {
        return avslagskode;
    }

    @Override
    public boolean getErVilkarOk() {
        return erVilkarOk;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
