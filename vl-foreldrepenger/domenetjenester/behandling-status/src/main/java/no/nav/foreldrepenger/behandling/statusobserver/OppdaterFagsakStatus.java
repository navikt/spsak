package no.nav.foreldrepenger.behandling.statusobserver;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface OppdaterFagsakStatus {
    void oppdaterFagsakNårBehandlingEndret(Behandling behandling);
}
