package no.nav.foreldrepenger.domene.vedtak;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtak;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtakMedBehandlingType;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtakType;

public interface VedtakTjeneste {
    List<LagretVedtakMedBehandlingType> hentLagreteVedtakPåFagsak(Long fagsakId);

    LagretVedtak hentLagretVedtak(Long lagretVedtakId);

    void lagHistorikkinnslagFattVedtak(Behandling behandling);

    LagretVedtakType finnLagretVedtakType(Behandling behandling);

    VedtakResultatType utledVedtakResultatType(Behandling behandling);
}
