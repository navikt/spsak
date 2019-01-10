package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType.IVERKSETT_VEDTAK;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class AvsluttBehandlingImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Inject
    private AvsluttBehandling avsluttBehandling;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private GrunnlagRepositoryProvider repositoryProvider;
    @Inject
    private ResultatRepositoryProvider resultatRepositoryProvider;

    private Behandling behandling;
    private Fagsak fagsak;

    @Before
    public void setUp() {
        behandling = lagBehandling(LocalDate.now());
        fagsak = behandling.getFagsak();
    }

    @Test
    public void testAvsluttBehandlingUtenAndreBehandlingerISaken() {
        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());
        // Assert
        verifiserIverksatt(behandling);
    }

    private void verifiserIverksatt(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = repositoryProvider.getBehandlingRepository().hentResultat(behandling.getId());
        Optional<BehandlingVedtak> vedtakOptional = resultatRepositoryProvider.getVedtakRepository().hentVedtakFor(behandlingsresultat.getId());
        assertThat(vedtakOptional).isPresent();
        BehandlingVedtak vedtak = vedtakOptional.get();
        assertThat(vedtak.getIverksettingStatus()).isEqualByComparingTo(IverksettingStatus.IVERKSATT);
    }

    @Test
    public void testAvsluttBehandlingMedAnnenBehandlingSomIkkeVenter() {
        // Arrange
        ScenarioMorSøkerEngangsstønad.forDefaultAktør().lagre(repositoryProvider, resultatRepositoryProvider);

        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());

        verifiserIverksatt(behandling);
    }

    @Test
    public void testAvsluttBehandlingMedAnnenBehandlingSomVenter() {
        // Arrange
        Behandling annenBehandling = lagBehandling(LocalDate.now());

        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());

        verifiserIverksatt(behandling);
        verifiserKallTilFortsettBehandling(annenBehandling);
    }

    @Test
    public void testAvsluttBehandlingMedToAndreBehandlingerSomVenterEldsteFørst() {
        // Arrange
        LocalDate now = LocalDate.now();
        Behandling annenBehandling = lagBehandling(now);
        Behandling tredjeBehandling = lagBehandling(now);

        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());

        verifiserIverksatt(behandling);
        verifiserKallTilFortsettBehandling(annenBehandling);
        verifiserIkkeKallTilFortsettBehandling(tredjeBehandling);
    }

    private void verifiserKallTilFortsettBehandling(Behandling behandling) {
        List<ProsessTaskData> arguments = prosessTaskRepository.finnIkkeStartet();

        assertThat(inneholderFortsettBehandlingTaskForBehandling(arguments, behandling)).isTrue();
    }

    private void verifiserIkkeKallTilFortsettBehandling(Behandling behandling) {
        List<ProsessTaskData> arguments = prosessTaskRepository.finnIkkeStartet();
        assertThat(inneholderFortsettBehandlingTaskForBehandling(arguments, behandling)).isFalse();
    }

    private boolean inneholderFortsettBehandlingTaskForBehandling(List<ProsessTaskData> arguments, Behandling behandling) {
        return arguments.stream()
            .anyMatch(argument -> argument.getTaskType().equals(FortsettBehandlingTaskProperties.TASKTYPE)
                && argument.getBehandlingId().equals(behandling.getId()));
    }

    @Test
    public void testAvsluttBehandlingMedToAndreBehandlingerSomVenterEldsteSist() {
        // Arrange
        LocalDate now = LocalDate.now();
        Behandling annenBehandling = lagBehandling(now);
        Behandling tredjeBehandling = lagBehandling(now);

        // Act
        avsluttBehandling.avsluttBehandling(behandling.getId());

        verifiserIverksatt(behandling);
        verifiserKallTilFortsettBehandling(annenBehandling);
        verifiserIkkeKallTilFortsettBehandling(tredjeBehandling);
    }

    private Behandling lagBehandling(LocalDate vedtaksdato) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        if (fagsak != null) {
            scenario.medFagsakId(fagsak.getId());
            scenario.medSaksnummer(fagsak.getSaksnummer());
        }
        scenario.medBehandlingsresultat(Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET));

        if (vedtaksdato != null) {
            setVedtakPåScenario(scenario.medBehandlingVedtak(), vedtaksdato);
        }
        scenario.medBehandlingStegStart(IVERKSETT_VEDTAK);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        // Sett for skjæringstidspunkt
        SykefraværRepository sfRepository = repositoryProvider.getSykefraværRepository();
        SykefraværBuilder builder = sfRepository.oppretBuilderForSykefravær(behandling.getId());
        builder.leggTil(builder.periodeBuilder().medArbeidsgiver(Arbeidsgiver.person(new AktørId("1234")))
            .medPeriode(vedtaksdato.minusDays(28), vedtaksdato)
            .medType(SykefraværPeriodeType.SYKEMELDT));

        sfRepository.lagre(behandling, builder);

        return behandling;
    }

    private BehandlingVedtak setVedtakPåScenario(BehandlingVedtak.Builder builder, LocalDate vedtaksdato) {
        return builder.medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Severin Saksbehandler")
            .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
            .medVedtaksdato(vedtaksdato).build();
    }

}
