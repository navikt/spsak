package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TilstotendeYtelseOgEndretBeregningsgrunnlagDto {

    @Valid
    @NotNull
    @Size(min = 1, max = 100)
    private List<TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto> perioder;

    private boolean gjelderBesteberegning;


    TilstotendeYtelseOgEndretBeregningsgrunnlagDto() { // NOSONAR
        // For Jackson
    }

    public TilstotendeYtelseOgEndretBeregningsgrunnlagDto(List<TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto> perioder, boolean gjelderBesteberegning) { // NOSONAR
        this.perioder = new ArrayList<>(perioder);
        this.gjelderBesteberegning = gjelderBesteberegning;
    }

    public List<TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto> getPerioder() {
        return perioder;
    }

    public void setPerioder(List<TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto> perioder) {
        this.perioder = perioder;
    }

    public boolean erBesteberegning() {
        return gjelderBesteberegning;
    }

    public void setGjelderBesteberegning(boolean gjelderBesteberegning) {
        this.gjelderBesteberegning = gjelderBesteberegning;
    }


}
