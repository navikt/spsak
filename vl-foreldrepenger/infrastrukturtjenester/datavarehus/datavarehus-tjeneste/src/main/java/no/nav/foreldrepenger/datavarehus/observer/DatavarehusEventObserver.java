package no.nav.foreldrepenger.datavarehus.observer;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktTilbakeførtEvent;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktUtførtEvent;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegTilstandEndringEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEvent;
import no.nav.foreldrepenger.datavarehus.tjeneste.DatavarehusTjeneste;

@ApplicationScoped
public class DatavarehusEventObserver {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private DatavarehusTjeneste tjeneste;

    public DatavarehusEventObserver() {
        //Cool Devices Installed
    }

    @Inject
    public DatavarehusEventObserver(DatavarehusTjeneste datavarehusTjeneste) {
        this.tjeneste = datavarehusTjeneste;
    }

    /*

    public void observerAksjonspunktUtførtEvent(@Observes AksjonspunktUtførtEvent event) {
        List<Aksjonspunkt> aksjonspunkter = event.getAksjonspunkter();
        log.debug("Lagrer {} aksjonspunkter i DVH datavarehus, for behandling {} og steg {}", aksjonspunkter.size(), event.getBehandlingId(), event.getBehandlingStegType());//NOSONAR
        tjeneste.lagreNedAksjonspunkter(aksjonspunkter, event.getBehandlingId(), event.getBehandlingStegType());
    }

    public void observerAksjonspunkterFunnetEvent(@Observes AksjonspunkterFunnetEvent event) {
        List<Aksjonspunkt> aksjonspunkter = event.getAksjonspunkter();
        log.debug("Lagrer {} aksjonspunkter i DVH datavarehus, for behandling {} og steg {}", aksjonspunkter.size(), event.getBehandlingId(), event.getBehandlingStegType());//NOSONAR
        tjeneste.lagreNedAksjonspunkter(aksjonspunkter, event.getBehandlingId(), event.getBehandlingStegType());
    }

    public void observerAksjonspunktTilbakeførtEvent(@Observes AksjonspunktTilbakeførtEvent event) {
        List<Aksjonspunkt> aksjonspunkter = event.getAksjonspunkter();
        log.debug("Lagrer {} aksjonspunkter i DVH datavarehus, for behandling {} og steg {}", aksjonspunkter.size(), event.getBehandlingId(), event.getBehandlingStegType());//NOSONAR
        tjeneste.lagreNedBehandlingOgTilstander(event.getBehandlingId());
        tjeneste.lagreNedAksjonspunkter(aksjonspunkter, event.getBehandlingId(), event.getBehandlingStegType());
    }

    public void observerFagsakStatus(@Observes FagsakStatusEvent event) {
        log.debug("Lagrer fagsak {} i DVH mellomalger", event.getFagsakId());//NOSONAR
        tjeneste.lagreNedFagsak(event.getFagsakId());
    }

    public void observerBehandlingStegTilstandEndringEvent(@Observes BehandlingStegTilstandEndringEvent event) {
        Optional<BehandlingStegTilstand> fraTilstand = event.getFraTilstand();
        if (fraTilstand.isPresent()) {
            BehandlingStegTilstand tilstand = fraTilstand.get();
            log.debug("Lagrer behandligsteg endring fra tilstand {} i DVH datavarehus for behandling {}; behandlingStegTilstandId {}", //NOSONAR
                tilstand.getBehandlingSteg().getKode(), event.getBehandlingId(), tilstand.getId());
            tjeneste.lagreNedBehandlingStegTilstand(tilstand);
        }
        Optional<BehandlingStegTilstand> tilTilstand = event.getTilTilstand();
        if (tilTilstand.isPresent()) {
            BehandlingStegTilstand tilstand = tilTilstand.get();
            log.debug("Lagrer behandligsteg endring til tilstand {} i DVH datavarehus for behandlingId {}; behandlingStegTilstandId {}", //NOSONAR
                tilstand.getBehandlingSteg().getKode(), event.getBehandlingId(), tilstand.getId());
            tjeneste.lagreNedBehandlingStegTilstand(tilstand);
        }
    }

    public void observerBehandlingOpprettetEvent(@Observes BehandlingStatusEvent.BehandlingOpprettetEvent event) {
        log.debug("Lagrer behandling {} i DVH datavarehus", event.getBehandlingId());//NOSONAR
        tjeneste.lagreNedBehandling(event.getBehandlingId());
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingAvsluttetEvent event) {
        log.debug("Lagrer behandling {} i DVH datavarehus", event.getBehandlingId());//NOSONAR
        tjeneste.lagreNedBehandlingOgTilstander(event.getBehandlingId());
    }

    public void observerBehandlingVedtakEvent(@Observes BehandlingVedtakEvent event) {
        log.debug("Lagrer vedtak {} for behandling {} i DVH datavarehus", event.getVedtak().getId(), event.getBehandlingId());//NOSONAR
        tjeneste.lagreNedVedtak(event.getVedtak(), event.getBehandlingId());
    }

    */


}
