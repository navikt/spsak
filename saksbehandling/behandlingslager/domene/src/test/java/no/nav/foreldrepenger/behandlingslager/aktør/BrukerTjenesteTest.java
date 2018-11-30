package no.nav.foreldrepenger.behandlingslager.aktør;

import no.nav.foreldrepenger.domene.typer.AktørId;
import static java.time.Month.JANUARY;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class BrukerTjenesteTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private BrukerTjeneste brukerTjeneste;

    @Before
    public void oppsett() {
        brukerTjeneste = new BrukerTjeneste(new NavBrukerRepositoryImpl(entityManager));
    }


    @Test
    public void test_opprett_ny_bruker() {
        Personinfo personinfo = new Personinfo.Builder()
            .medAktørId(new AktørId("123"))
            .medPersonIdent(new PersonIdent("12345678901"))
            .medNavn("Kari Nordmann")
            .medFødselsdato(LocalDate.of(1990, JANUARY, 1))
            .medNavBrukerKjønn(KVINNE)
            .build();

        NavBruker navBruker = brukerTjeneste.hentEllerOpprettFraAktorId(personinfo);
        assertThat(navBruker.getId()).as("Forventer at nyTerminbekreftelse bruker som ikke er lagret returneres uten id.").isNull();
    }

    @Test
    public void test_hent_bruker() {
        //Først lagre en bruker vi senere i testen skal forsøke å hente.
        Personinfo personinfo = new Personinfo.Builder()
            .medAktørId(new AktørId("123"))
            .medPersonIdent(new PersonIdent("12345678901"))
            .medNavn("Kari Nordmann")
            .medFødselsdato(LocalDate.of(1990, JANUARY, 1))
            .medNavBrukerKjønn(KVINNE)
            .build();

        NavBruker navBruker = brukerTjeneste.hentEllerOpprettFraAktorId(personinfo);
        assertThat(navBruker.getId()).as("Forventer at nyTerminbekreftelse bruker som ikke er lagret returneres uten id.").isNull();

        NavBruker navBrukerHent = brukerTjeneste.hentEllerOpprettFraAktorId(personinfo);
        assertThat(navBrukerHent.getId()).as("Forventer at vi henter opp eksisterende bruker.").isEqualTo(navBruker.getId());
    }
}
