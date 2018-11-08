package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Inntektsmeldinger som søker har rapport at ikke vil komme fra angitt arbeidsgiver
 */
public class InntektsmeldingSomIkkeKommerDto {

    @NotNull
    @Pattern(regexp = "[\\d]{9}|[\\d]{11}")
    private String organisasjonsnummer;

    private boolean brukerHarSagtAtIkkeKommer;

    public InntektsmeldingSomIkkeKommerDto() { // NOSONAR
        // Jackson
    }

    public InntektsmeldingSomIkkeKommerDto(String organisasjonsnummer, boolean brukerHarSagtAtIkkeKommer) {
        this.organisasjonsnummer = organisasjonsnummer;
        this.brukerHarSagtAtIkkeKommer = brukerHarSagtAtIkkeKommer;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public boolean isBrukerHarSagtAtIkkeKommer() {
        return brukerHarSagtAtIkkeKommer;
    }
}
