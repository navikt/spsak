package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.HendelseVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class SøknadRepositoryImplTest {


    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private SøknadRepository søknadRepository;
    private FamilieHendelseRepository familieHendelseRepository;
    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private YtelsesFordelingRepository ytelseFordelingRepository;

    @Before
    public void setup () {
        søknadRepository = repositoryProvider.getSøknadRepository();
        familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        fagsakRepository = repositoryProvider.getFagsakRepository();
        ytelseFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
    }

    @Test
    public void skal_finne_endringssøknad_for_behandling() {
        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, NavBruker.opprettNy(lagPerson()));
        fagsakRepository.opprettNy(fagsak);

        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        behandlingRepository.lagre(behandling, repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));

        Behandling behandling2 = Behandling.forFørstegangssøknad(fagsak).build();
        behandlingRepository.lagre(behandling2, repositoryProvider.getBehandlingRepository().taSkriveLås(behandling2));

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(LocalDate.now().minusDays(1), LocalDate.now()).build();
        OppgittFordeling oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(oppgittPeriode), true);
        ytelseFordelingRepository.lagre(behandling, oppgittFordeling);

        FamilieHendelseBuilder fhBuilder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.SØKNAD);
        fhBuilder.medFødselsDato(LocalDate.now()).medAntallBarn(1);
        familieHendelseRepository.lagre(behandling, fhBuilder);
        familieHendelseRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, behandling2);
        FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        FamilieHendelseGrunnlag familieHendelseGrunnlag2 = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling2);

        Søknad søknad = opprettSøknad(familieHendelseGrunnlag.getGjeldendeVersjon(), oppgittFordeling, true);
        søknadRepository.lagreOgFlush(behandling, søknad);

        OppgittPeriode oppgittPeriode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(LocalDate.now().minusDays(1), LocalDate.now()).build();
        OppgittFordeling oppgittFordeling1 = new OppgittFordelingEntitet(Arrays.asList(oppgittPeriode1), true);
        ytelseFordelingRepository.lagre(behandling2, oppgittFordeling1);
        Søknad søknad2 = opprettSøknad(familieHendelseGrunnlag2.getGjeldendeVersjon(), oppgittFordeling1, true);
        søknadRepository.lagreOgFlush(behandling2, søknad2);

        // Act
        Optional<Søknad> endringssøknad = repositoryProvider.getSøknadRepository().hentSøknadHvisEksisterer(behandling);
        Optional<Søknad> endringssøknad2 = repositoryProvider.getSøknadRepository().hentSøknadHvisEksisterer(behandling2);

        //Assert
        assertThat(endringssøknad).isPresent();
        assertThat(endringssøknad2).isPresent();
        assertThat(endringssøknad.get()).isNotEqualTo(endringssøknad2.get());
    }

    @Test
    public void skal_ikke_finne_endringssøknad_for_behandling() {
        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, NavBruker.opprettNy(lagPerson()));
        fagsakRepository.opprettNy(fagsak);

        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        behandlingRepository.lagre(behandling, repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));

        OppgittPeriode oppgittPeriode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(LocalDate.now().minusDays(1), LocalDate.now()).build();
        OppgittFordeling oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(oppgittPeriode), true);
        ytelseFordelingRepository.lagre(behandling, oppgittFordeling);

        FamilieHendelseBuilder fhBuilder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.SØKNAD);
        fhBuilder.medFødselsDato(LocalDate.now()).medAntallBarn(1);
        familieHendelseRepository.lagre(behandling, fhBuilder);
        FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);

        Søknad søknad = opprettSøknad(familieHendelseGrunnlag.getGjeldendeVersjon(), oppgittFordeling, false);
        søknadRepository.lagreOgFlush(behandling, søknad);

        // Act
        Optional<Søknad> endringssøknad = repositoryProvider.getSøknadRepository().hentSøknadHvisEksisterer(behandling);

        // Assert
        assertThat(endringssøknad).isPresent();
        assertThat(endringssøknad.get().erEndringssøknad()).isFalse();
    }

    private Søknad opprettSøknad(FamilieHendelse familieHendelse, OppgittFordeling fordeling, boolean erEndringssøknad) {
        return new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now().minusDays(1))
            .medFamilieHendelse(familieHendelse)
            .medErEndringssøknad(erEndringssøknad)
            .medFordeling(fordeling)
            .build();
    }

    private Personinfo lagPerson() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId("123"))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        return personinfo;
    }
}
