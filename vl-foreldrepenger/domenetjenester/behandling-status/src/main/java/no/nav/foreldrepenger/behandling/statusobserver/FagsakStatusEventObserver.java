package no.nav.foreldrepenger.behandling.statusobserver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;

/**
 * Observerer og propagerer / håndterer events internt i Behandlingskontroll
 */
@ApplicationScoped
public class FagsakStatusEventObserver {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private OppdaterFagsakStatusProvider oppdaterFagsakStatusProvider;
    private BehandlingRepository behandlingRepository;

    FagsakStatusEventObserver() {
        // For CDI
    }

    @Inject
    public FagsakStatusEventObserver(OppdaterFagsakStatusProvider oppdaterFagsakStatusProvider, BehandlingRepository behandlingRepository) {
        this.oppdaterFagsakStatusProvider = oppdaterFagsakStatusProvider;
        this.behandlingRepository = behandlingRepository;
    }

    public void observerBehandlingOpprettetEvent(@Observes BehandlingStatusEvent.BehandlingOpprettetEvent event) {
        log.debug("Oppdaterer status på Fagsak etter endring i behandling {}", event.getBehandlingId());//NOSONAR
        Behandling behandling = behandlingRepository.hentBehandling(event.getBehandlingId());
        OppdaterFagsakStatus oppdaterFagsakStatus = oppdaterFagsakStatusProvider.getOppdaterFagsakStatus(behandling);
        oppdaterFagsakStatus.oppdaterFagsakNårBehandlingEndret(behandling);
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingAvsluttetEvent event) {
        log.debug("Oppdaterer status på Fagsak etter endring i behandling {}", event.getBehandlingId());//NOSONAR
        Behandling behandling = behandlingRepository.hentBehandling(event.getBehandlingId());
        OppdaterFagsakStatus oppdaterFagsakStatus = oppdaterFagsakStatusProvider.getOppdaterFagsakStatus(behandling);
        oppdaterFagsakStatus.oppdaterFagsakNårBehandlingEndret(behandling);
    }
}
