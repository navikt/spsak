package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;

public interface TestScenarioResultatTillegg {
    void lagre(Behandling behandling, ResultatRepositoryProvider repositoryProvider);
}
