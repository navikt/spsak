package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

public interface TestScenarioTillegg {
    void lagre(Behandling behandling, BehandlingRepositoryProvider repositoryProvider);
}
