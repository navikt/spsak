package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class VurderteArbeidsforholdDto  {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long andelsnr;
    @NotNull
    private boolean tidsbegrensetArbeidsforhold;
    private Boolean opprinneligVerdi;

    public VurderteArbeidsforholdDto() { // NOSONAR
        // Jackson
    }

    public VurderteArbeidsforholdDto(Long andelsnr,
                                     boolean tidsbegrensetArbeidsforhold,
                                     Boolean opprinneligVerdi) {
        this.andelsnr = andelsnr;
        this.tidsbegrensetArbeidsforhold = tidsbegrensetArbeidsforhold;
        this.opprinneligVerdi = opprinneligVerdi;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public boolean isTidsbegrensetArbeidsforhold() {
        return tidsbegrensetArbeidsforhold;
    }

    public Boolean isOpprinneligVerdi() {
        return opprinneligVerdi;
    }
}
