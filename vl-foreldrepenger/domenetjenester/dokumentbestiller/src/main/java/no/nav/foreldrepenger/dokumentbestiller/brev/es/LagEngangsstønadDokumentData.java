package no.nav.foreldrepenger.dokumentbestiller.brev.es;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.dokumentbestiller.BrevFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.AvslagVedtakDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.PositivtVedtakDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.UendretUtfallDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.brev.klage.LagKlageDokumentData;

public class LagEngangsstønadDokumentData {
    private DokumentDataTjeneste dokumentDataTjeneste;
    private boolean revurderingMedUendretUtfall;
    private BrevParametere brevParametere;

    public LagEngangsstønadDokumentData(DokumentDataTjeneste dokumentDataTjeneste, boolean revurderingMedUendretUtfall) {
        this.dokumentDataTjeneste = dokumentDataTjeneste;
        this.revurderingMedUendretUtfall = revurderingMedUendretUtfall;
        this.brevParametere = dokumentDataTjeneste.getBrevParametere();
    }

    public Long lagDokumentData(Boolean forhåndsvis,
                                BestillVedtakBrevDto dto,
                                Behandling behandling,
                                Behandlingsresultat behandlingsresultat,
                                BehandlingVedtak behandlingVedtak) {
        DokumentType dokumentMal = null;
        if (forhåndsvis) {
            if (revurderingMedUendretUtfall) {
                dokumentMal = new UendretUtfallDokument();
            } else if (behandling.erKlage()) {
                dokumentMal = new LagKlageDokumentData(dokumentDataTjeneste).lagDokumentData(behandling, dto.getFritekst());
            } else if (behandlingsresultat.isBehandlingsresultatAvslått()) {
                dokumentMal = new AvslagVedtakDokument(brevParametere, dto.getFritekst());
            } else if (behandlingsresultat.isBehandlingsresultatInnvilget()) {
                dokumentMal = new PositivtVedtakDokument(brevParametere);
            }
        } else {
            if (behandlingVedtak.isBeslutningsvedtak()) {
                dokumentMal = new UendretUtfallDokument();
            } else if (VedtakResultatType.INNVILGET.equals(behandlingVedtak.getVedtakResultatType())) {
                dokumentMal = new PositivtVedtakDokument(brevParametere);
            } else if (VedtakResultatType.VEDTAK_I_KLAGEBEHANDLING.equals(behandlingVedtak.getVedtakResultatType())) {
                dokumentMal = new LagKlageDokumentData(dokumentDataTjeneste).opprettKlageVedtakbrev(behandlingsresultat);
            } else if (VedtakResultatType.AVSLAG.equals(behandlingVedtak.getVedtakResultatType())) {
                dokumentMal = new AvslagVedtakDokument(brevParametere, null);
            }
        }
        if (dokumentMal == null) {
            throw BrevFeil.FACTORY.ingenBrevmalKonfigurert(behandling.getId()).toException();
        }
        return dokumentDataTjeneste.lagreDokumentData(behandling.getId(), dokumentMal);
    }
}
