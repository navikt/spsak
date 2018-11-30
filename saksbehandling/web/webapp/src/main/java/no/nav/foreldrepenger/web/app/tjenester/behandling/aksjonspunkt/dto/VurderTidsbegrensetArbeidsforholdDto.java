package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class VurderTidsbegrensetArbeidsforholdDto {

    @Valid
    @Size(max = 100)
    private List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold;

    VurderTidsbegrensetArbeidsforholdDto() {
        // For Jackson
    }

    public VurderTidsbegrensetArbeidsforholdDto(List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold) { // NOSONAR
        this.fastsatteArbeidsforhold = new ArrayList<>(fastsatteArbeidsforhold);
    }

    public List<VurderteArbeidsforholdDto> getFastsatteArbeidsforhold() {
        return fastsatteArbeidsforhold;
    }

    public void setFastsatteArbeidsforhold(List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold) {
        this.fastsatteArbeidsforhold = fastsatteArbeidsforhold;
    }
}
