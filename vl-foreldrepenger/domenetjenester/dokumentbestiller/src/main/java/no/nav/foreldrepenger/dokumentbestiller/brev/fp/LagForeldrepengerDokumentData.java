package no.nav.foreldrepenger.dokumentbestiller.brev.fp;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.BrevFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.AvslagForeldrepengerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.FritekstVedtakDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnvilgelseForeldrepengerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.OpphørDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.UendretUtfallDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.brev.SjekkDokumentTilgjengelig;
import no.nav.foreldrepenger.dokumentbestiller.brev.klage.LagKlageDokumentData;

public class LagForeldrepengerDokumentData {
    private DokumentDataTjeneste dokumentDataTjeneste;
    private boolean revurderingMedUendretUtfall;
    private SjekkDokumentTilgjengelig sjekkDokumentTilgjengelig;
    private BrevParametere brevParametere;

    public LagForeldrepengerDokumentData(DokumentDataTjeneste dokumentDataTjeneste,
                                         boolean revurderingMedUendretUtfall) {
        this.sjekkDokumentTilgjengelig = new SjekkDokumentTilgjengelig(dokumentDataTjeneste);
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
            if (dto.skalBrukeOverstyrendeFritekstBrev()) {
                dokumentMal = new FritekstVedtakDokument(dto.getOverskrift(), dto.getFritekstBrev());
            } else if (revurderingMedUendretUtfall) {
                dokumentMal = new UendretUtfallDokument();
            } else if (behandling.erKlage()) {
                dokumentMal = new LagKlageDokumentData(dokumentDataTjeneste).lagDokumentData(behandling, dto.getFritekst());
            } else if (behandlingsresultat.isBehandlingsresultatAvslått()) {
                dokumentMal = new AvslagForeldrepengerDokument(brevParametere);
            } else if (behandlingsresultat.isBehandlingsresultatOpphørt()) {
                dokumentMal = new OpphørDokument(brevParametere);
            } else if (behandlingVedtak != null && behandlingVedtak.isBeslutningsvedtak()) {
                dokumentMal = new UendretUtfallDokument();
            } else if (behandlingsresultat.isBehandlingsresultatInnvilget() || skalBenytteBrevOmInnvilgelse(behandlingsresultat)) {
                dokumentMal = dto.getFritekst() == null
                    ? new InnvilgelseForeldrepengerDokument(brevParametere)
                    : new InnvilgelseForeldrepengerDokument(brevParametere, dto.getFritekst());
            } else if (behandlingsresultat.isBehandlingsresultatForeldrepengerEndret() && erKunEndringIFordelingAvYtelsen(behandlingsresultat) && harSendtVarselOmRevurdering(behandling.getId())) {
                dokumentMal = new UendretUtfallDokument();
            }
        } else {
            if (behandlingVedtak.isBeslutningsvedtak()) {
                dokumentMal = new UendretUtfallDokument();
            } else if (Vedtaksbrev.FRITEKST.equals(behandlingsresultat.getVedtaksbrev())) {
                dokumentMal = new FritekstVedtakDokument();
            } else if (VedtakResultatType.INNVILGET.equals(behandlingVedtak.getVedtakResultatType())) {
                dokumentMal = new InnvilgelseForeldrepengerDokument(brevParametere);
            } else if (VedtakResultatType.VEDTAK_I_KLAGEBEHANDLING.equals(behandlingVedtak.getVedtakResultatType())) {
                dokumentMal = new LagKlageDokumentData(dokumentDataTjeneste).opprettKlageVedtakbrev(behandlingsresultat);
            } else if (behandlingsresultat.isBehandlingsresultatOpphørt()) {
                dokumentMal = new OpphørDokument(brevParametere);
            } else if (behandlingsresultat.isBehandlingsresultatAvslått()) {
                dokumentMal = new AvslagForeldrepengerDokument(brevParametere);
            }
        }
        if (dokumentMal == null) {
            throw BrevFeil.FACTORY.ingenBrevmalKonfigurert(behandling.getId()).toException();
        }

        return dokumentDataTjeneste.lagreDokumentData(behandling.getId(), dokumentMal);
    }

    private boolean skalBenytteBrevOmInnvilgelse(Behandlingsresultat behandlingsresultat) {
        boolean foreldrepengerEndret = behandlingsresultat.isBehandlingsresultatForeldrepengerEndret();
        boolean endringIBeregningOgEllerUttak = !erKunEndringIFordelingAvYtelsen(behandlingsresultat);

        return foreldrepengerEndret && endringIBeregningOgEllerUttak;
    }

    private boolean erKunEndringIFordelingAvYtelsen(Behandlingsresultat behandlingsresultat) {
        List<KonsekvensForYtelsen> konsekvenserForYtelsen = behandlingsresultat.getKonsekvenserForYtelsen();
        if (konsekvenserForYtelsen.size() > 1) {
            return false;
        }
        return konsekvenserForYtelsen.contains(KonsekvensForYtelsen.ENDRING_I_FORDELING_AV_YTELSEN);
    }

    private Boolean harSendtVarselOmRevurdering(Long behandlingId) {
        return sjekkDokumentTilgjengelig.erDokumentProdusert(behandlingId, DokumentMalType.REVURDERING_DOK);
    }
}
