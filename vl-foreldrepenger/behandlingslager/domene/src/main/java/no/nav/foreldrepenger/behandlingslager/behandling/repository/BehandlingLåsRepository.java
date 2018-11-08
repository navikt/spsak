package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;

/**
 * @see BehandlingLås
 */
public interface BehandlingLåsRepository extends BehandlingslagerRepository {

    /** Initialiser lås og ta lock på tilhørende database rader. */
    BehandlingLås taLås(Long behandlingId);

    /**
     * Verifiser lås ved å sjekke mot underliggende lager.
     */
    void oppdaterLåsVersjon(BehandlingLås behandlingLås);

}
