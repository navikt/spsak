package no.nav.foreldrepenger.behandling.aksjonspunkt;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public abstract class OverstyringAksjonspunktDto implements AksjonspunktKode, AbacDto {

    @JsonProperty("begrunnelse")
    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    protected OverstyringAksjonspunktDto() { // NOSONAR
        //For Jackson
    }

    protected OverstyringAksjonspunktDto(String begrunnelse) { // NOSONAR
        this.begrunnelse = begrunnelse;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett()
            .leggTilAksjonspunktKode(getKode());
    }

    public abstract String getAvslagskode();

    public abstract boolean getErVilkarOk();

}
