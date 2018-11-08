package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

public class VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto {

    private Boolean erNyIArbeidslivet;

    VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto() {
        // For Jackson
    }

    public VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto(Boolean erNyIArbeidslivet) { // NOSONAR
        this.erNyIArbeidslivet = erNyIArbeidslivet;
    }

    public void setErNyIArbeidslivet(Boolean erNyIArbeidslivet) {
        this.erNyIArbeidslivet = erNyIArbeidslivet;
    }

    public Boolean erNyIArbeidslivet() {
        return erNyIArbeidslivet;
    }
}
