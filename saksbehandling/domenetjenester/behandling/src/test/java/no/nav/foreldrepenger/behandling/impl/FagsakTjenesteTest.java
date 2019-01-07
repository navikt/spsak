package no.nav.foreldrepenger.behandling.impl;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.BrukerTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class FagsakTjenesteTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private FagsakTjenesteImpl tjeneste;
    private BrukerTjeneste brukerTjeneste;

    @Mock
    private SøknadRepository søknadRepository;

    private Fagsak fagsak;
    private Personinfo personinfo;

    private final AktørId forelderAktørId = new AktørId("154523");
    private LocalDate forelderFødselsdato = LocalDate.of(1990, JANUARY, 1);

    @Before
    public void oppsett() {
        tjeneste = new FagsakTjenesteImpl(new GrunnlagRepositoryProviderImpl(entityManager), null);

        brukerTjeneste = new BrukerTjeneste(new NavBrukerRepositoryImpl(entityManager));

        personinfo = new Personinfo.Builder()
            .medAktørId(forelderAktørId)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medNavn("Kari Nordmann")
            .medFødselsdato(forelderFødselsdato)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medForetrukketSpråk(Språkkode.nb)
            .build();

        Fagsak fagsak = lagNyFagsak(personinfo);
        
        this.fagsak  = fagsak;
    }

    private Fagsak lagNyFagsak(Personinfo personinfo) {
        NavBruker søker = NavBruker.opprettNy(personinfo);
        Fagsak fagsak = Fagsak.opprettNy(søker);
        tjeneste.opprettFagsak(fagsak, personinfo);
        return fagsak;
    }

    @Test
    public void opprettFlereFagsakerSammeBruker() throws Exception {
        // Opprett en fagsak i systemet
        Whitebox.setInternalState(fagsak, "fagsakStatus", FagsakStatus.LØPENDE); // dirty, men eksponerer ikke status nå

        // Ifølgeregler i mottak skal vi opprette en nyTerminbekreftelse sak hvis vi ikke har sak nyere enn 10 mnd:
        NavBruker søker = brukerTjeneste.hentEllerOpprettFraAktorId(personinfo);
        Fagsak fagsakNy = Fagsak.opprettNy(søker);
        tjeneste.opprettFagsak(fagsakNy, personinfo);
        assertThat(fagsak.getNavBruker().getId()).as("Forventer at fagsakene peker til samme bruker")
            .isEqualTo(fagsakNy.getNavBruker().getId());
    }

}
