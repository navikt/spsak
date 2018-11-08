package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BarnetsDødsdatoEndringIdentifisererTest {
    private AktørId AKTØRID_SØKER = new AktørId("1");
    private AktørId AKTØRID_BARN = new AktørId("2");

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private PersonopplysningRepository personopplysningRepository;

    private Behandling behandlingNy;

    private BarnetsDødsdatoEndringIdentifiserer barnetsDødsdatoEndringIdentifiserer;
    private FamilieHendelseTjeneste familiehendelseTjeneste;

    @Before
    public void setup() {
        personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        familiehendelseTjeneste = Mockito.mock(FamilieHendelseTjeneste.class);
        barnetsDødsdatoEndringIdentifiserer = new BarnetsDødsdatoEndringIdentifiserer(familiehendelseTjeneste);
        behandlingNy = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(AKTØRID_SØKER, NavBrukerKjønn.KVINNE)
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD)
            .lagre(repositoryProvider);
    }

    @Test
    public void testBarnLever() {
        final LocalDate dødsdato = null;
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(dødsdato, true);
        PersonopplysningGrunnlag personopplysningGrunnlagOrginal = opprettPersonopplysningGrunnlag(dødsdato, true);
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandlingNy)).thenReturn(finnBarn(personopplysningGrunnlag1));

        boolean erEndret = barnetsDødsdatoEndringIdentifiserer.erEndret(behandlingNy, personopplysningGrunnlagOrginal);
        assertThat(erEndret).as("Forventer at informsjon om barnets død er uendret").isFalse();
    }

    @Test
    public void test_nytt_barn_i_tps_som_ikke_var_registrert_i_TPS_orginalt() {
        final LocalDate dødsdato = null;
        PersonopplysningGrunnlag personopplysningGrunnlagOrginal = opprettPersonopplysningGrunnlag(dødsdato, false);
        PersonopplysningGrunnlag personopplysningGrunnlagNy = opprettPersonopplysningGrunnlag(dødsdato, true);
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandlingNy)).thenReturn(finnBarn(personopplysningGrunnlagNy));

        boolean erEndret = barnetsDødsdatoEndringIdentifiserer.erEndret(behandlingNy, personopplysningGrunnlagOrginal);
        assertThat(erEndret).as("Forventer at informsjon om barnets død er uendret").isFalse();
    }

    @Test
    public void testDødsdatoUendret() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(dødsdato, true);
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(dødsdato, true);
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandlingNy)).thenReturn(finnBarn(personopplysningGrunnlag1));

        boolean erEndret = barnetsDødsdatoEndringIdentifiserer.erEndret(behandlingNy, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at informsjon om barnets død er uendret").isFalse();
    }

    @Test
    public void testBarnDør() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(null, true);
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(dødsdato, true);
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandlingNy)).thenReturn(finnBarn(personopplysningGrunnlag1));

        boolean erEndret = barnetsDødsdatoEndringIdentifiserer.erEndret(behandlingNy, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring om barnets død blir detektert.").isTrue();
    }

    @Test
    public void testDødsdatoEndret() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(dødsdato.minusDays(1), true);
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(dødsdato, true);
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandlingNy)).thenReturn(finnBarn(personopplysningGrunnlag1));

        boolean erEndret = barnetsDødsdatoEndringIdentifiserer.erEndret(behandlingNy, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring om barnets død blir detektert.").isTrue();
    }

    @Test
    public void skal_detektere_dødsdato_selv_om_registeropplysninger_ikke_finnes_på_originalt_grunnlag() {
        // Arrange
        final LocalDate dødsdato = LocalDate.now().minusDays(10);
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettTomtPersonopplysningGrunnlag();
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(dødsdato, true);
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandlingNy)).thenReturn(finnBarn(personopplysningGrunnlag2));

        // Act
        boolean erEndret = barnetsDødsdatoEndringIdentifiserer.erEndret(behandlingNy, personopplysningGrunnlag1);

        // Assert
        assertThat(erEndret).as("Forventer at barnets død blir detektert selv om det ikke finnes registeropplysninger på originalt grunnlag.").isTrue();
    }

    private List<Personopplysning> finnBarn(PersonopplysningGrunnlag grunnlag) {
        return grunnlag.getRegisterVersjon().getPersonopplysninger().stream()
            .filter(personopplysning -> AKTØRID_BARN.equals(personopplysning.getAktørId()))
            .collect(Collectors.toList());
    }

    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlag(LocalDate dødsdatoBarn, boolean registrerMedBarn) {
        final PersonInformasjonBuilder builder = personopplysningRepository.opprettBuilderForRegisterdata(behandlingNy);
        builder.leggTil(builder.getPersonopplysningBuilder(AKTØRID_SØKER).medFødselsdato(LocalDate.now().minusYears(30)));
        if (registrerMedBarn) {
            builder.leggTil(builder.getPersonopplysningBuilder(AKTØRID_BARN).medFødselsdato(LocalDate.now().minusMonths(1)).medDødsdato(dødsdatoBarn));
            builder.leggTil(builder.getRelasjonBuilder(AKTØRID_SØKER, AKTØRID_BARN, RelasjonsRolleType.BARN));
        }
        personopplysningRepository.lagre(behandlingNy, builder);
        return personopplysningRepository.hentPersonopplysninger(behandlingNy);
    }

    private PersonopplysningGrunnlag opprettTomtPersonopplysningGrunnlag() {
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).build();
    }
}
