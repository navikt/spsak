package no.nav.foreldrepenger.behandlingslager.aktør;

public class OrganisasjonsEnhet {

    private String enhetId;
    private String enhetNavn;
    private String status;

    public OrganisasjonsEnhet(String enhetId, String enhetNavn) {
        this.enhetId = enhetId;
        this.enhetNavn = enhetNavn;
    }

    public OrganisasjonsEnhet(String enhetId, String enhetNavn, String status){
        this.enhetId = enhetId;
        this.enhetNavn = enhetNavn;
        this.status = status;
    }

    public String getEnhetId() { return enhetId; }

    public String getEnhetNavn(){ return enhetNavn; }

    public String getStatus() { return status; }
}
