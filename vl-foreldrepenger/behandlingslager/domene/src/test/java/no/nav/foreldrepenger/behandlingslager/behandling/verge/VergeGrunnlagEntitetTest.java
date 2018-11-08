package no.nav.foreldrepenger.behandlingslager.behandling.verge;

import no.nav.foreldrepenger.domene.typer.AktørId;
import static java.time.Month.JANUARY;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class VergeGrunnlagEntitetTest {
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    public VergeGrunnlagEntitetTest() {
    }

    @Test
    public void skal_lagre_verge_grunnlag() throws Exception {
        Behandling behandling = opprettBehandling();

        Personinfo.Builder builder = new Personinfo.Builder();
        builder.medAktørId(new AktørId("201"))
            .medPersonIdent(new PersonIdent("12345678901"))
            .medNavn("Kari Verge")
            .medFødselsdato(LocalDate.of(1990, JANUARY, 1))
            .medForetrukketSpråk(Språkkode.nb)
            .medNavBrukerKjønn(KVINNE);

        Personinfo personinfo = builder.build();
        NavBruker bruker = NavBruker.opprettNy(personinfo);

        VergeBuilder vergeBuilder = new VergeBuilder()
            .medVergeType(VergeType.BARN)
            .medBruker(bruker)
            .medMandatTekst("Mandatet")
            .medStønadMottaker(false)
            .medVedtaksdato(LocalDate.now());

        repositoryProvider.getVergeGrunnlagRepository()
            .lagreOgFlush(behandling, vergeBuilder);

        List<VergeGrunnlagEntitet> resultat = repository.hentAlle(VergeGrunnlagEntitet.class);
        assertThat(resultat).hasSize(1);
    }

    private Behandling opprettBehandling() {
        Fagsak fagsak = opprettFagsak();
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
        return behandling;
    }

    private Fagsak opprettFagsak() {
        NavBruker bruker = NavBruker.opprettNy(
            new Personinfo.Builder()
                .medAktørId(new AktørId("200"))
                .medPersonIdent(new PersonIdent("12345678901"))
                .medNavn("Kari Nordmann")
                .medFødselsdato(LocalDate.of(1990, JANUARY, 1))
                .medForetrukketSpråk(Språkkode.nb)
                .medNavBrukerKjønn(KVINNE)
                .build());

        // Opprett fagsak
        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.ENGANGSTØNAD, bruker, null, new Saksnummer("1000"));
        repository.lagre(bruker);
        repository.lagre(fagsak);
        repository.flush();
        return fagsak;
    }
}
