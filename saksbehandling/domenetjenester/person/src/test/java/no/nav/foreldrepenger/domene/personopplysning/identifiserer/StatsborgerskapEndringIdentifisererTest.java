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
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class StatsborgerskapEndringIdentifisererTest {

    private AktørId AKTØRID = new AktørId("1");
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private StatsborgerskapEndringIdentifiserer statsborgerskapEndringIdentifiserer;

    @Before
    public void setup() {
        statsborgerskapEndringIdentifiserer = new StatsborgerskapEndringIdentifiserer();
        ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(AKTØRID, NavBrukerKjønn.KVINNE).medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD).lagre(repositoryProvider);
    }

    @Test
    public void testStatsborgerskapUendret() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.NOR, Region.NORDEN)));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.NOR, Region.NORDEN)));

        boolean erEndret = statsborgerskapEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at statsborgerskap er uendret").isFalse();
    }

    @Test
    public void testStatsborgerskapUendret_flere_koder() {

        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.NOR, Region.NORDEN), LandOgRegion.get(Landkoder.SWE, Region.NORDEN)));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.NOR, Region.NORDEN), LandOgRegion.get(Landkoder.SWE, Region.NORDEN)));

        boolean erEndret = statsborgerskapEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at statsborgerskap er uendret").isFalse();
    }

    @Test
    public void testStatsborgerskapUendret_men_rekkefølge_i_liste_endret() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.NOR, Region.NORDEN), LandOgRegion.get(Landkoder.SWE, Region.NORDEN)));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlagMotstattRekkefølge(personopplysningGrunnlag1.getRegisterVersjon().getStatsborgerskap());

        boolean erEndret = statsborgerskapEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i rekkefølge ikke skal detektere endring.").isFalse();
    }

    @Test
    public void testStatsborgerskapEndret() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.SWE, Region.NORDEN)));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.NOR, Region.NORDEN)));

        boolean erEndret = statsborgerskapEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i statsborgerskap blir detektert.").isTrue();
    }

    @Test
    public void testStatsborgerskapEndret_endret_type() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.SWE, Region.NORDEN), LandOgRegion.get(Landkoder.NOR, Region.NORDEN)));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.SWE, Region.NORDEN), LandOgRegion.get(Landkoder.USA, Region.UDEFINERT)));

        boolean erEndret = statsborgerskapEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i statsborgerskap blir detektert.").isTrue();
    }

    @Test
    public void testStatsborgerskapEndret_ekstra_statsborgerskap_lagt_til() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.SWE, Region.NORDEN)));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(Arrays.asList(LandOgRegion.get(Landkoder.SWE, Region.NORDEN), LandOgRegion.get(Landkoder.NOR, Region.NORDEN)));

        boolean erEndret = statsborgerskapEndringIdentifiserer.erEndret(personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i statsborgerskap blir detektert.").isTrue();
    }

    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlagMotstattRekkefølge(List<Statsborgerskap> statsborgerLand) {
        final PersonInformasjonBuilder builder1 = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        builder1.leggTil(builder1.getPersonopplysningBuilder(AKTØRID));
        //Bygg opp identiske statsborgerskap, bare legg de inn i motsatt rekkefølge.
        statsborgerLand.stream()
            .collect(Collectors.toCollection(LinkedList::new))
            .descendingIterator()
            .forEachRemaining(s -> builder1.leggTil(builder1.getStatsborgerskapBuilder(AKTØRID, s.getPeriode(), s.getStatsborgerskap(), s.getRegion()).medStatsborgerskap(s.getStatsborgerskap())));
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).medRegistrertVersjon(builder1).build();
    }


    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlag(List<LandOgRegion> statsborgerskap) {
        final PersonInformasjonBuilder builder1 = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        builder1
            .leggTil(builder1.getPersonopplysningBuilder(AKTØRID));
            IntStream.range(0, statsborgerskap.size())
                .forEach( i -> builder1.leggTil(builder1.getStatsborgerskapBuilder(AKTØRID, DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now()), statsborgerskap.get(i).land, statsborgerskap.get(i).region)));
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).medRegistrertVersjon(builder1).build();
    }

    private static class LandOgRegion {
        private Landkoder land;
        private Region region;

        private static LandOgRegion get(Landkoder land, Region region) {
            LandOgRegion landOgRegion = new LandOgRegion();
            landOgRegion.land = land;
            landOgRegion.region = region;
            return landOgRegion;
        }
    }

}
