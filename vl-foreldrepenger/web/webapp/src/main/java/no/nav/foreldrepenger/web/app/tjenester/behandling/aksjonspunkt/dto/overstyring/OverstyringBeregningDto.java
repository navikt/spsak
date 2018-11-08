package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
@JsonTypeName(OverstyringBeregningDto.AKSJONSPUNKT_KODE)
public class OverstyringBeregningDto extends OverstyringAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "6007";

    @JsonProperty("beregnetTilkjentYtelse")
    @Min(0)
    @Max(500000L)
    private long beregnetTilkjentYtelse;

    @SuppressWarnings("unused") // NOSONAR
    private OverstyringBeregningDto() {
        super();
        // For Jackson
    }

    public OverstyringBeregningDto(long beregnetTilkjentYtelse, String begrunnelse) { // NOSONAR
        super(begrunnelse);
        this.beregnetTilkjentYtelse = beregnetTilkjentYtelse;
    }

    @JsonGetter
    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public long getBeregnetTilkjentYtelse() {
        return beregnetTilkjentYtelse;
    }

    @JsonIgnore
    @Override
    public String getAvslagskode() {
        return null;
    }

    @JsonIgnore
    @Override
    public boolean getErVilkarOk() {
        return true;
    }
}
