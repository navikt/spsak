package no.nav.foreldrepenger.behandling.statusobserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEventPubliserer;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.vedtak.felles.testutilities.Whitebox;

public class OppdaterFagsakStatusFellesTest {

    @Mock
    private FagsakStatusEventPubliserer fagsakStatusEventPubliserer;

    // SUT
    private OppdaterFagsakStatusFelles fagsakStatusFelles;



    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void utløpt_ytelsesvedtak() {
        assertThat(erVedtakUtløpt(0, 3, 3)).as("Hverken maksdato uttak eller fødsel utløpt").isFalse();
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


        fagsakStatusFelles = new OppdaterFagsakStatusFelles(repositoryProvider, fagsakStatusEventPubliserer, foreldelsesfristAntallÅr);

        return fagsakStatusFelles.ingenLøpendeYtelsesvedtak(behandling);
    }

    private boolean erVedtakUtløpt(int antallDagerEtterMaksdato, int antallÅrSidenFødsel, int foreldelsesfristAntallÅr) {
        LocalDate fødselsDato = LocalDate.now().minusYears(antallÅrSidenFødsel);
        LocalDate maksDatoUttak = LocalDate.now().minusDays(antallDagerEtterMaksdato);

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medBekreftetHendelse().medFødselsDato(fødselsDato);
        Behandling behandling = scenario.lagMocked();
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();

        fagsakStatusFelles = new OppdaterFagsakStatusFelles(repositoryProvider, fagsakStatusEventPubliserer, foreldelsesfristAntallÅr);

        return fagsakStatusFelles.ingenLøpendeYtelsesvedtak(behandling);
    }

}
