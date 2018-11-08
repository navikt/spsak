package no.nav.foreldrepenger.vedtakslager;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtak;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtakMedBehandlingType;

public interface LagretVedtakRepository extends BehandlingslagerRepository {

    long lagre(LagretVedtak lagretVedtak);

    LagretVedtak hentLagretVedtak(long lagretVedtakId);

    LagretVedtak hentLagretVedtakForBehandling(long behandlingId);

    List<LagretVedtakMedBehandlingType> hentLagreteVedtakPåFagsak(long fagsakId);
}
