package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class BehandlingsresultatTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private Fagsak fagsak = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("909"))).build();
    private Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
    private Behandling behandling;

    @Before
    public void setup() {
        repository.lagre(fagsak.getNavBruker());
        repository.lagre(fagsak);
        repository.flush();

        behandling = behandlingBuilder.build();
    }

    @Test
    public void skal_opprette_ny_behandlingsresultat() throws Exception {

        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderForInngangsvilkår();
        Behandlingsresultat behandlingsresultat = behandlingsresultatBuilder.buildFor(behandling);

        assertThat(behandlingsresultat).isNotNull();
        assertThat(behandlingsresultat.getVilkårResultat()).isNotNull();
    }

    @Test
    public void skal_opprette_ny_behandlingsresultat_og_lagre_med_ikke_fastsatt_vilkårresultat() throws Exception {
        lagreBehandling(behandling);

        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderForInngangsvilkår();
        Behandlingsresultat behandlingsresultat = behandlingsresultatBuilder.buildFor(behandling);

        assertThat(behandling.getBehandlingsresultat()).isEqualTo(behandlingsresultat);
        assertThat(behandlingsresultat.getBehandling()).isNotNull();
        assertThat(behandlingsresultat.getVilkårResultat().getVilkårResultatType()).isEqualTo(VilkårResultatType.IKKE_FASTSATT);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        lagreBehandling(behandling);

        Long id = behandling.getId();
        assertThat(id).isNotNull();

        Behandling lagretBehandling = repository.hent(Behandling.class, id);
        assertThat(lagretBehandling).isEqualTo(behandling);
        assertThat(lagretBehandling.getBehandlingsresultat()).isEqualTo(behandlingsresultat);

    }

    private void lagreBehandling(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
    }

}
