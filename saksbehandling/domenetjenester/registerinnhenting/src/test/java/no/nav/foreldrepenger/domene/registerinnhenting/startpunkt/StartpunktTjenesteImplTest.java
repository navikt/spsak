package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import static no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType.OPPTJENING;
import static no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType.UDEFINERT;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.kontrollerfakta.StartpunktTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class StartpunktTjenesteImplTest {

    private StartpunktTjeneste tjeneste;

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private Behandling behandling;

    private Instance<StartpunktUtleder> utledere;

    private EndringsresultatSjekker endringsresultatSjekker;

    @SuppressWarnings("unchecked")
    @Before
    public void before() {

        behandling = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD).lagre(repositoryProvider, resultatRepositoryProvider);

        endringsresultatSjekker = mock(EndringsresultatSjekker.class);

        // Mock startpunktutlederprovider
        utledere = mock(Instance.class);
        Instance<StartpunktUtleder> utleder = mock(Instance.class);
        StartpunktUtleder utlederMock = (behandling, grunnlagId1, grunnlagId2) -> StartpunktType.OPPTJENING;
        when(utledere.select(any())).thenReturn(utleder);
        when(utleder.get()).thenReturn(utlederMock);

        tjeneste = new StartpunktTjenesteImpl(utledere, repositoryProvider, endringsresultatSjekker);
    }

    @Test
    public void skal_returnere_startpunkt_for_endret_aggregat() {
        // Arrange
        // To forskjellige id-er indikerer endring på grunnlag
        long grunnlagId1 = 1L, grunnlagId2 = 2L;
        EndringsresultatDiff endringsresultat = opprettEndringsresultat(grunnlagId1, grunnlagId2);

        when(endringsresultatSjekker.finnSporedeEndringerPåBehandlingsgrunnlag(any(Behandling.class), any(EndringsresultatSnapshot.class))).thenReturn(endringsresultat);

        // Act/Assert
        assertThat(tjeneste.utledStartpunktForDiffBehandlingsgrunnlag(behandling, endringsresultat)).isEqualTo(OPPTJENING);
    }

    @Test
    public void skal_gi_startpunkt_udefinert_dersom_ingen_endringer_på_aggregater() {
        // Arrange
        // To lik id-er indikerer ingen endring på grunnlag
        long grunnlagId1= 1L, grunnlagId2 = grunnlagId1;
        EndringsresultatDiff endringsresultat = opprettEndringsresultat(grunnlagId1, grunnlagId2);
        when(endringsresultatSjekker.finnSporedeEndringerPåBehandlingsgrunnlag(any(Behandling.class), any(EndringsresultatSnapshot.class))).thenReturn(endringsresultat);

        // Act/Assert
        assertThat(tjeneste.utledStartpunktForDiffBehandlingsgrunnlag(behandling, endringsresultat)).isEqualTo(UDEFINERT);
    }

    private EndringsresultatDiff opprettEndringsresultat(Long grunnlagId1, Long grunnlagId2) {

        EndringsresultatDiff endringsresultat = EndringsresultatDiff.opprett();
        DiffResult diffResult = mock(DiffResult.class);
        when(diffResult.isEmpty()).thenReturn(false); // Indikerer at det finnes diff
        endringsresultat.leggTilSporetEndring(EndringsresultatDiff.medDiff(Object.class, grunnlagId1, grunnlagId2), () -> diffResult);

        return endringsresultat;
    }
}
