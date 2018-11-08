package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.engangsstønad.v1;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.SoeknadsskjemaEngangsstoenadContants;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Aktoer;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.FoedselEllerAdopsjon;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmBarn;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmFar;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmMor;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Rettigheter;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Soknadsvalg;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.TilknytningNorge;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Vedlegg;

public class MottattDokumentWrapperEngangsstønad extends MottattDokumentWrapper<SoeknadsskjemaEngangsstoenad, Vedlegg> {

    public MottattDokumentWrapperEngangsstønad(SoeknadsskjemaEngangsstoenad skjema) {
        super(skjema, SoeknadsskjemaEngangsstoenadContants.NAMESPACE);
    }

    public Aktoer getBruker() {
        return getSkjema().getBruker();
    }

    public FoedselEllerAdopsjon getFoedselEllerAdopsjon () {
        return  getSkjema().getSoknadsvalg().getFoedselEllerAdopsjon();
    }

    @Override
    public List<Vedlegg> getVedleggListe() {
        return getSkjema().getVedleggListe().getVedlegg();
    }

    public OpplysningerOmBarn getOpplysningerOmBarn() {
        return getSkjema().getOpplysningerOmBarn();
    }

    public Rettigheter getRettigheter() {
        return getSkjema().getRettigheter();
    }

    public Soknadsvalg getSoknadsvalg() {
        return getSkjema().getSoknadsvalg();
    }

    public OpplysningerOmMor getOpplysningerOmMor() {
        return getSkjema().getOpplysningerOmMor();
    }

    public OpplysningerOmFar getOpplysningerOmFar() {
        return getSkjema().getOpplysningerOmFar();
    }

    public String getTilleggsopplysninger() {
        return getSkjema().getTilleggsopplysninger();
    }

    public TilknytningNorge getTilknytningNorge() {
        return getSkjema().getTilknytningNorge();
    }

    @Override
    public List<String> getVedleggSkjemanummer() {
        List<String> skjemaNummerListe = new ArrayList<>();
        if (getVedleggListe() != null) {
            for (Vedlegg vedlegg : getVedleggListe()) {
                if (no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Innsendingsvalg.LASTET_OPP
                    .equals(vedlegg.getInnsendingsvalg())) {
                    skjemaNummerListe.add(vedlegg.getSkjemanummer());
                }
            }
        }
        return skjemaNummerListe;
    }

}
