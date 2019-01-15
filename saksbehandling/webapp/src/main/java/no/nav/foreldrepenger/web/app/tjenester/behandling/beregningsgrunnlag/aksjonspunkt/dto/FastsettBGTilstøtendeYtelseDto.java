package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;


public class FastsettBGTilstøtendeYtelseDto {


    @Valid
    @Size(min = 1, max = 100)
    private List<FastsattBeløpTilstøtendeYtelseAndelDto> tilstøtendeYtelseAndeler;
    private boolean gjelderBesteberegning;


    FastsettBGTilstøtendeYtelseDto() {
        // For Jackson
    }

    public FastsettBGTilstøtendeYtelseDto(List<FastsattBeløpTilstøtendeYtelseAndelDto> tilstøtendeYtelseAndeler) { // NOSONAR
        this.tilstøtendeYtelseAndeler = new ArrayList<>(tilstøtendeYtelseAndeler);
    }

    public List<FastsattBeløpTilstøtendeYtelseAndelDto> getTilstøtendeYtelseAndeler() {
        return tilstøtendeYtelseAndeler;
    }

    public void setTilstøtendeYtelseAndeler(List<FastsattBeløpTilstøtendeYtelseAndelDto> tilstøtendeYtelseAndeler) {
        this.tilstøtendeYtelseAndeler = tilstøtendeYtelseAndeler;
    }

    public boolean erBesteberegning() {
        return gjelderBesteberegning;
    }

    public void setGjelderBesteberegning(boolean gjelderBesteberegning) {
        this.gjelderBesteberegning = gjelderBesteberegning;
    }
}
