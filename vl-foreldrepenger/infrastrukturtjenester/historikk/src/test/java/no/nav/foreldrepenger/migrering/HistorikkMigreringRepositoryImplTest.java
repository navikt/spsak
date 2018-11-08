package no.nav.foreldrepenger.migrering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringRepository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class HistorikkMigreringRepositoryImplTest {
    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();
    private EntityManager entityManager = repositoryRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private HistorikkMigreringRepository historikkMigreringRepository = new HistorikkMigreringRepositoryImpl(entityManager);
    private HistorikkMigreringTestdataBuilder testDataProvider = new HistorikkMigreringTestdataBuilder(historikkMigreringRepository, repositoryProvider.getAksjonspunktRepository());

    @Before
    public void setup() {
        // oppretter 6 historikkinnslag
        for (int i = 0; i < 3; i++) {
            opprettBehandlingMedHistorikkinnslag();
        }
        entityManager.flush();
    }

    @Test
    public void skal_hente_alle_historikkinnslag() {
        // Act
        Iterator<Historikkinnslag> alleHistorikkinnslag = historikkMigreringRepository.hentAlleHistorikkinnslag();
        assertThat(alleHistorikkinnslag).hasSize(18);
    }

    private void opprettBehandlingMedHistorikkinnslag() {
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forFødsel().lagre(repositoryProvider);
        testDataProvider.opprettHistorikkinnslag(behandling);
    }
}
