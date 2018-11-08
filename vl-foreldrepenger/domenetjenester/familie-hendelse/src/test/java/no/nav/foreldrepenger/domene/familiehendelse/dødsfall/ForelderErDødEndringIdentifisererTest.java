package no.nav.foreldrepenger.domene.familiehendelse.dødsfall;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPartBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadAnnenPartType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class ForelderErDødEndringIdentifisererTest {
    private AktørId AKTØRID = new AktørId("1");
    private AktørId AKTØRID_ANNEN_PART = new AktørId("2");


    private ForelderErDødEndringIdentifiserer forelderErDødEndringIdentifiserer;

    @Before
    public void setup() {
        forelderErDødEndringIdentifiserer = new ForelderErDødEndringIdentifiserer();
    }

    @Test
    public void testDødsdatoUendret() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(dødsdato);
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(dødsdato);

        boolean erEndret = forelderErDødEndringIdentifiserer.erEndret(AKTØRID, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at informsjon om brukers død er uendret").isFalse();
    }

    @Test
    public void testSøkerDør() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);

        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(null);
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(dødsdato);

        boolean erEndret = forelderErDødEndringIdentifiserer.erEndret(AKTØRID, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring om brukers død blir detektert.").isTrue();
    }

    @Test
    public void testAnnenPartDør() {
        LocalDate dødsdatoSøker = null;
        LocalDate dødsdatoAnnenPart1 = null;
        LocalDate dødsdatoAnnenPart2 = LocalDate.now();
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningMedAnnenPart(dødsdatoSøker, dødsdatoAnnenPart1);
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningMedAnnenPart(dødsdatoSøker, dødsdatoAnnenPart2);

        boolean erEndret = forelderErDødEndringIdentifiserer.erEndret(AKTØRID, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring om annen parts død blir detektert.").isTrue();
    }

    @Test
    public void testDødsdatoEndret() {
        final LocalDate dødsdato = LocalDate.now().minusDays(10);

        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(dødsdato.minusDays(1));
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(dødsdato);

        boolean erEndret = forelderErDødEndringIdentifiserer.erEndret(AKTØRID, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring om brukers død blir detektert.").isTrue();
    }

    @Test
    public void skal_detektere_brukes_dødsdato_selv_om_registeropplysninger_ikke_finnes_på_originalt_grunnlag() {
        // Arrange
        final LocalDate dødsdato = LocalDate.now().minusDays(10);
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettTomtPersonopplysningGrunnlag();
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(dødsdato);

        // Act
        boolean erEndret = forelderErDødEndringIdentifiserer.erEndret(AKTØRID, personopplysningGrunnlag1, personopplysningGrunnlag2);

        // Assert
        assertThat(erEndret).as("Forventer at endring om brukers død blir detektert selv om det ikke finnes registeropplysninger på originalt grunnlag.").isTrue();
    }

    @Test
    public void skal_detektere_annen_parts_dødsdato_selv_om_registeropplysninger_ikke_finnes_på_originalt_grunnlag() {
        // Arrange
        LocalDate dødsdatoSøker = null;
        LocalDate dødsdatoAnnenPart2 = LocalDate.now();
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettTomtPersonopplysningGrunnlag();
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningMedAnnenPart(dødsdatoSøker, dødsdatoAnnenPart2);

        // Act
        boolean erEndret = forelderErDødEndringIdentifiserer.erEndret(AKTØRID, personopplysningGrunnlag1, personopplysningGrunnlag2);

        // Assert
        assertThat(erEndret).as("Forventer at endring om annen parts død blir detektert selv om det ikke finnes registeropplysninger på originalt grunnlag.").isTrue();
    }

    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlag(LocalDate dødsdato) {
        final PersonInformasjonBuilder builder1 = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        builder1.leggTil(builder1.getPersonopplysningBuilder(AKTØRID).medDødsdato(dødsdato));
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).medRegistrertVersjon(builder1).build();
    }

    private PersonopplysningGrunnlag opprettPersonopplysningMedAnnenPart(LocalDate dødsdatoSøker, LocalDate dødsdatoAnnenPart) {
        PersonInformasjonBuilder builder1 = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        builder1.leggTil(builder1.getPersonopplysningBuilder(AKTØRID).medDødsdato(dødsdatoSøker));
        builder1.leggTil(builder1.getPersonopplysningBuilder(AKTØRID_ANNEN_PART).medDødsdato(dødsdatoAnnenPart));
        OppgittAnnenPartBuilder annenPartBuilder = new OppgittAnnenPartBuilder()
            .medAktørId(AKTØRID_ANNEN_PART)
            .medType(SøknadAnnenPartType.FAR);
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty())
            .medRegistrertVersjon(builder1)
            .medOppgittAnnenPart(annenPartBuilder.build())
            .build();
    }

    private PersonopplysningGrunnlag opprettTomtPersonopplysningGrunnlag() {
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).build();
    }
}
