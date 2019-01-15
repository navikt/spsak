package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

public class VirksomhetDto {

    private String navn;
    private String organisasjonsnummer;

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public void setOrganisasjonsnummer(String organisasjonsnummer) {
        this.organisasjonsnummer = organisasjonsnummer;
    }
}
