package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

public interface BehandlingskontrollRepository {

    /** Avslutter behandling. */
    void avsluttBehandling(Long behandlingId);
    
    /** Utfør eksisterende steg og oppdaterer tilstand for steg. */
    void nesteBehandlingStegStatusVedUtført(Long behandlingId, BehandlingStegTilstand nyttSteg);
    
    /** Utfør eksisterende steg og oppdaterer tilstand for steg. */
    void nesteBehandlingStegStatusVedTilbakeføring(Long behandlingId, BehandlingStegTilstand nyttSteg);
    
    /** Utfør eksisterende steg og oppdaterer tilstand for steg ved fremføring til nytt steg (hopper over mellomliggende steg). */
    void nesteBehandlingStegStatusVedFremføring(Long behandlingId, BehandlingStegTilstand nyttSteg);

    /** Finn aktivt behandling steg (hvis finnes), ellers Optional.empty(). */
    Optional<BehandlingStegTilstand> getAktivtBehandlingStegTilstand(Long behandlingId);
    
    /** Finn aktivt behandling steg (hvis finnes), ellers Optional.empty(). */
    Optional<BehandlingStegTilstand> getAktivtBehandlingStegTilstand(Long behandlingId, BehandlingStegType stegType);

    /** Finn aktivt behandling steg (hvis finnes), ellers kast exceptino hvis ikke finnes. */
    BehandlingStegTilstand getAktivtBehandlingStegTilstandDefinitiv(Long behandlingId);
    
    /** Finn aktivt behandling steg (hvis finnes) med angitt stegType, ellers kast exception hvis ikke finnes. */
    BehandlingStegTilstand getAktivtBehandlingStegTilstandDefinitiv(Long behandlingId, BehandlingStegType stegType);

    List<BehandlingStegTilstand> getBehandlingStegTilstandHistorikk(Long id);
    
}
