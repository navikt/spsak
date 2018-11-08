package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(VurdereYtelseSammeBarnAnnenForelderAksjonspunktDto.AKSJONSPUNKT_KODE)
public class VurdereYtelseSammeBarnAnnenForelderAksjonspunktDto extends BekreftetAksjonspunktDto implements AvslagbartAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5032";

    @NotNull
    private Boolean erVilkarOk;

    @Size(min = 1, max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String avslagskode;

    public VurdereYtelseSammeBarnAnnenForelderAksjonspunktDto() { // NOSONAR
        // for jackson
    }

    public VurdereYtelseSammeBarnAnnenForelderAksjonspunktDto(String begrunnelse, Boolean erVilkarOk) {  // NOSONAR
        super(begrunnelse);
        this.erVilkarOk = erVilkarOk;
    }

    @Override
    public Boolean getErVilkarOk() {
        return erVilkarOk;
    }

    public void setErVilkarOk(Boolean erVilkarOk) {
        this.erVilkarOk = erVilkarOk;
    }

    @Override
    public String getAvslagskode() {
        return avslagskode;
    }

    public void setAvslagskode(String avslagskode) {
        this.avslagskode = avslagskode;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
