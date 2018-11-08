package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;

public abstract class AvklarFaktaUttakDto extends BekreftetAksjonspunktDto {

    @Valid
    @Size(min = 1, max = 1000)
    private List<BekreftetUttakPeriodeDto> bekreftedePerioder = new ArrayList<>();

    @Valid
    @Size(max = 1000)
    private List<SlettetUttakPeriodeDto> slettedePerioder = new ArrayList<>();

    AvklarFaktaUttakDto() { //NOSONAR
        // jackson
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

    @JsonTypeName(AvklarFaktaUttakPerioderDto.AKSJONSPUNKT_KODE)
    public static class AvklarFaktaUttakPerioderDto extends AvklarFaktaUttakDto {
        public static final String AKSJONSPUNKT_KODE = "5070";


        public AvklarFaktaUttakPerioderDto() {
            super();
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

    @JsonTypeName(AvklarFaktaUttakFørsteUttakDatoDto.AKSJONSPUNKT_KODE)
    public static class AvklarFaktaUttakFørsteUttakDatoDto extends AvklarFaktaUttakDto {
        public static final String AKSJONSPUNKT_KODE = "5081";

        public AvklarFaktaUttakFørsteUttakDatoDto() {
            super();
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

}
