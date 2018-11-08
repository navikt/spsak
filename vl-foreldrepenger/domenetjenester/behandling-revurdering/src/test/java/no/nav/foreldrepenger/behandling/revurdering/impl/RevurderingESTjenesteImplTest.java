package no.nav.foreldrepenger.behandling.revurdering.impl;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.es.impl.RevurderingESTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class RevurderingESTjenesteImplTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepositoryImpl(repoRule.getEntityManager());
    private HistorikkRepository historikkRepository = repositoryProvider.getHistorikkRepository();
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private RevurderingTjeneste revurderingTjeneste;
    private Behandling behandlingSomSkalRevurderes;
    @Inject
    @FagsakYtelseTypeRef("ES")
    private RevurderingEndring revurderingEndringES;

    @Before
    public void setup() {
        opprettRevurderingsKandidat();
    }
    @Test
    public void skal_opprette_automatisk_revurdering_basert_på_siste_innvilgede_behandling() {
        final BehandlingskontrollTjenesteImpl behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            behandlingModellRepository, null);
        revurderingTjeneste = new RevurderingESTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, historikkRepository, revurderingEndringES);
        final Behandling revurdering = revurderingTjeneste.opprettAutomatiskRevurdering(behandlingSomSkalRevurderes.getFagsak(), BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN);

        assertThat(revurdering.getFagsak()).isEqualTo(behandlingSomSkalRevurderes.getFagsak());
        assertThat(revurdering.getBehandlingÅrsaker().get(0).getBehandlingÅrsakType()).isEqualTo(BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN);
    }

    @Test
    public void skal_opprette_manuell_behandling_med_saksbehandler_som_historikk_aktør() {
        final BehandlingskontrollTjenesteImpl behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            behandlingModellRepository, null);
        revurderingTjeneste = new RevurderingESTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, historikkRepository, revurderingEndringES);
        final Behandling revurdering = revurderingTjeneste.opprettManuellRevurdering(behandlingSomSkalRevurderes.getFagsak(), BehandlingÅrsakType.RE_MANGLER_FØDSEL_I_PERIODE);

        assertThat(revurdering.getFagsak()).isEqualTo(behandlingSomSkalRevurderes.getFagsak());
        assertThat(revurdering.getBehandlingÅrsaker().get(0).getBehandlingÅrsakType()).isEqualTo(BehandlingÅrsakType.RE_MANGLER_FØDSEL_I_PERIODE);
    }

    private void opprettRevurderingsKandidat() {

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.buildAvsluttet(behandlingRepository, repositoryProvider);
        repoRule.getRepository().flushAndClear();
        behandlingSomSkalRevurderes = repoRule.getEntityManager().find(Behandling.class, scenario.getBehandling().getId());
    }
}
