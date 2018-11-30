package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(AvklarStartdatoForFPperiodenDto.AKSJONSPUNKT_KODE)
public class AvklarStartdatoForFPperiodenDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5045";

    public AvklarStartdatoForFPperiodenDto(String begrunnelse, LocalDate startdatoFraSoknad) {
        super(begrunnelse);
        this.startdatoFraSoknad = startdatoFraSoknad;
    }

    @NotNull
    private LocalDate startdatoFraSoknad;

    @SuppressWarnings("unused") // NOSONAR
    private AvklarStartdatoForFPperiodenDto() {
        super();
        // For Jackson
    }


    public AvklarStartdatoForFPperiodenDto(String begrunnelse) { // NOSONAR
        super(begrunnelse);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public LocalDate getStartdatoFraSoknad() {
        return startdatoFraSoknad;
    }
}
