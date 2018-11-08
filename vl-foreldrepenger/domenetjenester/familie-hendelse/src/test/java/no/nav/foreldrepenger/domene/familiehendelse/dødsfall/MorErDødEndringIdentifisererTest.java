package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class MorErDødEndringIdentifisererTest {
    private AktørId AKTØRID_SØKER = new AktørId("1");
    private AktørId AKTØRID_MOR = new AktørId("2");
    private AktørId AKTØRID_BARN = new AktørId("3");

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private Behandling behandling;

    private MorErDødEndringIdentifiserer morErDødEndringIdentifiserer;

    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    private FamilieHendelseTjeneste familiehendelseTjeneste;

    @Before
    public void setup() {
        personopplysningTjeneste = mock(BasisPersonopplysningTjeneste.class);
        familiehendelseTjeneste = mock(FamilieHendelseTjeneste.class);
        morErDødEndringIdentifiserer = new MorErDødEndringIdentifiserer(personopplysningTjeneste, familiehendelseTjeneste);
        behandling = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBruker(AKTØRID_SØKER, NavBrukerKjønn.KVINNE)
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD)
            .lagre(repositoryProvider);
    }

    @Test
    public void testMorLever() {
        PersonopplysningGrunnlag orginaltGrunnlag = opprettPersonopplysning(null);
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(null); //Oppdater opplysninger med dødsdato og lagre på behandlingen.
        when(personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling)).thenReturn(Optional.of(tilAggregat(behandling, oppdatertGrunnlag)));
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        boolean erEndret = morErDødEndringIdentifiserer.erEndret(behandling, orginaltGrunnlag);
        assertThat(erEndret).as("Forventer at informsjon om mors død er uendret.").isFalse();
    }

    @Test
    public void testMorDør() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);

        PersonopplysningGrunnlag orginaltGrunnlag = opprettPersonopplysning(null);
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(dødsdato);//Oppdater opplysninger med dødsdato og lagre på behandlingen.
        when(personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling)).thenReturn(Optional.of(tilAggregat(behandling, oppdatertGrunnlag)));
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        boolean erEndret = morErDødEndringIdentifiserer.erEndret(behandling, orginaltGrunnlag);
        assertThat(erEndret).as("Forventer at endring om mors død blir detektert.").isTrue();
    }

    @Test
    public void testDødsdatoEndret() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);

        PersonopplysningGrunnlag orginaltGrunnlag = opprettPersonopplysning(dødsdato);
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(dødsdato.minusDays(1));//Oppdater dødsdato og lagre på behandlingen.
        when(personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling)).thenReturn(Optional.of(tilAggregat(behandling, oppdatertGrunnlag)));
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        boolean erEndret = morErDødEndringIdentifiserer.erEndret(behandling, orginaltGrunnlag);
        assertThat(erEndret).as("Forventer at endring om mors død blir detektert.").isTrue();
    }

    @Test
    public void testDødsdatoUendret() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);

        PersonopplysningGrunnlag orginaltGrunnlag = opprettPersonopplysning(dødsdato);
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(dødsdato);//Oppdater dødsdato og lagre på behandlingen.
        when(personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling)).thenReturn(Optional.of(tilAggregat(behandling, oppdatertGrunnlag)));
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        boolean erEndret = morErDødEndringIdentifiserer.erEndret(behandling, orginaltGrunnlag);
        assertThat(erEndret).as("Forventer at informsjon om mors død er uendret.").isFalse();
    }

    @Test
    public void skal_detektere_dødsdato_selv_om_registeropplysninger_ikke_finnes_på_originalt_grunnlag() {
        // Arrange
        final LocalDate dødsdato = LocalDate.now().minusDays(10);
        PersonopplysningGrunnlag orginaltGrunnlag = opprettTomtPersonopplysningGrunnlag();
        PersonopplysningGrunnlag oppdatertGrunnlag = opprettPersonopplysning(dødsdato);//Oppdater opplysninger med dødsdato og lagre på behandlingen.
        when(personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling)).thenReturn(Optional.of(tilAggregat(behandling, oppdatertGrunnlag)));
        when(familiehendelseTjeneste.finnBarnSøktStønadFor(behandling)).thenReturn(finnBarn(oppdatertGrunnlag));

        // Act
        boolean erEndret = morErDødEndringIdentifiserer.erEndret(behandling, orginaltGrunnlag);

        // Assert
        assertThat(erEndret).as("Forventer at endring om mors død blir detektert selv om det ikke finnes registeropplysninger på originalt grunnlag.").isTrue();
    }

    private PersonopplysningerAggregat tilAggregat(Behandling behandling, PersonopplysningGrunnlag grunnlag) {
        return new PersonopplysningerAggregat(grunnlag, behandling.getAktørId(), DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now()), new HashMap<>());
    }

    private List<Personopplysning> finnBarn(PersonopplysningGrunnlag grunnlag) {
        return grunnlag.getRegisterVersjon().getPersonopplysninger().stream()
            .filter(personopplysning -> AKTØRID_BARN.equals(personopplysning.getAktørId()))
            .collect(Collectors.toList());
    }

    private PersonopplysningGrunnlag opprettPersonopplysning(LocalDate dødsdatoMor) {
        PersonopplysningRepository personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        final PersonInformasjonBuilder builder = personopplysningRepository.opprettBuilderForRegisterdata(behandling);
        builder.leggTil(builder.getPersonopplysningBuilder(AKTØRID_SØKER).medFødselsdato(LocalDate.now().minusYears(30)));
        builder.leggTil(builder.getPersonopplysningBuilder(AKTØRID_MOR).medFødselsdato(LocalDate.now().minusYears(28)).medDødsdato(dødsdatoMor));
        builder.leggTil(builder.getPersonopplysningBuilder(AKTØRID_BARN).medFødselsdato(LocalDate.now().minusYears(1)));
        builder.leggTil(builder.getRelasjonBuilder(AKTØRID_SØKER, AKTØRID_BARN, RelasjonsRolleType.BARN));
        builder.leggTil(builder.getRelasjonBuilder(AKTØRID_BARN, AKTØRID_MOR, RelasjonsRolleType.MORA));
        builder.leggTil(builder.getRelasjonBuilder(AKTØRID_MOR, AKTØRID_BARN, RelasjonsRolleType.BARN));
        personopplysningRepository.lagre(behandling, builder);
        return personopplysningRepository.hentPersonopplysninger(behandling);
    }

    private PersonopplysningGrunnlag opprettTomtPersonopplysningGrunnlag() {
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).build();
    }
}
