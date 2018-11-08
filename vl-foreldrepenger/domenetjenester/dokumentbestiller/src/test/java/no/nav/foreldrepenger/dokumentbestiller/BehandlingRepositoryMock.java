package no.nav.foreldrepenger.dokumentbestiller;

import javax.persistence.EntityManager;

import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryImpl;

class BehandlingRepositoryMock extends BehandlingRepositoryImpl {

    private Behandling behandling;

    BehandlingRepositoryMock(Behandling behandling) {
        super(Mockito.mock(EntityManager.class));
        this.behandling = behandling;
    }

    @Override
    public Behandling hentBehandling(Long behandlingId) {
        return behandling;
    }
}
