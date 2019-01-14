package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

@JsonTypeName(AvklarFortsattMedlemskapDto.AKSJONSPUNKT_KODE)
public class AvklarFortsattMedlemskapDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5053";

    @NotNull
    private LocalDate fomDato;

    AvklarFortsattMedlemskapDto() { // NOSONAR
        // For Jackson
    }

    public AvklarFortsattMedlemskapDto(String begrunnelse, LocalDate fomDato) { // NOSONAR
        super(begrunnelse);
        this.fomDato = fomDato;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public LocalDate getFomDato() {
        return fomDato;
    }
}
