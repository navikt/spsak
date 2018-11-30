package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

public class VurderLønnsendringDto {

    private Boolean erLønnsendringIBeregningsperioden;

    VurderLønnsendringDto() {
        // For Jackson
    }

    public VurderLønnsendringDto(Boolean erLønnsendringIBeregningsperioden) { // NOSONAR
        this.erLønnsendringIBeregningsperioden = erLønnsendringIBeregningsperioden;
    }

    public Boolean erLønnsendringIBeregningsperioden() {
        return erLønnsendringIBeregningsperioden;
    }

    public void setErLønnsendringIBeregningsperioden(Boolean erLønnsendringIBeregningsperioden) {
        this.erLønnsendringIBeregningsperioden = erLønnsendringIBeregningsperioden;
    }
}
