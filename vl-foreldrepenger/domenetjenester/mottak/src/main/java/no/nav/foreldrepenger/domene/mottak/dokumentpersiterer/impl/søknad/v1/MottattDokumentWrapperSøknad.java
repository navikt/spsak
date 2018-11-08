package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.søknad.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentFeil;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;
import no.nav.foreldrepenger.søknad.v1.SøknadConstants;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Vedlegg;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

public class MottattDokumentWrapperSøknad extends MottattDokumentWrapper<Soeknad, Vedlegg> {

    public MottattDokumentWrapperSøknad(Soeknad skjema) {
        super(skjema, SøknadConstants.NAMESPACE);
        sjekkNødvendigeFeltEksisterer(getSkjema());
    }

    public static void sjekkNødvendigeFeltEksisterer(Soeknad søknad) {
        if (søknad.getMottattDato() == null || søknad.getOmYtelse() == null || søknad.getSoeker() == null) {
            throw MottattDokumentFeil.FACTORY.ukjentSoeknadXMLFormat(søknad.getClass().getCanonicalName()).toException();
        }
    }

    public String getBegrunnelseForSenSoeknad() {
        return getSkjema().getBegrunnelseForSenSoeknad();
    }

    @SuppressWarnings("unchecked")
    public Ytelse getOmYtelse() {
        sjekkNødvendigeFeltEksisterer(getSkjema());
        return getSkjema().getOmYtelse().getAny().stream()
            .filter(o -> o instanceof JAXBElement)
            .map(o -> (Ytelse) ((JAXBElement) o).getValue())
            .findFirst().orElse(null);
    }

    public Bruker getBruker() {
        return getSkjema().getSoeker();
    }

    public String getTilleggsopplysninger() {
        return getSkjema().getTilleggsopplysninger();
    }

    @Override
    public List<Vedlegg> getVedleggListe() {
        List<Vedlegg> alleVedlegg = new ArrayList<>();
        alleVedlegg.addAll(getPåkrevdVedleggListe());
        alleVedlegg.addAll(getIkkePåkrevdVedleggListe());
        return alleVedlegg;
    }

    public List<Vedlegg> getPåkrevdVedleggListe() {
        return getSkjema().getPaakrevdeVedlegg();
    }

    public List<Vedlegg> getIkkePåkrevdVedleggListe() {
        return getSkjema().getAndreVedlegg();
    }

    @Override
    public List<String> getVedleggSkjemanummer() {
        List<String> skjemaNummerListe = new ArrayList<>();
        for (Vedlegg vedlegg : getVedleggListe()) {
            String innsendingstype = "LASTET_OPP"; // FIXME (Maur) Benytt Innsendingsvalg kodeverket ...
            if ((innsendingstype).equals(vedlegg.getInnsendingstype().getKode())) {
                skjemaNummerListe.add(vedlegg.getSkjemanummer());
            }
        }
        return skjemaNummerListe;
    }
}
