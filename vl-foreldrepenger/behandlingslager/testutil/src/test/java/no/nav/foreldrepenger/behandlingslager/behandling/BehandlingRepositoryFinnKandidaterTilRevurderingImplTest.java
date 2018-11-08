package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.etterkontroll.BehandlingEtterkontrollRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.etterkontroll.BehandlingEtterkontrollRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.etterkontroll.EtterkontrollLogg;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class BehandlingRepositoryFinnKandidaterTilRevurderingImplTest {

    private final static int revurderingDagerTilbake = 60;
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final BehandlingVedtakRepository behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
    private final BehandlingEtterkontrollRepository behandlingEtterkontrollRepository = new BehandlingEtterkontrollRepositoryImpl(
            repoRule.getEntityManager());

    private Behandling behandling;

    @Test
    public void skal_finne_kandidat_til_revurdering_og_logge_til_etterkontollog() {
        Behandling behandling = opprettRevurderingsKandidat(revurderingDagerTilbake + 2);

        final List<Behandling> behandlings = behandlingEtterkontrollRepository
                .finnKandidaterForAutomatiskEtterkontroll(Period.parse("P" + revurderingDagerTilbake + "D"));

        assertThat(behandlings.size()).isEqualTo(1);
        assertThat(behandlings.get(0).getId()).isEqualTo(behandling.getId());
    }

    @Test
    public void behandling_som_har_vært_etterkontrollert_skal_ikke_være_kandidat_til_revurdering() {
        Behandling behandling = opprettRevurderingsKandidat(revurderingDagerTilbake + 2);
        EtterkontrollLogg etterkontrollLogg = new EtterkontrollLogg.Builder(behandling).build();

        behandlingEtterkontrollRepository.lagre(etterkontrollLogg, behandlingRepository.taSkriveLås(behandling));

        List<Behandling> fagsakList = behandlingEtterkontrollRepository
                .finnKandidaterForAutomatiskEtterkontroll(Period.parse("P" + revurderingDagerTilbake + "D"));

        assertThat(fagsakList).isEmpty();
    }

    @Test
    public void fagsak_som_har_eksisterende_revurderingsbehandling_skal_ikke_være_kandidat_til_revurdering() {
        Behandling behandling = opprettRevurderingsKandidat(revurderingDagerTilbake + 2);

        Behandling revurderingsBehandling = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING)
                .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN)).build();

        behandlingRepository.lagre(revurderingsBehandling, behandlingRepository.taSkriveLås(revurderingsBehandling));

        List<Behandling> fagsakList = behandlingEtterkontrollRepository
                .finnKandidaterForAutomatiskEtterkontroll(Period.parse("P" + revurderingDagerTilbake + "D"));

        assertThat(fagsakList).isEmpty();
    }

    @Test
    public void skal_hente_ut_siste_vedtak_til_revurdering() {
        final FamilieHendelseRepository grunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        Behandling behandling = opprettRevurderingsKandidat(revurderingDagerTilbake + 2);
        LocalDate terminDato = LocalDate.now().minusDays(revurderingDagerTilbake + 2);

        Behandling.Builder revurderingBuilder = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING)
                .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ANNET));
        Behandling revurderingsBehandling = revurderingBuilder.build();

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET)
                .buildFor(revurderingsBehandling);
        final BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder().medVedtaksdato(LocalDate.now())
                .medBehandlingsresultat(behandlingsresultat).medVedtakResultatType(VedtakResultatType.INNVILGET)
                .medAnsvarligSaksbehandler("asdf").build();
        revurderingsBehandling.avsluttBehandling();
        behandlingRepository.lagre(revurderingsBehandling, behandlingRepository.taSkriveLås(revurderingsBehandling));
        grunnlagRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, revurderingsBehandling);
        final FamilieHendelseBuilder oppdatere = grunnlagRepository.opprettBuilderFor(revurderingsBehandling);
        oppdatere.medTerminbekreftelse(oppdatere.getTerminbekreftelseBuilder()
                .medTermindato(terminDato)
                .medNavnPå("Lege Legsen")
                .medUtstedtDato(terminDato.minusDays(40)))
                .medAntallBarn(1);
        grunnlagRepository.lagre(revurderingsBehandling, oppdatere);
        behandlingVedtakRepository.lagre(behandlingVedtak, behandlingRepository.taSkriveLås(revurderingsBehandling));

        List<Behandling> fagsakList = behandlingEtterkontrollRepository
                .finnKandidaterForAutomatiskEtterkontroll(Period.parse("P" + revurderingDagerTilbake + "D"));

        assertThat(fagsakList).containsOnly(revurderingsBehandling);
    }

    @Test
    public void behandling_med_nyere_termindato_skal_ikke_være_kandidat_til_revurdering() {
        opprettRevurderingsKandidat(revurderingDagerTilbake - 2);

        List<Behandling> fagsakList = behandlingEtterkontrollRepository
                .finnKandidaterForAutomatiskEtterkontroll(Period.parse("P" + revurderingDagerTilbake + "D"));

        assertThat(fagsakList).isEmpty();
    }

    @Test
    public void behandling_med_registrert_fødsel_skal_ikke_være_kandidat_til_revurdering() {
        LocalDate terminDato = LocalDate.now().minusDays(revurderingDagerTilbake + 2);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
                .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                        .medTermindato(terminDato)
                        .medUtstedtDato(LocalDate.now())
                        .medNavnPå("Lege Legesen"))
                .medAntallBarn(1);
        scenario.medBekreftetHendelse()
                .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                        .medTermindato(terminDato)
                        .medUtstedtDato(terminDato.minusDays(40))
                        .medNavnPå("LEGEN MIN"))
                .medFødselsDato(LocalDate.now().minusDays(revurderingDagerTilbake + 2))
                .medAntallBarn(1);

        behandling = scenario.lagre(repositoryProvider);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
                .medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling);
        final BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder().medVedtaksdato(LocalDate.now())
                .medBehandlingsresultat(behandlingsresultat).medVedtakResultatType(VedtakResultatType.INNVILGET)
                .medAnsvarligSaksbehandler("asdf").build();

        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        behandlingVedtakRepository.lagre(behandlingVedtak, behandlingRepository.taSkriveLås(behandling));

        List<Behandling> fagsakList = behandlingEtterkontrollRepository
                .finnKandidaterForAutomatiskEtterkontroll(Period.parse("P" + revurderingDagerTilbake + "D"));

        assertThat(fagsakList).isEmpty();
    }

    @Test
    public void skal_hente_liste_over_revurderingsaarsaker() {
        Repository repository = repoRule.getRepository();
        List<VurderÅrsak> aarsaksListe = repository.hentAlle(VurderÅrsak.class);
        assertThat(aarsaksListe.size()).isEqualTo(5);
        assertThat(aarsaksListe.get(0).getKode()).isEqualTo("FEIL_FAKTA");
    }

    private Behandling opprettRevurderingsKandidat(int dagerTilbake) {
        LocalDate terminDato = LocalDate.now().minusDays(dagerTilbake);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
                .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                        .medTermindato(terminDato)
                        .medUtstedtDato(LocalDate.now())
                        .medNavnPå("Lege Legesen"))
                .medAntallBarn(1);
        scenario.medBekreftetHendelse()
                .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                        .medTermindato(terminDato)
                        .medNavnPå("LEGEN MIN")
                        .medUtstedtDato(terminDato.minusDays(40)))
                .medAntallBarn(1);

        behandling = scenario.lagre(repositoryProvider);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
                .medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling);
        final BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder().medVedtaksdato(LocalDate.now().minusDays(1))
                .medBehandlingsresultat(behandlingsresultat).medVedtakResultatType(VedtakResultatType.INNVILGET)
                .medAnsvarligSaksbehandler("asdf").build();

        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        behandlingVedtakRepository.lagre(behandlingVedtak, behandlingRepository.taSkriveLås(behandling));

        return behandling;
    }

    @Test
    public void skal_finne_nyeste_innvilgete_avsluttede_behandling_som_ikke_er_henlagt() {
        Behandling behandling = opprettRevurderingsKandidat(revurderingDagerTilbake + 2);

        Behandlingsresultat innvilget = new Behandlingsresultat.Builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET)
                .buildFor(behandling);
        behandling.setBehandlingresultat(innvilget);
        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        Behandling henlagtBehandling = Behandling.fraTidligereBehandling(behandling, BehandlingType.FØRSTEGANGSSØKNAD).build();
        Behandlingsresultat henlagt = new Behandlingsresultat.Builder()
                .medBehandlingResultatType(BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET).buildFor(henlagtBehandling);
        henlagtBehandling.setBehandlingresultat(henlagt);
        henlagtBehandling.avsluttBehandling();
        behandlingRepository.lagre(henlagtBehandling, behandlingRepository.taSkriveLås(henlagtBehandling));

        Optional<Behandling> resultatOpt = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(behandling.getFagsak().getId());
        assertThat(resultatOpt).hasValueSatisfying(resultat ->
            assertThat(resultat.getId()).isEqualTo(behandling.getId())
        );
    }

}
