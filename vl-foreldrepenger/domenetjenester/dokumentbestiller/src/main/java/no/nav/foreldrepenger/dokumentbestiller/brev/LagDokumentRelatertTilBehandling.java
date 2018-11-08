package no.nav.foreldrepenger.dokumentbestiller.brev;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.BrevFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.ForlengetDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnhenteOpplysningerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnsynskravSvarDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InntektsmeldingForTidligDokument;
import no.nav.foreldrepenger.dokumentbestiller.brev.klage.LagKlageDokumentData;

public class LagDokumentRelatertTilBehandling {

    private DokumentDataTjeneste dokumentDataTjeneste;
    private BrevParametere brevParametere;

    public LagDokumentRelatertTilBehandling(DokumentDataTjeneste dokumentDataTjeneste) {
        this.dokumentDataTjeneste = dokumentDataTjeneste;
        this.brevParametere = dokumentDataTjeneste.getBrevParametere();
    }

    public DokumentType lagDokumentData(Behandling behandling, String brevmalkode, String fritekst) {
        if (DokumentMalType.FORLENGET_DOK.equals(brevmalkode) ||
            DokumentMalType.FORLENGET_MEDL_DOK.equals(brevmalkode) ||
            DokumentMalType.FORLENGET_TIDLIG_SOK.equals(brevmalkode)) {
            return lagForlengetDokument(brevmalkode, behandling);
        } else if (DokumentMalType.INNTEKTSMELDING_FOR_TIDLIG_DOK.equals(brevmalkode)) {
            return new InntektsmeldingForTidligDokument(brevParametere, behandling.getType());
        } else if (DokumentMalType.INNHENT_DOK.equals(brevmalkode)) {
            sjekkOmFritekstErTomNårInnhentDokumentasjon(brevmalkode, fritekst);
            return new InnhenteOpplysningerDokument(brevParametere, fritekst, behandling.getType());
        } else if (DokumentMalType.INNSYNSKRAV_SVAR.equals(brevmalkode)) {
            return new InnsynskravSvarDokument(brevParametere, fritekst, behandling.getInnsyn().getInnsynResultatType());
        } else if (behandling.erKlage()) {
            return new LagKlageDokumentData(dokumentDataTjeneste).lagDokumentData(behandling, fritekst);
        }
        throw BrevFeil.FACTORY.brevmalIkkeTilgjengelig(brevmalkode).toException();
    }

    private DokumentType lagForlengetDokument(String brevmalkode, Behandling behandling) {
        if (new SjekkDokumentTilgjengelig(dokumentDataTjeneste).sjekkOmTilgjengelig(behandling, dokumentDataTjeneste.hentDokumentMalType(brevmalkode))) {
            return new ForlengetDokument(brevmalkode);
        }
        throw BrevFeil.FACTORY.brevmalIkkeTilgjengelig(brevmalkode).toException();
    }

    static void sjekkOmFritekstErTomNårInnhentDokumentasjon(String brevmalkode, String fritekst) {
        if (DokumentMalType.INNHENT_DOK.equals(brevmalkode) && fritekst.isEmpty()) {
            throw BrevFeil.FACTORY.innhentDokumentasjonKreverFritekst().toException();
        }
    }
}
