package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;

public abstract class FastsetteUttakDto extends BekreftetAksjonspunktDto {

    @Valid
    @NotNull
    @Size(min = 1, max = 1500)
    private List<UttakResultatPeriodeLagreDto> perioder;

    FastsetteUttakDto() { //NOSONAR
        // jackson
    }

    public FastsetteUttakDto(List<UttakResultatPeriodeLagreDto> perioder) {
        this.perioder = perioder;
    }

    public List<UttakResultatPeriodeLagreDto> getPerioder() {
        return perioder;
    }

    @JsonTypeName(FastsetteUttakPerioderDto.AKSJONSPUNKT_KODE)
    public static class FastsetteUttakPerioderDto extends FastsetteUttakDto {
        static final String AKSJONSPUNKT_KODE = "5071";

        @SuppressWarnings("unused") // NOSONAR
        private FastsetteUttakPerioderDto() {
            // For Jackson
        }

        public FastsetteUttakPerioderDto(List<UttakResultatPeriodeLagreDto> perioder) {
            super(perioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

    @JsonTypeName(FastsetteUttakTilknyttetStortinget.AKSJONSPUNKT_KODE)
    public static class FastsetteUttakTilknyttetStortinget extends FastsetteUttakDto {
        static final String AKSJONSPUNKT_KODE = "5072";

        @SuppressWarnings("unused") // NOSONAR
        private FastsetteUttakTilknyttetStortinget() {
            // For Jackson
        }

        public FastsetteUttakTilknyttetStortinget(List<UttakResultatPeriodeLagreDto> perioder) {
            super(perioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }


    @JsonTypeName(FastsetteUttakKontrollerRealitetsBehandlingEllerKlageDto.AKSJONSPUNKT_KODE)
    public static class FastsetteUttakKontrollerRealitetsBehandlingEllerKlageDto extends FastsetteUttakDto {
        static final String AKSJONSPUNKT_KODE = "5073";

        @SuppressWarnings("unused") // NOSONAR
        private FastsetteUttakKontrollerRealitetsBehandlingEllerKlageDto() {
            // For Jackson
        }

        public FastsetteUttakKontrollerRealitetsBehandlingEllerKlageDto(List<UttakResultatPeriodeLagreDto> perioder) {
            super(perioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

    @JsonTypeName(FastsetteUttakKontrollerOpplysningerOmMedlemskapDto.AKSJONSPUNKT_KODE)
    public static class FastsetteUttakKontrollerOpplysningerOmMedlemskapDto extends FastsetteUttakDto {
        static final String AKSJONSPUNKT_KODE = "5074";

        @SuppressWarnings("unused") // NOSONAR
        private FastsetteUttakKontrollerOpplysningerOmMedlemskapDto() {
            // For Jackson
        }

        public FastsetteUttakKontrollerOpplysningerOmMedlemskapDto(List<UttakResultatPeriodeLagreDto> perioder) {
            super(perioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

    @JsonTypeName(FastsetteUttakKontrollerOpplysningerOmFordelingAvStønadsperiodenDto.AKSJONSPUNKT_KODE)
    public static class FastsetteUttakKontrollerOpplysningerOmFordelingAvStønadsperiodenDto extends FastsetteUttakDto {
        static final String AKSJONSPUNKT_KODE = "5075";

        @SuppressWarnings("unused") // NOSONAR
        private FastsetteUttakKontrollerOpplysningerOmFordelingAvStønadsperiodenDto() {
            // For Jackson
        }

        public FastsetteUttakKontrollerOpplysningerOmFordelingAvStønadsperiodenDto(List<UttakResultatPeriodeLagreDto> perioder) {
            super(perioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

    @JsonTypeName(FastsetteUttakKontrollerOpplysningerOmDødDto.AKSJONSPUNKT_KODE)
    public static class FastsetteUttakKontrollerOpplysningerOmDødDto extends FastsetteUttakDto {
        static final String AKSJONSPUNKT_KODE = "5076";

        @SuppressWarnings("unused") // NOSONAR
        private FastsetteUttakKontrollerOpplysningerOmDødDto() {
            // For Jackson
        }

        public FastsetteUttakKontrollerOpplysningerOmDødDto(List<UttakResultatPeriodeLagreDto> perioder) {
            super(perioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

    @JsonTypeName(FastsetteUttakKontrollerOpplysningerOmSøknadsfristDto.AKSJONSPUNKT_KODE)
    public static class FastsetteUttakKontrollerOpplysningerOmSøknadsfristDto extends FastsetteUttakDto {
        static final String AKSJONSPUNKT_KODE = "5077";

        @SuppressWarnings("unused") // NOSONAR
        private FastsetteUttakKontrollerOpplysningerOmSøknadsfristDto() {
            // For Jackson
        }

        public FastsetteUttakKontrollerOpplysningerOmSøknadsfristDto(List<UttakResultatPeriodeLagreDto> perioder) {
            super(perioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

    @JsonTypeName(FastsetteUttakKontrollerTilstøtendeYtelserInnvilgetDto.AKSJONSPUNKT_KODE)
    public static class FastsetteUttakKontrollerTilstøtendeYtelserInnvilgetDto extends FastsetteUttakDto {
        static final String AKSJONSPUNKT_KODE = "5078";

        @SuppressWarnings("unused") // NOSONAR
        private FastsetteUttakKontrollerTilstøtendeYtelserInnvilgetDto() {
            // For Jackson
        }

        public FastsetteUttakKontrollerTilstøtendeYtelserInnvilgetDto(List<UttakResultatPeriodeLagreDto> perioder) {
            super(perioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

    @JsonTypeName(FastsetteUttakKontrollerTilstøtendeYtelserOpphørtDto.AKSJONSPUNKT_KODE)
    public static class FastsetteUttakKontrollerTilstøtendeYtelserOpphørtDto extends FastsetteUttakDto {
        static final String AKSJONSPUNKT_KODE = "5079";

        @SuppressWarnings("unused") // NOSONAR
        private FastsetteUttakKontrollerTilstøtendeYtelserOpphørtDto() {
            // For Jackson
        }

        public FastsetteUttakKontrollerTilstøtendeYtelserOpphørtDto(List<UttakResultatPeriodeLagreDto> perioder) {
            super(perioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }
}
