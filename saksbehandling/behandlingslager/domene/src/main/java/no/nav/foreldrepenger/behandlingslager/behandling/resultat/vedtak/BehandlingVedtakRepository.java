package no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;

public interface BehandlingVedtakRepository {

    Optional<BehandlingVedtak> hentVedtakFor(Long behandlingsresultatId);

    BehandlingVedtak hentVedtakFraRevurderingensOriginaleBehandling(Behandling revurdering);

    /**
     * Lagrer vedtak på behandling. Sørger for at samtidige oppdateringer på samme Behandling, eller andre Behandlinger
     * på samme Fagsak ikke kan gjøres samtidig.
     *
     * @see BehandlingLås
     */
    Long lagre(BehandlingVedtak vedtak, BehandlingLås lås);

}
