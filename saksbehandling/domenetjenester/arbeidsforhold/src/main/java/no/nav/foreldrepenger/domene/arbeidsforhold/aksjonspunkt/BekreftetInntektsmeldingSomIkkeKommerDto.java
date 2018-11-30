package no.nav.foreldrepenger.domene.arbeidsforhold.aksjonspunkt;

public class BekreftetInntektsmeldingSomIkkeKommerDto {

    private String organisasjonsnummer;

    public BekreftetInntektsmeldingSomIkkeKommerDto(String organisasjonsnummer) {
        this.organisasjonsnummer = organisasjonsnummer;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }
}
