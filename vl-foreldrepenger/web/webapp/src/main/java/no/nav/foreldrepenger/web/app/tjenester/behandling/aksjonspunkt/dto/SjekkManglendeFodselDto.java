package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(SjekkManglendeFodselDto.AKSJONSPUNKT_KODE)
public class SjekkManglendeFodselDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5027";
    @NotNull
    private Boolean dokumentasjonForeligger;

    private boolean brukAntallBarnITps;
    private LocalDate fodselsdato;

    @Min(1)
    @Max(9)
    private Integer antallBarnFodt;

    SjekkManglendeFodselDto() { // NOSONAR
        //For Jackson
    }

    public SjekkManglendeFodselDto(String begrunnelse, Boolean dokumentasjonForeligger, boolean brukAntallBarnITps,
            LocalDate fodselsdato, Integer antallBarnFodt) { // NOSONAR
        super(begrunnelse);
        this.dokumentasjonForeligger = dokumentasjonForeligger;
        this.brukAntallBarnITps = brukAntallBarnITps;
        this.fodselsdato = fodselsdato;
        this.antallBarnFodt = antallBarnFodt;
    }

    public Boolean getDokumentasjonForeligger() {
        return dokumentasjonForeligger;
    }

    public boolean isBrukAntallBarnITps() {
        return Boolean.FALSE.equals(dokumentasjonForeligger) || brukAntallBarnITps;
    }

    public LocalDate getFodselsdato() {
        return fodselsdato;
    }

    public Integer getAntallBarnFodt() {
        return antallBarnFodt;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
