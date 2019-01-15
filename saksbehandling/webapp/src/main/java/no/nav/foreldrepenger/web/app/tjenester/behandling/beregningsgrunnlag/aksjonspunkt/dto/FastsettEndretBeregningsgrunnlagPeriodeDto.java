package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FastsettEndretBeregningsgrunnlagPeriodeDto {

    @Valid
    @Size(max = 100)
    private List<FastsettEndretBeregningsgrunnlagAndelDto> andeler;
    @NotNull
    private LocalDate fom;
    private LocalDate tom;



    FastsettEndretBeregningsgrunnlagPeriodeDto() { // NOSONAR
        // Jackson
    }

    public FastsettEndretBeregningsgrunnlagPeriodeDto(List<FastsettEndretBeregningsgrunnlagAndelDto> andeler, LocalDate fom, LocalDate tom) { // NOSONAR
        this.andeler = andeler;
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public List<FastsettEndretBeregningsgrunnlagAndelDto> getAndeler() {
        return andeler;
    }

}
