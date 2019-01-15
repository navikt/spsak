package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto;

public class KortvarigeArbeidsforholdDto extends FaktaOmBeregningAndelDto {
    private Boolean erTidsbegrensetArbeidsforhold;
    public KortvarigeArbeidsforholdDto() {
        // Hibernate
    }

    public Boolean getErTidsbegrensetArbeidsforhold() {
        return erTidsbegrensetArbeidsforhold;
    }

    public void setErTidsbegrensetArbeidsforhold(Boolean erTidsbegrensetArbeidsforhold) {
        this.erTidsbegrensetArbeidsforhold = erTidsbegrensetArbeidsforhold;
    }

}
