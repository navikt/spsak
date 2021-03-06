package no.nav.foreldrepenger.domene.mottak.hendelser;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.*;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.StegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.Kompletthetskontroller;
import no.nav.foreldrepenger.domene.mottak.ytelse.YtelseForretningshendelse;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.Endringskontroller;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.util.FPDateUtil;

@RunWith(CdiRunner.class)
public class YtelseForretningshendelseHåndtererTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private YtelseForretningshendelseHåndterer håndterer;
    private Kompletthetskontroller kompletthetskontroller = mock(Kompletthetskontroller.class);

    @Inject
    private BehandlingModellRepository behandlingModellRepository;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private HistorikkRepository historikkRepository;
    @Inject
    private RevurderingTjenesteProvider revurderingTjenesteProvider;
    @Inject
    private ProsessTaskRepository prosessTaskRepository;
    @Inject
    private Endringskontroller endringskontroller;
    @Mock
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;
    @Mock
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    @Mock
    private InntektArbeidYtelseTjeneste iayTjeneste;

    private Behandling behandling;
    @Mock
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    @Before
    public void setUp() {
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = lagBehandlingskontrollTjenesteMock(repositoryProvider, behandlingModellRepository);
        Behandlingsoppretter behandlingsoppretter = new BehandlingsoppretterImpl(repositoryProvider, resultatRepositoryProvider, behandlingskontrollTjeneste, revurderingTjenesteProvider, prosessTaskRepository, mottatteDokumentTjeneste, behandlendeEnhetTjeneste, historikkinnslagTjeneste, iayTjeneste);
        håndterer = new YtelseForretningshendelseHåndterer(repositoryProvider, behandlingsoppretter, kompletthetskontroller, endringskontroller, behandlingskontrollTjeneste, historikkinnslagTjeneste);
    }

    @Test
    public void skal_oppdatere_fagsak_med_registeropplysninger_når_sak_er_under_behandling() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        Fagsak fagsak = behandling.getFagsak();
        YtelseForretningshendelse ytelseForretningshendelse = new YtelseForretningshendelse(ForretningshendelseType.YTELSE_ENDRET, fagsak.getAktørId().getId(), FPDateUtil.iDag());

        // Act
        List<Fagsak> fagsaker = håndterer.finnRelaterteFagsaker(ytelseForretningshendelse);

        // Assert
        assertThat(fagsaker.stream().map(Fagsak::getId).collect(toList()))
            .containsExactly(fagsak.getId());
    }

    @Test
    public void skal_opprette_revurdering_når_hendelse_er_endring_og_behandling_er_iverksett_vedtak() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.medBehandlingStegStart(BehandlingStegType.IVERKSETT_VEDTAK);
        scenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now())
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navesen")
            .build();
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        behandling = behandlingRepository.hentBehandling(behandling.getId());
        assertThat(behandling.erUnderIverksettelse()).isTrue();

        // Act
        håndterer.håndterAvsluttetBehandling(behandling, ForretningshendelseType.YTELSE_ENDRET);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandling.getId());
        assertThat(behandling.erUnderIverksettelse()).isTrue();

        Optional<Behandling> revurdering = behandlingRepository.hentSisteBehandlingForFagsakId(behandling.getFagsakId());
        assertThat(revurdering.get().erRevurdering()).isTrue();

        List<Historikkinnslag> historikkinnslag = historikkRepository.hentHistorikk(revurdering.get().getId());
        assertThat(historikkinnslag.isEmpty()).isFalse();
        List<HistorikkinnslagFelt> historikkinnslagFelter = historikkinnslag.get(0).getHistorikkinnslagDeler().get(0).getHistorikkinnslagFelt();
        assertThat(historikkinnslagFelter.stream().filter(historikkinnslagFelt -> HistorikkinnslagFeltType.BEGRUNNELSE.equals(historikkinnslagFelt.getFeltType())).findFirst().get().getTilVerdi()).isEqualTo("RE-ENDR-BER-GRUN");
    }

    @Test
    public void skal_opprette_revurdering_når_hendelse_er_innvilget_og_behandling_er_avsluttet() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        scenario.avsluttBehandling(repositoryProvider, behandling);

        // Act
        håndterer.håndterAvsluttetBehandling(behandling, ForretningshendelseType.YTELSE_INNVILGET);

        // Assert
        Behandling avsluttetBehandling = behandlingRepository.hentBehandling(behandling.getId());
        assertThat(avsluttetBehandling.erAvsluttet()).isTrue();

        Optional<Behandling> revurdering = behandlingRepository.hentSisteBehandlingForFagsakId(behandling.getFagsakId());
        assertThat(revurdering.get().erRevurdering()).isTrue();

        List<Historikkinnslag> historikkinnslag = historikkRepository.hentHistorikk(revurdering.get().getId());
        assertThat(historikkinnslag.isEmpty()).isFalse();
        List<HistorikkinnslagFelt> historikkinnslagFelter = historikkinnslag.get(0).getHistorikkinnslagDeler().get(0).getHistorikkinnslagFelt();
        assertThat(historikkinnslagFelter.stream().filter(historikkinnslagFelt -> HistorikkinnslagFeltType.BEGRUNNELSE.equals(historikkinnslagFelt.getFeltType())).findFirst().get().getTilVerdi()).isEqualTo("RE-TILST-YT-INNVIL");
    }
    
    private static BehandlingskontrollTjeneste lagBehandlingskontrollTjenesteMock(GrunnlagRepositoryProvider repositoryProvider, BehandlingModellRepository behandlingModellRepository) {
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, behandlingModellRepository,
            null) {
            @Override
            protected void fireEventBehandlingStatus(BehandlingskontrollKontekst kontekst, Optional<StegTilstand> forrigeTilstand,
                                                           Optional<StegTilstand> nyTilstand) {
                // NOOP
            }
            @Override
            public StegTilstand prosesserBehandling(BehandlingskontrollKontekst kontekst) {
                // NOOP
                return null;
            }
        };
        return behandlingskontrollTjeneste;
    }
}
