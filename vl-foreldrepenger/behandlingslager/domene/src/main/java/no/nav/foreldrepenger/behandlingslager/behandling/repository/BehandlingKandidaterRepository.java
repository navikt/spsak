package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface BehandlingKandidaterRepository {

    List<Behandling> finnBehandlingerForAutomatiskGjenopptagelse();

    List<Behandling> finnBehandlingerMedUtløptBehandlingsfrist();

}
