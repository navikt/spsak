package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
public class Forelder2ErDødEndringIdentifisererTest {
    private AktørId AKTØRID_SØKER = new AktørId("1");
    private AktørId AKTØRID_MEDMOR = new AktørId("2");
    private AktørId AKTØRID_BARN = new AktørId("3");

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private Behandling behandling;

    private Forelder2ErDødEndringIdentifiserer forelder2ErDødEndringIdentifiserer;

    private FamilieHendelseTjeneste familiehendelseTjeneste;

    @Before
    public void setup() {
        familiehendelseTjeneste = mock(FamilieHendelseTjeneste.class);
        forelder2ErDødEndringIdentifiserer = new Forelder2ErDødEndringIdentifiserer(familiehendelseTjeneste);
        behandling = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(AKTØRID_SØKER, NavBrukerKjønn.KVINNE)
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD)
            .lagre(repositoryProvider);
    }

    @Test
    public void testForelder2Lever() {
        PersonopplysningGrunnlag orginaltGrunnlag = opprettPersonopplysning(null);
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(null); //Oppdater opplysninger med dødsdato og lagre på behandlingen.

        familiehendelseTjeneste = Mockito.mock(FamilieHendelseTjeneste.class);
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        boolean erEndret = forelder2ErDødEndringIdentifiserer.erEndret(behandling, oppdatertGrunnlag, orginaltGrunnlag);
        assertThat(erEndret).as("Forventer at informsjon om forelder2 død er uendret.").isFalse();
    }

    @Test
    public void testForelder2Dør() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);

        PersonopplysningGrunnlag orginaltGrunnlag = opprettPersonopplysning(null);
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(dødsdato);//Oppdater opplysninger med dødsdato og lagre på behandlingen.
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        boolean erEndret = forelder2ErDødEndringIdentifiserer.erEndret(behandling, oppdatertGrunnlag, orginaltGrunnlag);
        assertThat(erEndret).as("Forventer at endring om forelder2 død blir detektert.").isTrue();
    }

    @Test
    public void testDødsdatoEndret() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);

        PersonopplysningGrunnlag orginaltGrunnlag = opprettPersonopplysning(dødsdato);
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(dødsdato.minusDays(1));//Oppdater dødsdato og lagre på behandlingen.
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        boolean erEndret = forelder2ErDødEndringIdentifiserer.erEndret(behandling, oppdatertGrunnlag, orginaltGrunnlag);
        assertThat(erEndret).as("Forventer at endring om forelder2 død blir detektert.").isTrue();
    }

    @Test
    public void testDødsdatoUendret() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);

        PersonopplysningGrunnlag orginaltGrunnlag = opprettPersonopplysning(dødsdato);
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(dødsdato);
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        boolean erEndret = forelder2ErDødEndringIdentifiserer.erEndret(behandling, oppdatertGrunnlag, orginaltGrunnlag);
        assertThat(erEndret).as("Forventer at informsjon om forelder2 død er uendret.").isFalse();
    }

    @Test
    public void skal_detektere_dødsdato_selv_om_registeropplysninger_ikke_finnes_på_originalt_grunnlag() {
        // Arrange
        final LocalDate dødsdato = LocalDate.now().minusDays(10);
        PersonopplysningGrunnlag orginaltGrunnlag = opprettTomtPersonopplysningGrunnlag();
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(dødsdato);
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        // Act
        boolean erEndret = forelder2ErDødEndringIdentifiserer.erEndret(behandling, oppdatertGrunnlag, orginaltGrunnlag);

        // Assert
        assertThat(erEndret).as("Forventer at endring om forelder2 død blir detektert selv om det ikke finnes registeropplysninger på originalt grunnlag.").isTrue();
    }

    private List<Personopplysning> finnBarn(PersonopplysningGrunnlag grunnlag) {
        return grunnlag.getRegisterVersjon().getPersonopplysninger().stream()
            .filter(personopplysning -> AKTØRID_BARN.equals(personopplysning.getAktørId()))
            .collect(Collectors.toList());
    }

    private PersonopplysningGrunnlag opprettPersonopplysning(LocalDate dødsdatoForelder2) {
        PersonopplysningRepository personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        final PersonInformasjonBuilder builder = personopplysningRepository.opprettBuilderForRegisterdata(behandling);
        builder.leggTil(builder.getPersonopplysningBuilder(AKTØRID_SØKER).medFødselsdato(LocalDate.now().minusYears(30)));
        builder.leggTil(builder.getPersonopplysningBuilder(AKTØRID_MEDMOR).medFødselsdato(LocalDate.now().minusYears(28)).medDødsdato(dødsdatoForelder2));
        builder.leggTil(builder.getPersonopplysningBuilder(AKTØRID_BARN).medFødselsdato(LocalDate.now().minusYears(1)));
        builder.leggTil(builder.getRelasjonBuilder(AKTØRID_SØKER, AKTØRID_BARN, RelasjonsRolleType.BARN));
        builder.leggTil(builder.getRelasjonBuilder(AKTØRID_BARN, AKTØRID_MEDMOR, RelasjonsRolleType.MEDMOR));
        builder.leggTil(builder.getRelasjonBuilder(AKTØRID_MEDMOR, AKTØRID_BARN, RelasjonsRolleType.BARN));
        personopplysningRepository.lagre(behandling, builder);
        return personopplysningRepository.hentPersonopplysninger(behandling);
    }

    private PersonopplysningGrunnlag opprettTomtPersonopplysningGrunnlag() {
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).build();
    }
}
