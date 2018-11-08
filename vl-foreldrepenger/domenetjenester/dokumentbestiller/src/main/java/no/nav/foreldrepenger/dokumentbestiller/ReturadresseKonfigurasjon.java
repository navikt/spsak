package no.nav.foreldrepenger.dokumentbestiller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class ReturadresseKonfigurasjon {
    private String brevReturadresseEnhetNavn;
    private String brevReturadresseAdresselinje1;
    private String brevReturadressePostnummer;
    private String brevReturadressePoststed;
    private String brevReturadresseKlageEnhet;

    public ReturadresseKonfigurasjon() {
        // for CDI proxy
    }

    @Inject
    public ReturadresseKonfigurasjon(@KonfigVerdi("brev.returadresse.enhet.navn") String brevReturadresseEnhetNavn,
            @KonfigVerdi("brev.returadresse.adresselinje1") String brevReturadresseAdresselinje1,
            @KonfigVerdi("brev.returadresse.postnummer") String brevReturadressePostnummer,
            @KonfigVerdi("brev.returadresse.poststed") String brevReturadressePoststed,
            @KonfigVerdi("brev.returadresse.klage.enhet.navn") String brevReturadresseKlageEnhet) {

        this.brevReturadresseEnhetNavn = brevReturadresseEnhetNavn;
        this.brevReturadresseAdresselinje1 = brevReturadresseAdresselinje1;
        this.brevReturadressePostnummer = brevReturadressePostnummer;
        this.brevReturadressePoststed = brevReturadressePoststed;
        this.brevReturadresseKlageEnhet = brevReturadresseKlageEnhet;
    }

    public String getBrevReturadresseEnhetNavn() {
        return brevReturadresseEnhetNavn;
    }

    public String getBrevReturadresseAdresselinje1() {
        return brevReturadresseAdresselinje1;
    }

    public String getBrevReturadressePostnummer() {
        return brevReturadressePostnummer;
    }

    public String getBrevReturadressePoststed() {
        return brevReturadressePoststed;
    }

    public String getBrevReturadresseKlageEnhet() {
        return brevReturadresseKlageEnhet;
    }

}
