package no.nav.foreldrepenger.behandling.status.observer;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface OppdaterFagsakStatus {
    void oppdaterFagsakNårBehandlingEndret(Behandling behandling);
}
