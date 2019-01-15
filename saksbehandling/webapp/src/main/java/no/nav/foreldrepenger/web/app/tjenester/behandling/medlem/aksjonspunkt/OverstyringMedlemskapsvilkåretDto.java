package no.nav.foreldrepenger.web.app.tjenester.behandling.medlem.aksjonspunkt;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.OverstyringAksjonspunktDto;
import no.nav.vedtak.util.InputValideringRegex;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
@JsonTypeName(OverstyringMedlemskapsvilkåretDto.AKSJONSPUNKT_KODE)
public class OverstyringMedlemskapsvilkåretDto extends OverstyringAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "6005";

    @JsonProperty("avslagskode")
    @Size(min = 4, max = 4)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String avslagskode;

    @JsonProperty("erVilkarOk")
    private boolean erVilkarOk;

    @SuppressWarnings("unused") // NOSONAR
    private OverstyringMedlemskapsvilkåretDto() {
        super();
        // For Jackson
    }

    public OverstyringMedlemskapsvilkåretDto(boolean erVilkarOk, String begrunnelse, String avslagskode) { // NOSONAR
        super(begrunnelse);
        this.erVilkarOk = erVilkarOk;
        this.avslagskode = avslagskode;
    }

    @JsonGetter
    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    @Override
    public String getAvslagskode() {
        return avslagskode;
    }

    @Override
    public boolean getErVilkarOk() {
        return erVilkarOk;
    }
}
