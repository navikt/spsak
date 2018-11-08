package no.nav.foreldrepenger.domene.personopplysning.identifiserer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class PersonAdresseEndringIdentifisererTest {

    private AktørId AKTØRID = new AktørId("1");
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private PersonAdresseEndringIdentifiserer personAdresseEndringIdentifiserer;
    private Behandling behandling;

    @Before
    public void setup() {
        personAdresseEndringIdentifiserer = new PersonAdresseEndringIdentifiserer();
        behandling = ScenarioMorSøkerForeldrepenger.forFødsel().medBruker(AKTØRID, NavBrukerKjønn.KVINNE).medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD).lagre(repositoryProvider);
    }

    @Test
    public void testPersonAdresseUendret() {
        final String postnummer = "2040";
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(postnummer));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(postnummer));

        boolean erEndret = personAdresseEndringIdentifiserer.erEndret(behandling, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at adresse er uendret").isFalse();
    }

    @Test
    public void testPersonAdresseUendret_flere_postnummer() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList("2040", "2050"));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList("2040", "2050"));

        boolean erEndret = personAdresseEndringIdentifiserer.erEndret(behandling, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at adresse er uendret").isFalse();
    }

    @Test
    public void testPersonAdresseUendret_men_rekkefølge_er_endret() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList("2050", "2040"));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlagMotstattRekkefølge(personopplysningGrunnlag1.getRegisterVersjon().getAdresser());

        boolean erEndret = personAdresseEndringIdentifiserer.erEndret(behandling, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at adresse er uendret").isFalse();
    }

    @Test
    public void testPersonAdresseEndret() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList("2040"));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList("2050"));

        boolean erEndret = personAdresseEndringIdentifiserer.erEndret(behandling, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i adresse blir detektert.").isTrue();
    }

    @Test
    public void testPersonAdresseEndretNår() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList("2040"));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList("2050"));

        boolean erEndret = personAdresseEndringIdentifiserer.erEndretFørSkjæringstidspunkt(behandling.getAktørId(),
            personopplysningGrunnlag1,
            personopplysningGrunnlag2,
            LocalDate.now().plusDays(1));
        assertThat(erEndret).as("Forventer at endring i adresse blir detektert.").isTrue();
    }

    @Test
    public void testPersonAdresseEndret_flere_postnummer() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList("2040", "2050"));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList("2040", "2060"));

        boolean erEndret = personAdresseEndringIdentifiserer.erEndret(behandling, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i adresse blir detektert.").isTrue();
    }

    @Test
    public void testPersonAdresseEndret_ekstra_postnummer_lagt_til() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList("2040", "2050"));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList("2040", "2050", "9046"));

        boolean erEndret = personAdresseEndringIdentifiserer.erEndret(behandling, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i adresse blir detektert.").isTrue();
    }

    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlagMotstattRekkefølge(List<PersonAdresse> personadresser) {
        final PersonInformasjonBuilder builder1 = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        builder1.leggTil(builder1.getPersonopplysningBuilder(AKTØRID));
        //Bygg opp identiske statsborgerskap, bare legg de inn i motsatt rekkefølge.
        new LinkedList<>(personadresser)
            .descendingIterator()
            .forEachRemaining(a -> builder1.leggTil(builder1.getAdresseBuilder(AKTØRID, a.getPeriode(), a.getAdresseType()).medPostnummer(a.getPostnummer())));
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).medRegistrertVersjon(builder1).build();
    }

    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlag(List<String> postnummer) {
        final PersonInformasjonBuilder builder1 = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        builder1
            .leggTil(builder1.getPersonopplysningBuilder(AKTØRID));
        //Opprett adresser med forskjellig fra og med dato. Går 1 mnd tilbake for hver adresse. Endrer kun postnummer i denne testen
        IntStream.range(0, postnummer.size()).forEach(i ->
            builder1.leggTil(
                builder1.getAdresseBuilder(AKTØRID, DatoIntervallEntitet.fraOgMed(LocalDate.now().minusMonths(i)), AdresseType.POSTADRESSE)
                    .medPostnummer(postnummer.get(i))
                    .medAdresseType(AdresseType.POSTADRESSE)));
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).medRegistrertVersjon(builder1).build();
    }
}
