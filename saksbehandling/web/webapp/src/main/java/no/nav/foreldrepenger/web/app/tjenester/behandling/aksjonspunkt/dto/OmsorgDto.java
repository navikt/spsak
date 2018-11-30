package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class OmsorgDto {
    @Min(1)
    @Max(9)
    Integer antallBarn;

    @Size(min = 1, max = 9)
    private List<LocalDate> foedselsDato;

    private LocalDate omsorgsovertakelsesdato;

    private LocalDate ankomstdato;

    private boolean erEktefellesBarn;

    public Integer getAntallBarn() {
        return antallBarn;
    }

    public void setAntallBarn(Integer antallBarn) {
        this.antallBarn = antallBarn;
    }

    public List<LocalDate> getFoedselsDato() {
        return foedselsDato;
    }

    public void setFoedselsDato(List<LocalDate> foedselsDato) {
        this.foedselsDato = foedselsDato;
    }

    public LocalDate getOmsorgsovertakelsesdato() {
        return omsorgsovertakelsesdato;
    }

    public void setOmsorgsovertakelsesdato(LocalDate omsorgsovertakelsesdato) {
        this.omsorgsovertakelsesdato = omsorgsovertakelsesdato;
    }

    public LocalDate getAnkomstdato() {
        return ankomstdato;
    }

    public void setAnkomstdato(LocalDate ankomstdato) {
        this.ankomstdato = ankomstdato;
    }

    public boolean isErEktefellesBarn() {
        return erEktefellesBarn;
    }

    public void setErEktefellesBarn(boolean erEktefellesBarn) {
        this.erEktefellesBarn = erEktefellesBarn;
    }
}
