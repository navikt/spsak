package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringAksjonspunktDto;

@JsonTypeName(ManuellAvklarFaktaUttakDto.AKSJONSPUNKT_KODE)
public class ManuellAvklarFaktaUttakDto extends OverstyringAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "6070";

    @Valid
    @Size(min = 1, max = 1000)
    private List<BekreftetUttakPeriodeDto> bekreftedePerioder = new ArrayList<>();

    @Valid
    @Size(max = 1000)
    private List<SlettetUttakPeriodeDto> slettedePerioder = new ArrayList<>();

    @SuppressWarnings("unused") // NOSONAR
    public ManuellAvklarFaktaUttakDto() {
        // For Jackson
    }

    public List<BekreftetUttakPeriodeDto> getBekreftedePerioder() {
        return bekreftedePerioder;
    }

    public void setBekreftedePerioder(List<BekreftetUttakPeriodeDto> bekreftedePerioder) {
        this.bekreftedePerioder = bekreftedePerioder;
    }

    public void setSlettedePerioder(List<SlettetUttakPeriodeDto> slettedePerioder) {
        this.slettedePerioder = slettedePerioder;
    }

    public List<SlettetUttakPeriodeDto> getSlettedePerioder() {
        return slettedePerioder;
    }

    @JsonGetter
    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    @JsonIgnore
    @Override
    public String getAvslagskode() {
        //Brukes ikke
        return null;
    }

    @JsonIgnore
    @Override
    public boolean getErVilkarOk() {
        //Brukes ikke
        return false;
    }

}
