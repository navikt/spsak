package no.nav.foreldrepenger.behandling.status.observer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEventPubliserer;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;

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
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagMocked();
        GrunnlagRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider().getElement1();
        ResultatRepositoryProvider resultatRepositoryProvider = scenario.mockBehandlingRepositoryProvider().getElement2();
        BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        BehandlingVedtak build = BehandlingVedtak.builder().medVedtaksdato(LocalDate.now().minusDays(1))
            .medAnsvarligSaksbehandler("Nav Navesen").medVedtakResultatType(vedtakResultatType).medBehandlingsresultat(behandlingsresultat).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        resultatRepositoryProvider.getVedtakRepository().lagre(build, lås);
        behandlingRepository.lagre(behandlingsresultat, lås);
        int foreldelsesfristAntallÅr = 100; // Kun for teset

        fagsakStatusFelles = new OppdaterFagsakStatusFelles(repositoryProvider, fagsakStatusEventPubliserer, foreldelsesfristAntallÅr);

        return fagsakStatusFelles.ingenLøpendeYtelsesvedtak(behandling);
    }

    private boolean erVedtakUtløpt(int antallDagerEtterMaksdato, int antallÅrSidenFødsel, int foreldelsesfristAntallÅr) {
        LocalDate.now().minusYears(antallÅrSidenFødsel);
        LocalDate.now().minusDays(antallDagerEtterMaksdato);

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagMocked();
        GrunnlagRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider().getElement1();

        fagsakStatusFelles = new OppdaterFagsakStatusFelles(repositoryProvider, fagsakStatusEventPubliserer, foreldelsesfristAntallÅr);

        return fagsakStatusFelles.ingenLøpendeYtelsesvedtak(behandling);
    }

}
