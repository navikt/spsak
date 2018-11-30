package no.nav.foreldrepenger.behandlingslager.fagsak;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

@ApplicationScoped
public class FagsakStatusEventPubliserer {
    private static final Logger log = LoggerFactory.getLogger(FagsakStatusEventPubliserer.class);

    private Event<FagsakStatusEvent> fagsakStatusEvent;

    FagsakStatusEventPubliserer() {
        // for CDI
    }

    @Inject
    public FagsakStatusEventPubliserer(Event<FagsakStatusEvent> fagsakStatusEvent) {
        this.fagsakStatusEvent = fagsakStatusEvent;
    }

    public void fireEvent(Fagsak fagsak, Behandling behandling, FagsakStatus gammelStatusIn, FagsakStatus nyStatusIn) {
        if ((gammelStatusIn == null && nyStatusIn == null) // NOSONAR
                || Objects.equals(gammelStatusIn, nyStatusIn)) { // NOSONAR
            // gjør ingenting
            return;
        } else if (gammelStatusIn == null && nyStatusIn != null) {// NOSONAR
            log.info("Fagsak status opprettet: id [{}]; type [{}];", fagsak.getId(), fagsak.getYtelseType());
        } else {
            Long fagsakId = fagsak.getId();
            String gammelStatus = gammelStatusIn.getKode(); // NOSONAR false positive NPE dereference
            String nyStatus = nyStatusIn == null ? null : nyStatusIn.getKode();

            if (behandling != null) {
                log.info("Fagsak status oppdatert: {} -> {}; fagsakId [{}] behandlingId [{}]", gammelStatus, nyStatus, fagsakId, //$NON-NLS-1$
                        behandling.getId());
            } else {
                log.info("Fagsak status oppdatert: {} -> {}; fagsakId [{}]", gammelStatus, nyStatus, fagsakId); //$NON-NLS-1$
            }
        }

        FagsakStatusEvent event = new FagsakStatusEvent(fagsak.getId(), fagsak.getAktørId(), gammelStatusIn, nyStatusIn);
        fagsakStatusEvent.fire(event);
    }

    public void fireEvent(Fagsak fagsak, FagsakStatus status) {
        fireEvent(fagsak, null, null, status);
    }
}
