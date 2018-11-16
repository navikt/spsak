package no.nav.foreldrepenger.behandling.revurdering.impl;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class RevurderingEndringFPTest {

    @Rule
    public final UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private RevurderingEndring revurderingEndringFP = new RevurderingEndringFP();
    private Behandling originalBehandling;
    private Behandling revurdering;

    @Before
    public void setup() {
        originalBehandling = opprettOriginalBehandling();
        revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_MANGLER_FØDSEL).medOriginalBehandling(originalBehandling)).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);
    }

    @Test
    public void jaHvisRevurderingMedUendretUtfall() {
        Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.INGEN_ENDRING)
            .buildFor(revurdering);

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);

        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(revurdering)).isTrue();
    }

    @Test
    public void kasterFeilHvisRevurderingMedUendretUtfallOgOpphørAvYtelsen() {
        // Arrange
        Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.INGEN_ENDRING)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.FORELDREPENGER_OPPHØRER)
            .buildFor(revurdering);

        // Assert
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(RevurderingEndringFP.UTVIKLERFEIL_INGEN_ENDRING_SAMMEN);

        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);

        // Assert
        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(revurdering)).isFalse();
    }

    @Test
    public void neiHvisRevurderingMedEndring() {
        Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.FORELDREPENGER_ENDRET)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.ENDRING_I_BEREGNING)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.ENDRING_I_UTTAK)
            .buildFor(revurdering);

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);

        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(revurdering)).isFalse();
    }

    @Test
    public void neiHvisRevurderingMedOpphør() {
        Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.OPPHØR)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.FORELDREPENGER_OPPHØRER)
            .buildFor(revurdering);

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);

        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(revurdering)).isFalse();
    }

    @Test
    public void neiHvisFørstegangsbehandling() {
        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(originalBehandling)).isFalse();
    }

    private Behandling opprettOriginalBehandling() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forFødsel();
        Behandling originalBehandling = scenario.lagre(repositoryProvider);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(originalBehandling);
        behandlingRepository.lagre(originalBehandling, behandlingLås);
        return originalBehandling;
    }
}
