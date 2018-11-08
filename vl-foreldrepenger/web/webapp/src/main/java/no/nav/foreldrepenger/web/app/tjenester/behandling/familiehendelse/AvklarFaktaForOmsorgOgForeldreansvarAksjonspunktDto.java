package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
@JsonTypeName(AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto.AKSJONSPUNKT_KODE)
public class AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto extends BekreftetAksjonspunktDto implements OmsorgsOvertakelse {

    static final String AKSJONSPUNKT_KODE = "5008";

    @JsonProperty("omsorgsovertakelseDato")
    @NotNull
    private LocalDate omsorgsovertakelseDato;

    @JsonProperty("vilkarType")
    @NotNull
    @ValidKodeverk
    private VilkårType vilkårType;

    @JsonProperty("antallBarn")
    @Min(1)
    @Max(9)
    private Integer antallBarn;

    @JsonProperty("barn")
    @Valid
    @NotNull
    @Size(max = 9)
    private List<AvklartDataBarnDto> barn = new ArrayList<>();
    
    @JsonProperty("foreldre")
    @Valid
    @NotNull
    @Size(max = 9)
    private List<AvklartDataForeldreDto> foreldre = new ArrayList<>();

    @JsonGetter
    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    @Override
    public LocalDate getOmsorgsovertakelseDato() {
        return omsorgsovertakelseDato;
    }

    public void setOmsorgsovertakelseDato(LocalDate omsorgsovertakelseDato) {
        this.omsorgsovertakelseDato = omsorgsovertakelseDato;
    }

    public Integer getAntallBarn() {
        return antallBarn;
    }

    public void setAntallBarn(Integer antallBarn) {
        this.antallBarn = antallBarn;
    }

    public VilkårType getVilkårType() {
        return vilkårType;
    }

    public void setVilkårType(VilkårType vilkarType) {
        this.vilkårType = vilkarType;
    }

    public List<AvklartDataBarnDto> getBarn() {
        return barn;
    }

    public void setBarn(List<AvklartDataBarnDto> barn) {
        this.barn = barn;
    }

    public List<AvklartDataForeldreDto> getForeldre() {
        return foreldre;
    }

    public void setForeldre(List<AvklartDataForeldreDto> foreldre) {
        this.foreldre = foreldre;
    }
}
