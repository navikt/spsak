package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class BehandlingEntitetTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    @Before
    public void setup() {
    }

    @Test
    public void skal_opprette_ny_behandling_på_ny_fagsak() {

        Behandling behandling = opprettOgLagreBehandling();

        List<Behandling> alle = repository.hentAlle(Behandling.class);

        assertThat(alle).hasSize(1);

        Behandling første = alle.get(0);

        assertThat(første).isEqualTo(behandling);
    }

    private Behandling opprettOgLagreBehandling() {
        final ScenarioMorSøkerEngangsstønad scenarioMorSøkerEngangsstønad = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        return scenarioMorSøkerEngangsstønad.lagre(repositoryProvider);
    }

    @Test
    public void skal_opprette_ny_behandling_på_fagsak_med_tidligere_behandling() {

        Behandling behandling = opprettOgLagreBehandling();

        Behandling behandling2 = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING).build();
        lagreBehandling(behandling2);

        List<Behandling> alle = repository.hentAlle(Behandling.class);

        assertThat(alle).hasSize(2);

        Behandling første = alle.get(0);
        Behandling andre = alle.get(1);

        assertThat(første).isNotEqualTo(andre);
    }

    private void lagreBehandling(Behandling behandling) {
        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        repositoryProvider.getBehandlingRepository().lagre(behandling, lås);
    }

    @Test
    public void skal_opprette_ny_behandling_med_søknad() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        scenario.lagre(repositoryProvider);

        List<Behandling> alle = repository.hentAlle(Behandling.class);

        assertThat(alle).hasSize(1);

        Behandling første = alle.get(0);
        final Søknad søknad = repositoryProvider.getSøknadRepository().hentSøknad(første);
        assertThat(søknad).isNotNull();
        assertThat(søknad.getSøknadsdato()).isEqualTo(LocalDate.now());
    }

    @Test
    public void skal_ikke_opprette_nytt_behandlingsgrunnlag_når_endring_skjer_på_samme_behandling_som_originalt_lagd_for() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);

        lagreBehandling(behandling);

        // Ny behandling gir samme grunnlag når det ikke er endringer
        Behandling.Builder behandlingBuilder2 = Behandling.fraTidligereBehandling(behandling, BehandlingType.FØRSTEGANGSSØKNAD);

        Behandling behandling2 = behandlingBuilder2.build();
        lagreBehandling(behandling2);
    }
}
