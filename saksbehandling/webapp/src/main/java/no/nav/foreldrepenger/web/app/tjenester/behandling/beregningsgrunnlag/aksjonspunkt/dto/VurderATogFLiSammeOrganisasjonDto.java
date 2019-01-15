package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class VurderATogFLiSammeOrganisasjonDto {

    @Valid
    @Size(max = 100)
    private List<VurderATogFLiSammeOrganisasjonAndelDto> vurderATogFLiSammeOrganisasjonAndelListe;

    VurderATogFLiSammeOrganisasjonDto() {
        // For Jackson
    }

    public List<VurderATogFLiSammeOrganisasjonAndelDto> getVurderATogFLiSammeOrganisasjonAndelListe() {
        return vurderATogFLiSammeOrganisasjonAndelListe;
    }

    public void setVurderATogFLiSammeOrganisasjonAndelListe(List<VurderATogFLiSammeOrganisasjonAndelDto> vurderATogFLiSammeOrganisasjonAndelListe) {
        this.vurderATogFLiSammeOrganisasjonAndelListe = vurderATogFLiSammeOrganisasjonAndelListe;
    }
}
