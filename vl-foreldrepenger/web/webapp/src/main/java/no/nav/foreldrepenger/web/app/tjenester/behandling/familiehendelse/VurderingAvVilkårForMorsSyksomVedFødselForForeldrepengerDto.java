package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;

@JsonTypeName(VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto.AKSJONSPUNKT_KODE)
public class VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto extends BekreftetAksjonspunktDto {

    public static final String AKSJONSPUNKT_KODE = "5044";

    @NotNull
    private Boolean erMorForSykVedFodsel;

    public VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto() { //NOSONAR
        // Jackson
    }

    public VurderingAvVilkårForMorsSyksomVedFødselForForeldrepengerDto(String begrunnelse, boolean erMorForSykVedFodsel) {
        super(begrunnelse);
        this.erMorForSykVedFodsel = erMorForSykVedFodsel;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public Boolean getErMorForSykVedFodsel() {
        return erMorForSykVedFodsel;
    }
}
