package no.nav.foreldrepenger.dokumentbestiller.brev.klage;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.KlageAvvistDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.KlageYtelsesvedtakOpphevetDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.KlageYtelsesvedtakStadfestetDokument;

public class LagKlageDokumentData {
    private BrevParametere brevParametere;

    public LagKlageDokumentData(DokumentDataTjeneste dokumentDataTjeneste) {
        brevParametere = dokumentDataTjeneste.getBrevParametere();
    }

    public DokumentType lagDokumentData(Behandling behandling, String fritekst) {
        Optional<KlageVurderingResultat> klagevurdering = behandling.hentGjeldendeKlageVurderingResultat();
        if (!klagevurdering.isPresent()) {
            return null;
        }
        KlageVurdering klageVurderingResultat = klagevurdering.get().getKlageVurdering();
        if (KlageVurdering.AVVIS_KLAGE.equals(klageVurderingResultat)) {
            return new KlageAvvistDokument(brevParametere);
        } else if (KlageVurdering.OPPHEVE_YTELSESVEDTAK.equals(klageVurderingResultat) || KlageVurdering.MEDHOLD_I_KLAGE.equals(klageVurderingResultat)) {
            return new KlageYtelsesvedtakOpphevetDokument(brevParametere, fritekst);
        } else if (KlageVurdering.STADFESTE_YTELSESVEDTAK.equals(klageVurderingResultat)) {
            return new KlageYtelsesvedtakStadfestetDokument(brevParametere, fritekst);
        }
        return null;
    }

    public DokumentType opprettKlageVedtakbrev(Behandlingsresultat behandlingsresultat) {
        DokumentType vedtakDokument;
        if (BehandlingResultatType.KLAGE_AVVIST.equals(behandlingsresultat.getBehandlingResultatType())) {
            vedtakDokument = new KlageAvvistDokument(brevParametere);
        } else if (BehandlingResultatType.KLAGE_MEDHOLD.equals(behandlingsresultat.getBehandlingResultatType())
            || BehandlingResultatType.KLAGE_YTELSESVEDTAK_OPPHEVET.equals(behandlingsresultat.getBehandlingResultatType())) {
            vedtakDokument = new KlageYtelsesvedtakOpphevetDokument(brevParametere, behandlingsresultat.getAvslagarsakFritekst());
        } else {
            vedtakDokument = new KlageYtelsesvedtakStadfestetDokument(brevParametere, behandlingsresultat.getAvslagarsakFritekst());
        }
        return vedtakDokument;
    }
}
