package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.IverksetteVedtakHistorikkTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.vedtak.KanVedtaketIverksettesTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.util.Tuple;

public class IverksetteVedtakStegImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;

    private Behandling behandling;

    @Mock
    private IverksetteVedtakHistorikkTjeneste iverksetteVedtakHistorikkTjeneste;

    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null);

    private IverksetteVedtakStegImpl iverksetteVedtakSteg;

    private BehandlingVedtak vedtak;

    @Mock
    private KanVedtaketIverksettesTjeneste kanVedtaketIverksettesTjeneste;

    @Mock
    private BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer;

    private BehandlingVedtakRepository behandlingVedtakRepository;
    private ResultatRepositoryProvider resultatRepositoryProvider;

    private void opprettSteg(ScenarioMorSøkerEngangsstønad scenario) {
        behandling = scenario.lagMocked();
        vedtak = scenario.mockBehandlingVedtak();
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenario.mockBehandlingRepositoryProvider();
        this.repositoryProvider = providerTuple.getElement1();
        this.behandlingRepository = this.repositoryProvider.getBehandlingRepository();
        this.resultatRepositoryProvider = providerTuple.getElement2();
        this.behandlingVedtakRepository = resultatRepositoryProvider.getVedtakRepository();
        iverksetteVedtakSteg = new IverksetteVedtakStegImpl(this.repositoryProvider, resultatRepositoryProvider, prosessTaskRepository,
            behandlingVedtakEventPubliserer, iverksetteVedtakHistorikkTjeneste, kanVedtaketIverksettesTjeneste);

    }

    @Test
    public void testUtførStegForIverksetteVedtakUtenResultat() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        opprettSteg(scenario);

        when(behandlingVedtakRepository.hentVedtakFor(behandlingRepository.hentResultat(behandling.getId()).getId())).thenReturn(Optional.of(vedtak));

        List<ProsessTaskData> resultat = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(resultat).isEmpty();

        // Act
        Fagsak fagsak = behandling.getFagsak();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat behStegRestulat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        // Assert
        assertThat(behStegRestulat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        verify(vedtak, never()).setIverksettingStatus(any());
    }

    @Test
    public void testUtførStegForIverksetteVedtakMedResultat() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        opprettSteg(scenario);

        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.IKKE_IVERKSATT);
        when(behandlingVedtakRepository.hentVedtakFor(behandlingRepository.hentResultat(behandling.getId()).getId())).thenReturn(Optional.of(vedtak));

        List<ProsessTaskData> resultat = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(resultat).isEmpty();
        Fagsak fagsak = behandling.getFagsak();
        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat behStegRestulat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        // Assert
        assertThat(behStegRestulat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        verify(vedtak).setIverksettingStatus(IverksettingStatus.UNDER_IVERKSETTING);
    }

    @Test
    public void testUtførStegForIverksetteVedtakNårIverksettelsePågår() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        opprettSteg(scenario);

        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.UNDER_IVERKSETTING);
        when(behandlingVedtakRepository.hentVedtakFor(behandlingRepository.hentResultat(behandling.getId()).getId())).thenReturn(Optional.of(vedtak));
        Fagsak fagsak = behandling.getFagsak();
        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat behStegRestulat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        // Assert
        assertThat(behStegRestulat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        verify(vedtak, never()).setIverksettingStatus(any());
    }

    @Test
    public void testUtførStegForIverksetteVedtakNårIverksettelseFerdig() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        opprettSteg(scenario);

        when(behandlingVedtakRepository.hentVedtakFor(behandlingRepository.hentResultat(behandling.getId()).getId())).thenReturn(Optional.of(vedtak));
        Fagsak fagsak = behandling.getFagsak();
        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat behStegRestulat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        // Assert
        assertThat(behStegRestulat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        verify(vedtak, never()).setIverksettingStatus(any());
    }

    @Test
    public void testIverksettingHindresIkkeNårDetBareErEnBehandling() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        opprettSteg(scenario);

        when(behandlingVedtakRepository.hentVedtakFor(behandlingRepository.hentResultat(behandling.getId()).getId())).thenReturn(Optional.of(vedtak));
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer()))
            .thenReturn(Collections.singletonList(behandling));
        when(vedtak.getIverksettingStatus()).thenReturn(IverksettingStatus.IKKE_IVERKSATT);

        // Act
        boolean hindres = iverksetteVedtakSteg.iverksettingHindresAvAnnenBehandling(behandling);

        // Assert
        assertThat(hindres).isEqualTo(false);
        verify(behandlingRepository).hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer());
    }

}
