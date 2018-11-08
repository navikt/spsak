package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class BesteberegningFødendeKvinneDto {

    @Valid
    @Size(max = 100)
    private List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe;

    BesteberegningFødendeKvinneDto() {
        // For Jackson
    }

    public List<BesteberegningFødendeKvinneAndelDto> getBesteberegningAndelListe() {
        return besteberegningAndelListe;
    }

    public void setBesteberegningAndelListe(List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe) {
        this.besteberegningAndelListe = besteberegningAndelListe;
    }
}
