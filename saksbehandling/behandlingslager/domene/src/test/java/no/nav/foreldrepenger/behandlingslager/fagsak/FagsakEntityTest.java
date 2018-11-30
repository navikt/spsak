package no.nav.foreldrepenger.behandlingslager.fagsak;

import static java.time.Month.JANUARY;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class FagsakEntityTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private <T> T hentFørste(Class<T> c) {
        Collection<T> result = repository.hentAlle(c);
        Assertions.assertThat(result).hasSize(1);
        return result.iterator().next();
    }

    @Test
    public void skal_lagre_og_hente_fagsak() {
        // Opprett søker objekt.
        NavBruker navBruker = NavBruker.opprettNy(new Personinfo.Builder()
            .medAktørId(new AktørId("123"))
            .medPersonIdent(new PersonIdent("12345678901"))
            .medNavn("Kari Nordmann")
            .medFødselsdato(LocalDate.of(1990, JANUARY, 1))
            .medNavBrukerKjønn(KVINNE)
            .medForetrukketSpråk(Språkkode.nb)
            .build());

        repository.lagre(navBruker);

        // Opprett fagsak
        Fagsak fagsak = Fagsak.opprettNy(navBruker);

        // Sjekk at det ikke er saker i basen fra før
        Collection<Fagsak> fagsaker = repository.hentAlle(Fagsak.class);
        Assertions.assertThat(fagsaker).hasSize(0);

        // Lagre fagsak
        repository.lagre(fagsak);

        repository.flush();

        // Sjekk at saken ser riktig ut etterpå
        Fagsak sammeFagsak = hentFørste(Fagsak.class);

        assertThat(sammeFagsak).isNotNull();
        assertThat(sammeFagsak.getYtelseType()).isNotNull();
        assertThat(sammeFagsak.getYtelseType().getNavn()).isEqualTo(FagsakYtelseType.UDEFINERT.getNavn());
        assertThat(sammeFagsak.getNavBruker()).isNotNull();
        assertThat(sammeFagsak.getNavBruker().getAktørId()).isEqualTo(new AktørId("123"));
        assertThat(sammeFagsak.getSkalTilInfotrygd()).isEqualTo(false);
    }
}
