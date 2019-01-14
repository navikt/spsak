package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

@JsonTypeName(FastsettBGTidsbegrensetArbeidsforholdDto.AKSJONSPUNKT_KODE)
public class FastsettBGTidsbegrensetArbeidsforholdDto extends BekreftetAksjonspunktDto {

    public static final String AKSJONSPUNKT_KODE = "5047";

    @Valid
    @Size(max = 100)
    private List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder;

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Integer frilansInntekt;


    FastsettBGTidsbegrensetArbeidsforholdDto() {
        // For Jackson
    }

    public FastsettBGTidsbegrensetArbeidsforholdDto(String begrunnelse, List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder) { // NOSONAR
        super(begrunnelse);
        this.fastsatteTidsbegrensedePerioder = new ArrayList<>(fastsatteTidsbegrensedePerioder);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public List<FastsattePerioderTidsbegrensetDto> getFastsatteTidsbegrensedePerioder() {
        return fastsatteTidsbegrensedePerioder;
    }

    public Integer getFrilansInntekt() {
        return frilansInntekt;
    }

    public void setFastsatteTidsbegrensedePerioder(List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder) {
        this.fastsatteTidsbegrensedePerioder = fastsatteTidsbegrensedePerioder;
    }

    public void setFrilansInntekt(Integer frilansInntekt) {
        this.frilansInntekt = frilansInntekt;
    }
}
