package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;


import static no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand.FASTSATT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class ForeslåVedtakRevurderingStegForeldrepengerImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    @Mock
    private ForeslåVedtakTjeneste foreslåVedtakTjeneste;
    @Mock
    private BehandleStegResultat behandleStegResultat;

    private final EntityManager entityManager = repoRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    private ForeslåVedtakRevurderingStegForeldrepengerImpl foreslåVedtakRevurderingStegForeldrepenger;

    private Behandling orginalBehandling;
    private Behandling revurdering;
    private BehandlingskontrollKontekst kontekstRevurdering;

    @Before
    public void before() {
        orginalBehandling = ScenarioMorSøkerEngangsstønad.forFødsel().lagre(repositoryProvider);
        orginalBehandling.avsluttBehandling();
        revurdering = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.REVURDERING)
            .medOriginalBehandling(orginalBehandling, BehandlingÅrsakType.UDEFINERT)
            .lagre(repositoryProvider);

        BehandlingLås låsRevurdering = behandlingRepository.taSkriveLås(revurdering);
        Fagsak fagsak = revurdering.getFagsak();
        kontekstRevurdering = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), låsRevurdering);
        foreslåVedtakRevurderingStegForeldrepenger =
            new ForeslåVedtakRevurderingStegForeldrepengerImpl(beregningsgrunnlagRepository, behandlingRepository,foreslåVedtakTjeneste);
        when(foreslåVedtakTjeneste.foreslåVedtak(revurdering)).thenReturn(behandleStegResultat);
        when(behandleStegResultat.getAksjonspunktResultater()).thenReturn(Collections.emptyList());



    }
    @Test
    public void skal_ikke_opprette_aksjonspunkt_når_samme_beregningsgrunnlag(){
        beregningsgrunnlagRepository.
            lagre(orginalBehandling,buildBeregningsgrunnlag(1000L), FASTSATT);
        beregningsgrunnlagRepository.
            lagre(revurdering,buildBeregningsgrunnlag(1000L), FASTSATT);
        BehandleStegResultat behandleStegResultat = foreslåVedtakRevurderingStegForeldrepenger.utførSteg(kontekstRevurdering);
        assertThat(behandleStegResultat.getAksjonspunktListe()).isEmpty();
    }

    @Test
    public void skal_opprette_aksjonspunkt_når_revurdering_har_mindre_beregningsgrunnlag(){
        beregningsgrunnlagRepository.
            lagre(orginalBehandling,buildBeregningsgrunnlag(1000L), FASTSATT);
        beregningsgrunnlagRepository.
            lagre(revurdering,buildBeregningsgrunnlag(900L), FASTSATT);
        BehandleStegResultat behandleStegResultat = foreslåVedtakRevurderingStegForeldrepenger.utførSteg(kontekstRevurdering);
        assertThat(behandleStegResultat.getAksjonspunktListe().get(0)).isEqualTo(AksjonspunktDefinisjon.KONTROLLER_REVURDERINGSBEHANDLING);
    }

    @Test
    public void testTilbakehopp() {
        // Arrange
        BehandlingLås behandlingLås = repositoryProvider.getBehandlingLåsRepository().taLås(revurdering.getId());
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(revurdering.getFagsakId(), revurdering.getAktørId(), behandlingLås);

        // Act
        foreslåVedtakRevurderingStegForeldrepenger.vedHoppOverBakover(kontekst, revurdering, null, null, null);
        entityManager.flush();
        entityManager.clear();

        // Assert
        revurdering = behandlingRepository.hentBehandling(revurdering.getId());
        assertThat(revurdering.getBehandlingsresultat().getKonsekvenserForYtelsen()).isEmpty();
    }

    private Beregningsgrunnlag buildBeregningsgrunnlag(Long bruttoPerÅr){
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(LocalDate.now())
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(91425))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(91425))
            .build();
        BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now().minusDays(1),LocalDate.now().plusDays(1))
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoPerÅr))
            .build(beregningsgrunnlag);
        return beregningsgrunnlag;
    }



}
