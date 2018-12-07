package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingskontrollTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.StegTilstand;

public interface BehandlingskontrollRepository {

    /** Avslutter behandling. */
    void avsluttBehandling(Long behandlingId);

    /**
     * Finn aktivt behandling steg (hvis finnes), ellers Optional.empty().
     * 
     * @deprecated Bruk {@link #getBehandlingskontrollTilstand(Long)}
     */
    @Deprecated(forRemoval = true)
    Optional<BehandlingStegTilstand> getAktivtBehandlingStegTilstand(Long behandlingId);

    BehandlingskontrollTilstand getBehandlingskontrollTilstand(Long behandlingId);

    List<BehandlingStegTilstand> getBehandlingStegTilstandHistorikk(Long id);

    /** Utfør eksisterende steg og oppdaterer tilstand for steg ved fremføring til nytt steg (hopper over mellomliggende steg). */
    void nesteBehandlingStegStatusVedFremføring(Long behandlingId, StegTilstand nyttSteg);

    /** Utfør eksisterende steg og oppdaterer tilstand for steg. */
    void nesteBehandlingStegStatusVedTilbakeføring(Long behandlingId, StegTilstand nyttSteg);

    /** Utfør eksisterende steg og oppdaterer tilstand for steg. */
    void nesteBehandlingStegStatusVedUtført(Long behandlingId, StegTilstand nyttSteg);

    /** Oppdater behandling steg status internt (samme steg). */
    void nesteBehandlingStegStatusIntern(Long behandlingId, BehandlingStegType stegType, BehandlingStegStatus nyStegStatus);

}
