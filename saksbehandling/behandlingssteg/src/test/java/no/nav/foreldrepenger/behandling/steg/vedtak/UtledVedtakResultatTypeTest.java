package no.nav.foreldrepenger.behandling.steg.vedtak;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.vedtak.util.Tuple;

public class UtledVedtakResultatTypeTest {
    private ScenarioMorSøkerEngangsstønad scenarioFørstegang;

    @Before
    public void setup() {
        scenarioFørstegang = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
    }

    @Test
    public void vedtakResultatTypeSettesTilAVSLAG() {
        // Arrange
        scenarioFørstegang.medBehandlingsresultat(Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT));
        Behandling behandling = scenarioFørstegang.lagMocked();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenarioFørstegang.mockBehandlingRepositoryProvider();
        GrunnlagRepositoryProvider grunnlagRepositoryProvider = providerTuple.getElement1();

        // Act
        VedtakResultatType vedtakResultatType = UtledVedtakResultatType.utled(grunnlagRepositoryProvider.getBehandlingRepository(), behandling);

        // Assert
        assertThat(vedtakResultatType).isEqualTo(VedtakResultatType.AVSLAG);
    }

    @Test
    public void vedtakResultatTypeSettesTilINNVILGETForInnvilget() {
        // Arrange
        scenarioFørstegang.medBehandlingsresultat(Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET));
        Behandling behandling = scenarioFørstegang.lagMocked();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenarioFørstegang.mockBehandlingRepositoryProvider();
        GrunnlagRepositoryProvider grunnlagRepositoryProvider = providerTuple.getElement1();

        // Act
        VedtakResultatType vedtakResultatType = UtledVedtakResultatType.utled(grunnlagRepositoryProvider.getBehandlingRepository(), behandling);

        // Assert
        assertThat(vedtakResultatType).isEqualTo(VedtakResultatType.INNVILGET);
    }

    @Test
    public void vedtakResultatTypeSettesTilINNVILGETForForeldrepengerEndret() {
        // Arrange
        scenarioFørstegang.medBehandlingsresultat(Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.FORELDREPENGER_ENDRET));
        Behandling behandling = scenarioFørstegang.lagMocked();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenarioFørstegang.mockBehandlingRepositoryProvider();
        GrunnlagRepositoryProvider grunnlagRepositoryProvider = providerTuple.getElement1();

        // Act
        VedtakResultatType vedtakResultatType = UtledVedtakResultatType.utled(grunnlagRepositoryProvider.getBehandlingRepository(), behandling);

        // Assert
        assertThat(vedtakResultatType).isEqualTo(VedtakResultatType.INNVILGET);
    }

    /**
     * Behandling 1: Avslått
     * Behandling 2: Ingen endring
     */
    @Test
    public void vedtakResultatTypeSettesTilAVSLAGForIngenEndringNårForrigeBehandlingAvslått() {
        // Arrange
        scenarioFørstegang.medBehandlingsresultat(Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT));
        Behandling behandling1 = scenarioFørstegang.lagMocked();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenarioFørstegang.mockBehandlingRepositoryProvider();
        GrunnlagRepositoryProvider grunnlagRepositoryProvider = providerTuple.getElement1();
        BehandlingRepository behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
        Behandling behandling2 = lagRevurdering(behandlingRepository, behandling1, BehandlingResultatType.INGEN_ENDRING);


        // Act
        VedtakResultatType vedtakResultatType = UtledVedtakResultatType.utled(behandlingRepository, behandling2);

        // Assert
        assertThat(vedtakResultatType).isEqualTo(VedtakResultatType.AVSLAG);
    }

    /**
     * Behandling 1: Innvilget
     * Behandling 2: Ingen endring
     * Behandling 3: Ingen endring
     */
    @Test
    public void vedtakResultatTypeSettesTilAVSLAGForIngenEndringNårBehandling1InnvilgetOgBehandling2IngenEndring() {
        // Arrange
        scenarioFørstegang.medBehandlingsresultat(Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET));
        Behandling behandling1 = scenarioFørstegang.lagMocked();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenarioFørstegang.mockBehandlingRepositoryProvider();
        GrunnlagRepositoryProvider grunnlagRepositoryProvider = providerTuple.getElement1();
        BehandlingRepository behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
        Behandling behandling2 = lagRevurdering(behandlingRepository, behandling1, BehandlingResultatType.INGEN_ENDRING);
        Behandling behandling3 = lagRevurdering(behandlingRepository, behandling2, BehandlingResultatType.INGEN_ENDRING);

        // Act
        VedtakResultatType vedtakResultatType = UtledVedtakResultatType.utled(behandlingRepository, behandling3);

        // Assert
        assertThat(vedtakResultatType).isEqualTo(VedtakResultatType.INNVILGET);
    }

    private Behandling lagRevurdering(BehandlingRepository behandlingRepository, Behandling tidligereBehandling, BehandlingResultatType behandlingResultatType) {
        BehandlingÅrsak.Builder årsakBuilder = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING)
            .medOriginalBehandling(tidligereBehandling);
        Behandling revurdering = Behandling.fraTidligereBehandling(tidligereBehandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(årsakBuilder)
            .build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().medBehandlingResultatType(behandlingResultatType).buildFor(revurdering);
        behandlingRepository.lagre(revurdering, lås);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);
        return revurdering;
    }
}
