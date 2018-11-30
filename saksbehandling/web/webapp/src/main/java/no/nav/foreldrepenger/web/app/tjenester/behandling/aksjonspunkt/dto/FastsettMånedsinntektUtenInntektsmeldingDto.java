package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class FastsettMånedsinntektUtenInntektsmeldingDto {

    @Valid
    @Size(max = 100)
    private List<VurderLønnsendringAndelDto> vurderLønnsendringAndelListe;

    public List<VurderLønnsendringAndelDto> getVurderLønnsendringAndelListe() {
        return vurderLønnsendringAndelListe;
    }

    public void setVurderLønnsendringAndelListe(List<VurderLønnsendringAndelDto> vurderLønnsendringAndelListe) {
        this.vurderLønnsendringAndelListe = vurderLønnsendringAndelListe;
    }
}
