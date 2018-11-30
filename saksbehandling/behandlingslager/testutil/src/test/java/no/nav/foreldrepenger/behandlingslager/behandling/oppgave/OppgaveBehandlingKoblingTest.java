package no.nav.foreldrepenger.behandlingslager.behandling.oppgave;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class OppgaveBehandlingKoblingTest {
    private static final Saksnummer SAKSNUMMER = new Saksnummer("123");
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository = new OppgaveBehandlingKoblingRepositoryImpl(repoRule.getEntityManager());

    private Fagsak fagsak = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("909"))).build();

    @Before
    public void setup() {
        repository.lagre(fagsak.getNavBruker());
        repository.lagre(fagsak);
        repository.flush();
    }

    @Test
    public void skal_lagre_ned_en_oppgave() throws Exception {
        // Arrange
        String oppgaveIdFraGSAK = "IDFRAGSAK";
        OppgaveÅrsak behandleSøknad = OppgaveÅrsak.BEHANDLE_SAK;

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        OppgaveBehandlingKobling oppgave = new OppgaveBehandlingKobling(behandleSøknad, oppgaveIdFraGSAK, SAKSNUMMER, behandling);
        long id = lagreOppgave(oppgave);

        // Assert
        OppgaveBehandlingKobling oppgaveFraBase = repository.hent(OppgaveBehandlingKobling.class, id);
        assertThat(oppgaveFraBase.getOppgaveId()).isEqualTo(oppgaveIdFraGSAK);
    }

    private long lagreOppgave(OppgaveBehandlingKobling oppgave) {
        return oppgaveBehandlingKoblingRepository.lagre(oppgave);
    }

    @Test
    public void skal_knytte_en_oppgave_til_en_behandling() throws Exception {
        // Arrange
        String oppgaveIdFraGSAK = "IDFRAGSAK";
        OppgaveÅrsak behandleSøknad = OppgaveÅrsak.BEHANDLE_SAK;

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        OppgaveBehandlingKobling oppgave = new OppgaveBehandlingKobling(behandleSøknad, oppgaveIdFraGSAK, SAKSNUMMER, behandling);
        lagreOppgave(oppgave);

        // Assert
        List<Behandling> behandlinger = repository.hentAlle(Behandling.class);
        assertThat(behandlinger).hasSize(1);
        List<OppgaveBehandlingKobling> oppgaveBehandlingKoblinger = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandlinger.get(0).getId());
        assertThat(OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(OppgaveÅrsak.BEHANDLE_SAK, oppgaveBehandlingKoblinger)).isNotNull();
    }

    @Test
    public void skal_kunne_ferdigstille_en_eksisterende_oppgave() throws Exception {
        // Arrange
        String oppgaveIdFraGSAK = "IDFRAGSAK";
        OppgaveÅrsak behandleSøknad = OppgaveÅrsak.BEHANDLE_SAK;
        String saksbehandler = "R160223";

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);

        OppgaveBehandlingKobling oppgave = new OppgaveBehandlingKobling(behandleSøknad, oppgaveIdFraGSAK, SAKSNUMMER, behandling);
        Long id = lagreOppgave(oppgave);

        // Act
        OppgaveBehandlingKobling oppgaveFraBase = repository.hent(OppgaveBehandlingKobling.class, id);
        oppgaveFraBase.ferdigstillOppgave(saksbehandler);
        lagreOppgave(oppgaveFraBase);

        OppgaveBehandlingKobling oppgaveHentetFraBasen = repository.hent(OppgaveBehandlingKobling.class, oppgaveFraBase.getId());
        assertThat(oppgaveHentetFraBasen.isFerdigstilt()).isTrue();
    }

}
