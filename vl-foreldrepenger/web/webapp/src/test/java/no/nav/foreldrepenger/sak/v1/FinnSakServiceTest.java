package no.nav.foreldrepenger.sak.v1;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.meldinger.FinnSakListeResponse;

public class FinnSakServiceTest {

    private FinnSakService finnSakService; // objektet vi tester

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private BehandlingRepositoryProviderImpl repositoryProvider;

    @Before
    public void setup() {
        repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
        finnSakService = new FinnSakService(repositoryProvider);
    }

    @Test
    public void skal_konvertere_fagsak_for_engangsstønad_ved_fødsel_til_ekstern_representasjon() throws Exception {
        final ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSaksnummer(new Saksnummer("1337"));
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now());
        final Behandling behandling = scenario.lagre(repositoryProvider);

        FinnSakListeResponse respons = finnSakService.lagResponse(Collections.singletonList(behandling.getFagsak()));

        assertThat(respons.getSakListe()).hasSize(1);
        Sak sak = respons.getSakListe().get(0);
        assertThat(sak.getBehandlingstema().getValue()).isEqualTo("ab0050"); //betyr engangsstønad ved fødsel
        assertThat(sak.getBehandlingstema().getTermnavn()).isEqualTo("Engangsstønad ved fødsel");
        assertThat(sak.getSakId()).isEqualTo("1337");
    }

    @Test
    public void skal_konvertere_fagsak_for_engangsstønad_ved_adopsjon_til_ekstern_representasjon() throws Exception {
        final ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSaksnummer(new Saksnummer("1337"));
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));
        final Behandling behandling = scenario.lagre(repositoryProvider);

        FinnSakListeResponse respons = finnSakService.lagResponse(Collections.singletonList(behandling.getFagsak()));

        assertThat(respons.getSakListe()).hasSize(1);
        Sak sak = respons.getSakListe().get(0);
        assertThat(sak.getBehandlingstema().getValue()).isEqualTo("ab0027"); //betyr engangsstønad ved adopsjon
        assertThat(sak.getBehandlingstema().getTermnavn()).isEqualTo("Engangsstønad ved adopsjon");
        assertThat(sak.getSakId()).isEqualTo("1337");
    }

    @Test
    public void skal_konvertere_fagsak_uten_behandlinger_til_ekstern_representasjon() {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(new AktørId("42"))
            .medKjønn(NavBrukerKjønn.KVINNE)
            .build();

        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, navBruker, null, new Saksnummer("1338"));
        fagsak.setId(1L);
        FinnSakListeResponse respons = finnSakService.lagResponse(Collections.singletonList(fagsak));

        assertThat(respons.getSakListe()).hasSize(1);
        Sak sak = respons.getSakListe().get(0);
        assertThat(sak.getBehandlingstema().getValue()).isEqualTo("ab0326"); //betyr foreldrepenger
        assertThat(sak.getBehandlingstema().getTermnavn()).isEqualTo("Foreldrepenger");
        assertThat(sak.getSakId()).isEqualTo("1338");
    }

}
