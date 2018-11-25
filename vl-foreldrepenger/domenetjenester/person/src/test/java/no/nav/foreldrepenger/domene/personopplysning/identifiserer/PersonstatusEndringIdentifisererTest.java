package no.nav.foreldrepenger.domene.personopplysning.identifiserer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class PersonstatusEndringIdentifisererTest {

    private AktørId AKTØRID = new AktørId("1");
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private PersonstatusEndringIdentifiserer personstatusEndringIdentifiserer;
//    private Behandling behandling;

    @Before
    public void setup() {
        personstatusEndringIdentifiserer = new PersonstatusEndringIdentifiserer();
        ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(AKTØRID, NavBrukerKjønn.KVINNE).medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD).lagre(repositoryProvider);
    }

    @Test
    public void testPersonstatusUendret() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(PersonstatusType.ABNR));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(PersonstatusType.ABNR));

        boolean erEndret = personstatusEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at personstatus er uendret").isFalse();
    }

    @Test
    public void testPersonstatusUendret_flere_statuser() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(PersonstatusType.ABNR, PersonstatusType.BOSA));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(PersonstatusType.ABNR, PersonstatusType.BOSA));

        boolean erEndret = personstatusEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at personstatus er uendret").isFalse();
    }

    @Test
    public void testPersonstatusEndret_ekstra_status_lagt_til() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(PersonstatusType.ABNR, PersonstatusType.BOSA));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(PersonstatusType.ABNR, PersonstatusType.BOSA, PersonstatusType.FOSV));

        boolean erEndret = personstatusEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i personstatus blir detektert.").isTrue();
    }
    @Test
    public void testPersonstatusEndret_status_endret_type() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(PersonstatusType.ABNR, PersonstatusType.BOSA));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(PersonstatusType.ABNR, PersonstatusType.FOSV));

        boolean erEndret = personstatusEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i personstatus blir detektert.").isTrue();
    }

    @Test
    public void testPersonstatusUendret_men_rekkefølge_i_liste_endret() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(PersonstatusType.ABNR, PersonstatusType.BOSA));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlagMotstattRekkefølge(personopplysningGrunnlag1.getRegisterVersjon().getPersonstatus());

        boolean erEndret = personstatusEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i rekkefølge ikke skal detektere endring.").isFalse();
    }

    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlagMotstattRekkefølge(List<Personstatus> personstatuser) {
        final PersonInformasjonBuilder builder1 = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        builder1.leggTil(builder1.getPersonopplysningBuilder(AKTØRID));
        personstatuser.stream()
            .collect(Collectors.toCollection(LinkedList::new))
            .descendingIterator()
            .forEachRemaining(ps -> builder1.leggTil(builder1.getPersonstatusBuilder(AKTØRID, ps.getPeriode()).medPersonstatus(ps.getPersonstatus())));
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).medRegistrertVersjon(builder1).build();
    }

    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlag(List<PersonstatusType> personstatuser) {
        final PersonInformasjonBuilder builder1 = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        builder1.leggTil(builder1.getPersonopplysningBuilder(AKTØRID));
        //Opprett personstatuser med forskjellig fra og med dato. Går 1 mnd tilbake for hver status.
        IntStream.range(0, personstatuser.size()).forEach(i -> builder1.leggTil(builder1.getPersonstatusBuilder(AKTØRID, DatoIntervallEntitet.fraOgMed(LocalDate.now().minusMonths(i))).medPersonstatus(personstatuser.get(i))));
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).medRegistrertVersjon(builder1).build();
    }
}
