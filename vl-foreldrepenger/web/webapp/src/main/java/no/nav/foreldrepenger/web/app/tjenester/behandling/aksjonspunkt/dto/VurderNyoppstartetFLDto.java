package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

public class VurderNyoppstartetFLDto {

    private Boolean erNyoppstartetFL;

    VurderNyoppstartetFLDto() {
        // For Jackson
    }

    public VurderNyoppstartetFLDto(Boolean erNyoppstartetFL) { // NOSONAR
        this.erNyoppstartetFL = erNyoppstartetFL;
    }

    public void setErNyoppstartetFL(Boolean erNyoppstartetFL) {
        this.erNyoppstartetFL = erNyoppstartetFL;
    }

    public Boolean erErNyoppstartetFL() {
        return erNyoppstartetFL;
    }
}
