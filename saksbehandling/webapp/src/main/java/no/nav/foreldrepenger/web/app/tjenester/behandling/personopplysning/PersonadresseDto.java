package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;

public class PersonadresseDto {

    private AdresseType adresseType;
    private String mottakerNavn;
    private String adresselinje1;
    private String adresselinje2;
    private String adresselinje3;
    private String postNummer;
    private String poststed;
    private String land;

    public PersonadresseDto() {
    }

    public AdresseType getAdresseType() {
        return adresseType;
    }

    public String getMottakerNavn() {
        return mottakerNavn;
    }

    public String getAdresselinje1() {
        return adresselinje1;
    }

    public String getAdresselinje2() {
        return adresselinje2;
    }

    public String getAdresselinje3() {
        return adresselinje3;
    }

    public String getPostNummer() {
        return postNummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public String getLand() {
        return land;
    }

    public void setAdresseType(AdresseType adresseType) {
        this.adresseType = adresseType;
    }

    public void setMottakerNavn(String mottakerNavn) {
        this.mottakerNavn = mottakerNavn;
    }

    public void setAdresselinje1(String adresselinje1) {
        this.adresselinje1 = adresselinje1;
    }

    public void setAdresselinje2(String adresselinje2) {
        this.adresselinje2 = adresselinje2;
    }

    public void setAdresselinje3(String adresselinje3) {
        this.adresselinje3 = adresselinje3;
    }

    public void setPostNummer(String postNummer) {
        this.postNummer = postNummer;
    }

    public void setPoststed(String poststed) {
        this.poststed = poststed;
    }

    public void setLand(String land) {
        this.land = land;
    }
}
