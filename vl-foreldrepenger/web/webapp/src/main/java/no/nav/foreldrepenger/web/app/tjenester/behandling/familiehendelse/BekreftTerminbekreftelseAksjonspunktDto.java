package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;

@JsonTypeName(BekreftTerminbekreftelseAksjonspunktDto.AKSJONSPUNKT_KODE)
public class BekreftTerminbekreftelseAksjonspunktDto extends BekreftetAksjonspunktDto {

    public static final String AKSJONSPUNKT_KODE = "5001";

    @NotNull
    private LocalDate utstedtdato;

    @NotNull
    private LocalDate termindato;

    @NotNull
    @Min(1)
    @Max(9)
    private int antallBarn;

    BekreftTerminbekreftelseAksjonspunktDto() {  // NOSONAR
        // For Jackson
    }

    public BekreftTerminbekreftelseAksjonspunktDto( // NOSONAR
                                                    String begrunnelse,
                                                    LocalDate termindato,
                                                    LocalDate utstedtdato,
                                                    int antallBarn) {

        super(begrunnelse);
        this.termindato = termindato;
        this.utstedtdato = utstedtdato;
        this.antallBarn = antallBarn;
    }

    public LocalDate getUtstedtdato() {
        return utstedtdato;
    }

    public LocalDate getTermindato() {
        return termindato;
    }

    public int getAntallBarn() {
        return antallBarn;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

}
