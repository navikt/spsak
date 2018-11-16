package no.nav.foreldrepenger.behandling.statusobserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.konfig.KonfigVerdi;

@RunWith(CdiRunner.class)
public class AutomatiskFagsakAvslutningTjenesteTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final Repository repository = repoRule.getRepository();

    @Inject
    private AutomatiskFagsakAvslutningTjeneste automatiskFagsakAvslutningTjeneste;
    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    @KonfigVerdi(value = "foreldelsesfrist.foreldrenger.år")
    private int foreldelsesfristFP;

    @Test
    public void fagsak_avslutning_fødselsdato() {
        opprettBehandlingMedFødselsdato(LocalDate.now().minusYears(3));
        String avsluttedeFagsaker = automatiskFagsakAvslutningTjeneste.avsluttFagsaker();
        assertThat(avsluttedeFagsaker).isNull();

        opprettBehandlingMedFødselsdato(LocalDate.now().minusYears(4));
        avsluttedeFagsaker = automatiskFagsakAvslutningTjeneste.avsluttFagsaker();
        assertThat(avsluttedeFagsaker).isNotNull();
    }

    @Test
    public void fagsak_avslutning_omsorgsovertakelsesdato() {
        opprettBehandlingMedOvertakelsesdato(LocalDate.now().minusYears(3));
        String avsluttedeFagsaker = automatiskFagsakAvslutningTjeneste.avsluttFagsaker();
        assertThat(avsluttedeFagsaker).isNull();

        opprettBehandlingMedOvertakelsesdato(LocalDate.now().minusYears(4));
        avsluttedeFagsaker = automatiskFagsakAvslutningTjeneste.avsluttFagsaker();
        assertThat(avsluttedeFagsaker).isNotNull();
    }

    @Test
    public void fagsak_avslutning_vedtak() {
        opprettBehandlingMedVedtak(VedtakResultatType.INNVILGET);
        String avsluttedeFagsaker = automatiskFagsakAvslutningTjeneste.avsluttFagsaker();
        assertThat(avsluttedeFagsaker).isNull();

        opprettBehandlingMedVedtak(VedtakResultatType.AVSLAG);
        avsluttedeFagsaker = automatiskFagsakAvslutningTjeneste.avsluttFagsaker();
        assertThat(avsluttedeFagsaker).isNotNull();

        opprettBehandlingMedVedtak(VedtakResultatType.OPPHØR);
        avsluttedeFagsaker = automatiskFagsakAvslutningTjeneste.avsluttFagsaker();
        assertThat(avsluttedeFagsaker).isNotNull();
    }


    private void opprettBehandlingMedVedtak(VedtakResultatType vedtakResultatType) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medBehandlingsresultat(Behandlingsresultat.builderForInngangsvilkår().medBehandlingResultatType(BehandlingResultatType.INNVILGET));
        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medVedtakResultatType(vedtakResultatType)
            .medVedtaksdato(LocalDate.now())
            .medBehandlingsresultat(behandling.getBehandlingsresultat())
            .medAnsvarligSaksbehandler("Severin Saksbehandler")
            .build();
        Whitebox.setInternalState(behandling.getBehandlingsresultat(), "behandlingVedtak", vedtak);
        repository.lagre(behandling.getBehandlingsresultat());
        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);

        behandling.avsluttBehandling();
        repository.flush();
    }

    private void opprettBehandlingMedFødselsdato(LocalDate fødselsDato) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling behandling = scenario.lagre(repositoryProvider);

        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);

        behandling.avsluttBehandling();
        repository.flush();
    }

    private void opprettBehandlingMedOvertakelsesdato(LocalDate omsorgsovertakelseDato) {
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();

        Behandling behandling = scenario.lagre(repositoryProvider);

        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);
        behandling.avsluttBehandling();

        repository.flush();
    }

}
