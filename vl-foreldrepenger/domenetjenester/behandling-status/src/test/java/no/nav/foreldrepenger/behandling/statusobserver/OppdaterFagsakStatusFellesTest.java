package no.nav.foreldrepenger.behandling.statusobserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEventPubliserer;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.uttak.saldo.Saldoer;
import no.nav.foreldrepenger.domene.uttak.saldo.StønadskontoSaldoTjeneste;
import no.nav.vedtak.felles.testutilities.Whitebox;

public class OppdaterFagsakStatusFellesTest {

    @Mock
    private FagsakStatusEventPubliserer fagsakStatusEventPubliserer;
    @Mock
    private FagsakRelasjonRepository fagsakRelasjonRepository;
    @Mock
    private StønadskontoSaldoTjeneste stønadskontoSaldoTjeneste;
    @Mock
    private Saldoer saldoer;
    // SUT
    private OppdaterFagsakStatusFelles fagsakStatusFelles;



    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void utløpt_ytelsesvedtak() {
        assertThat(erVedtakUtløpt(0, 3, 3)).as("Hverken maksdato uttak eller fødsel utløpt").isFalse();
        assertThat(erVedtakUtløpt(1, 3, 3)).as("Maksdato utløpt").isTrue();
        assertThat(erVedtakUtløpt(1, 3, 3)).as("Fødsel foreldelsesfrist utløpt").isTrue();
    }

    @Test
    public void avslått_ytelsesvedtak() {
        assertThat(erVedtakDirekteAvsluttbart(VedtakResultatType.AVSLAG)).as("Vedtak AVSLAG avsluttes direkte").isTrue();
        assertThat(erVedtakDirekteAvsluttbart(VedtakResultatType.OPPHØR)).as("Vedtak OPPHØR avsluttes direkte").isTrue();
        assertThat(erVedtakDirekteAvsluttbart(VedtakResultatType.INNVILGET)).as("Vedtak INNVILGET avsluttes direkte").isFalse();
    }

    private boolean erVedtakDirekteAvsluttbart(VedtakResultatType vedtakResultatType) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        BehandlingVedtak behandlingVedtak = scenario.medBehandlingVedtak().medVedtakResultatType(vedtakResultatType).build();
        Behandling behandling = scenario.lagMocked();
        Whitebox.setInternalState(behandling.getBehandlingsresultat(), "behandlingVedtak", behandlingVedtak);

        int foreldelsesfristAntallÅr = 100; // Kun for teset
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();


        Mockito.when(saldoer.getMaksDatoUttak()).thenReturn(Optional.empty());
        Mockito.when(fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak()))
            .thenReturn(Optional.empty()); // Brukes kun som guardbetingelse før StønadskontoSaldoTjeneste

        fagsakStatusFelles = new OppdaterFagsakStatusFelles(repositoryProvider, fagsakStatusEventPubliserer, stønadskontoSaldoTjeneste, fagsakRelasjonRepository, foreldelsesfristAntallÅr);

        return fagsakStatusFelles.ingenLøpendeYtelsesvedtak(behandling);
    }

    private boolean erVedtakUtløpt(int antallDagerEtterMaksdato, int antallÅrSidenFødsel, int foreldelsesfristAntallÅr) {
        LocalDate fødselsDato = LocalDate.now().minusYears(antallÅrSidenFødsel);
        LocalDate maksDatoUttak = LocalDate.now().minusDays(antallDagerEtterMaksdato);

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medBekreftetHendelse().medFødselsDato(fødselsDato);
        Behandling behandling = scenario.lagMocked();
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();

        Mockito.when(saldoer.getMaksDatoUttak()).thenReturn(Optional.of(maksDatoUttak));
        Mockito.when(stønadskontoSaldoTjeneste.finnSaldoer(behandling)).thenReturn(saldoer);
        Mockito.when(fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak()))
            .thenReturn(Optional.of(Mockito.mock(FagsakRelasjon.class))); // Brukes kun som guardbetingelse før StønadskontoSaldoTjeneste

        fagsakStatusFelles = new OppdaterFagsakStatusFelles(repositoryProvider, fagsakStatusEventPubliserer, stønadskontoSaldoTjeneste, fagsakRelasjonRepository, foreldelsesfristAntallÅr);

        return fagsakStatusFelles.ingenLøpendeYtelsesvedtak(behandling);
    }

}
