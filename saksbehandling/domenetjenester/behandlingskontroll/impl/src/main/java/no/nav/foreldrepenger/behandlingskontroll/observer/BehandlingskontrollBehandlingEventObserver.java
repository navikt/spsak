package no.nav.foreldrepenger.behandlingskontroll.observer;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegOvergangEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegStatusEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;

/**
 * Observerer og propagerer / håndterer events internt i Behandlingskontroll
 */
@ApplicationScoped
public class BehandlingskontrollBehandlingEventObserver {
    private static final Logger log = LoggerFactory.getLogger(BehandlingskontrollBehandlingEventObserver.class);

    private BehandlingskontrollEventPubliserer eventPubliserer;

    BehandlingskontrollBehandlingEventObserver() {
    }

    @Inject
    public BehandlingskontrollBehandlingEventObserver(BehandlingskontrollEventPubliserer eventPubliserer) {
        this.eventPubliserer = eventPubliserer;
    }

    /**
     * Intern event propagering i Behandlingskontroll.
     * 
     * Observer {@link BehandlingStegOvergangEvent} og propagerer events for {@link BehandlingStegStatusEvent} og
     * {@link BehandlingStatusEvent}
     * endringer
     */
    public void propagerBehandlingStatusEventVedStegOvergang(@Observes BehandlingStegOvergangEvent event) {

        if (eventPubliserer == null) {
            // gjør ingenting
            return;
        }

        Optional<BehandlingStegTilstand> fraTilstand = event.getFraTilstand();
        Optional<BehandlingStegTilstand> tilTilstand = event.getTilTilstand();

        if ((!fraTilstand.isPresent() && !tilTilstand.isPresent())
                || (fraTilstand.isPresent() && tilTilstand.isPresent() && Objects.equals(fraTilstand.get(), tilTilstand.get()))) {
            // gjør ingenting - ingen endring i steg
            return;
        }
        
        log.info("transisjon fra {} til {}", fraTilstand, tilTilstand);

        // fyr behandling status event
        BehandlingStatus gammelStatus = null;
        if (fraTilstand.isPresent()) {
            gammelStatus = fraTilstand.get().getStegType().getDefinertBehandlingStatus();
        }
        BehandlingStatus nyStatus = null;
        if (tilTilstand.isPresent()) {
            nyStatus = tilTilstand.get().getStegType().getDefinertBehandlingStatus();
        }

        // fyr behandling status event
        if (!Objects.equals(gammelStatus, nyStatus)) {
            eventPubliserer.fireEvent(event.getKontekst(), gammelStatus, nyStatus);
        }
    }
}
