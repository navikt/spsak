package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto {


    @Valid
    @NotNull
    @Size(min = 1, max = 100)
    private List<FastsattBeløpTilstøtendeYtelseAndelDto> andeler;
    @NotNull
    private LocalDate fom;
    private LocalDate tom;



    TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto() { // NOSONAR
        // Jackson
    }

    public TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto(List<FastsattBeløpTilstøtendeYtelseAndelDto> andeler, LocalDate fom, LocalDate tom) { // NOSONAR
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

    public List<FastsattBeløpTilstøtendeYtelseAndelDto> getAndeler() {
        return andeler;
    }


}
