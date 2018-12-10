package no.nav.foreldrepenger.behandling.revurdering.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.fp.impl.RevurderingTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class RevurderingTjenesteImplTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepositoryImpl(repoRule.getEntityManager());
    private HistorikkRepository historikkRepository = spy(repositoryProvider.getHistorikkRepository());

    @Inject
    private RevurderingEndring revurderingEndring;

    @Test
    public void skal_opprette_revurdering() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS, BehandlingStegType.KONTROLLER_FAKTA);
        Behandling behandlingSomSkalRevurderes = scenario.lagre(repositoryProvider);
        scenario.avsluttBehandling(repositoryProvider, behandlingSomSkalRevurderes);

        final BehandlingskontrollTjenesteImpl behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            behandlingModellRepository, null);
        RevurderingTjeneste revurderingTjeneste = new RevurderingTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, historikkRepository, revurderingEndring);

        // Act
        Behandling revurdering = revurderingTjeneste
            .opprettAutomatiskRevurdering(behandlingSomSkalRevurderes.getFagsak(), BehandlingÅrsakType.RE_ANNET);

        // Assert
        assertThat(revurdering.getFagsak()).isEqualTo(behandlingSomSkalRevurderes.getFagsak());
        assertThat(revurdering.getBehandlingÅrsaker().get(0).getBehandlingÅrsakType()).isEqualTo(BehandlingÅrsakType.RE_ANNET);
        assertThat(revurdering.getType()).isEqualTo(BehandlingType.REVURDERING);
        assertThat(revurdering.getAlleAksjonspunkterInklInaktive()).hasSize(1);
        Aksjonspunkt aksjonspunkt = revurdering.getAlleAksjonspunkterInklInaktive().iterator().next();
        assertThat(aksjonspunkt.getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS);
        assertThat(aksjonspunkt.erAktivt()).isFalse();
    }


}
