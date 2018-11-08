package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(OmsorgsvilkårAksjonspunktDto.AKSJONSPUNKT_KODE)
public class OmsorgsvilkårAksjonspunktDto extends BekreftetAksjonspunktDto implements AvslagbartAksjonspunktDto{

    static final String AKSJONSPUNKT_KODE = "5011";

    @NotNull
    private Boolean erVilkarOk;

    @Size(min = 1, max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String avslagskode;

    OmsorgsvilkårAksjonspunktDto() { // NOSONAR
        //For Jackson
    }

    public OmsorgsvilkårAksjonspunktDto(String begrunnelse, Boolean erVilkarOk, String avslagskode) { // NOSONAR
        super(begrunnelse);
        this.erVilkarOk = erVilkarOk;
        this.avslagskode = avslagskode;
    }

    @Override
    public Boolean getErVilkarOk() {
        return erVilkarOk;
    }

    @Override
    public String getAvslagskode() {
        return avslagskode;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

}
