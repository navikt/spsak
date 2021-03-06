package no.nav.foreldrepenger.behandling.revurdering;


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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class RevurderingEndringTest {

    @Rule
    public final UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private RevurderingEndring revurderingEndringFP = new RevurderingEndring();
    private Behandling originalBehandling;
    private Behandling revurdering;

    @Before
    public void setup() {
        originalBehandling = opprettOriginalBehandling();
        revurdering = Behandling.fraTidligereBehandling(originalBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ANNET).medOriginalBehandling(originalBehandling)).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);
    }

    @Test
    public void jaHvisRevurderingMedUendretUtfall() {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.INGEN_ENDRING)
            .buildFor(revurdering);

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(revurdering, behandlingsresultat)).isTrue();
    }

    @Test
    public void kasterFeilHvisRevurderingMedUendretUtfallOgOpphørAvYtelsen() {
        // Arrange
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.INGEN_ENDRING)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.YTELSE_OPPHØRER)
            .buildFor(revurdering);

        // Assert
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(RevurderingEndring.UTVIKLERFEIL_INGEN_ENDRING_SAMMEN);

        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        // Assert
        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(revurdering, behandlingsresultat)).isFalse();
    }

    @Test
    public void neiHvisRevurderingMedEndring() {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.FORELDREPENGER_ENDRET)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.ENDRING_I_BEREGNING)
            .buildFor(revurdering);

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(revurdering, behandlingsresultat)).isFalse();
    }

    @Test
    public void neiHvisRevurderingMedOpphør() {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.OPPHØR)
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.YTELSE_OPPHØRER)
            .buildFor(revurdering);

        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(revurdering, behandlingsresultat)).isFalse();
    }

    @Test
    public void neiHvisFørstegangsbehandling() {
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(originalBehandling.getId());
        assertThat(revurderingEndringFP.erRevurderingMedUendretUtfall(originalBehandling, behandlingsresultat)).isFalse();
    }

    private Behandling opprettOriginalBehandling() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forDefaultAktør();
        Behandling originalBehandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(originalBehandling);
        behandlingRepository.lagre(originalBehandling, behandlingLås);
        return originalBehandling;
    }
}
