package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class FastsettEndretBeregningsgrunnlagDto {

    @Valid
    @Size(max = 100)
    private List<FastsettEndretBeregningsgrunnlagPeriodeDto> endretBeregningsgrunnlagPerioder;

    FastsettEndretBeregningsgrunnlagDto() { // NOSONAR
        // Jackson
    }

    public FastsettEndretBeregningsgrunnlagDto(List<FastsettEndretBeregningsgrunnlagPeriodeDto> endretBeregningsgrunnlagPerioder) { // NOSONAR
        this.endretBeregningsgrunnlagPerioder = endretBeregningsgrunnlagPerioder;
    }



    public List<FastsettEndretBeregningsgrunnlagPeriodeDto> getEndretBeregningsgrunnlagPerioder() {
        return endretBeregningsgrunnlagPerioder;
    }
}
