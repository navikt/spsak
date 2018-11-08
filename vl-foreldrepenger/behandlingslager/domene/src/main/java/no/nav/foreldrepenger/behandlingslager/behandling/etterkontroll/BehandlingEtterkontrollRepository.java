package no.nav.foreldrepenger.behandlingslager.behandling.etterkontroll;

import java.time.Period;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;

/**
 * Oppdatering av tilstand for etterkontroll av behandling.
 */
public interface BehandlingEtterkontrollRepository {

    List<Behandling> finnKandidaterForAutomatiskEtterkontroll(Period etterkontrollTidTilbake);


    /**
     * Lagrer etterkontroll logginslag på en behandling
     *
     * @return id for {@link EtterkontrollLogg} opprettet
     */
    Long lagre(EtterkontrollLogg etterkontrollLogg, BehandlingLås lås);
}
