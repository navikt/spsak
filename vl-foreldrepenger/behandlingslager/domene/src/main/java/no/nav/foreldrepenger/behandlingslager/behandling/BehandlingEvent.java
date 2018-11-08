package no.nav.foreldrepenger.behandlingslager.behandling;

import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakEvent;

/**
 * Marker interface for events fyrt på en Behandling.
 * Disse fyres ved hjelp av CDI Events.
 */
public interface BehandlingEvent extends FagsakEvent {

    Long getBehandlingId();

}
