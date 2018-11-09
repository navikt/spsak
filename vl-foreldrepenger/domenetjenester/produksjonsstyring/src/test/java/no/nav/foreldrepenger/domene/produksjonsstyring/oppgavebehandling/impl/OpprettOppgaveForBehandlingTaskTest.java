package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumer;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;
import no.nav.vedtak.felles.integrasjon.oppgave.OppgaveConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class OpprettOppgaveForBehandlingTaskTest {

    private static final String FORNAVN_ETTERNAVN = "Fornavn Etternavn";
    private static final String FNR = "000000000000";
    private static final LocalDate FØDSELSDATO = LocalDate.now().minusYears(20);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private Repository repository = repoRule.getRepository();
    private OppgaveTjenesteImpl tjeneste;

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;

    private Fagsak fagsak;

    @Mock
    private BehandleoppgaveConsumer oppgavebehandlingConsumer;
    @Mock
    private TpsTjeneste tpsTjeneste;
    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    @Mock
    private OppgaveConsumer oppgaveConsumer;

    @Before
    public void setup() {
        oppgavebehandlingConsumer = Mockito.mock(BehandleoppgaveConsumer.class);
        oppgaveConsumer = Mockito.mock(OppgaveConsumer.class);
        tpsTjeneste = Mockito.mock(TpsTjeneste.class);

        oppgaveBehandlingKoblingRepository = new OppgaveBehandlingKoblingRepositoryImpl(entityManager);
        tjeneste = new OppgaveTjenesteImpl(repositoryProvider, oppgaveBehandlingKoblingRepository, oppgavebehandlingConsumer,
            oppgaveConsumer, prosessTaskRepository, tpsTjeneste);

        // Bygg fagsak som gjenbrukes over testene
        fagsak = opprettOgLagreFagsak();

        // Sett opp default mock-oppførsel
        Personinfo personinfo = new Personinfo.Builder()
            .medAktørId(fagsak.getNavBruker().getAktørId())
            .medPersonIdent(new PersonIdent(FNR))
            .medNavn(FORNAVN_ETTERNAVN)
            .medFødselsdato(FØDSELSDATO)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .build();

        when(tpsTjeneste.hentBrukerForAktør(personinfo.getAktørId())).thenReturn(Optional.of(personinfo));
    }

    @Test
    public void skal_utføre_tasken_opprett_oppgave_for_behandling_av_førstegangsbehandling() throws Exception {
        // Arrange
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak).medBehandlendeEnhet(new OrganisasjonsEnhet("0234", null));
        Behandling behandling = behandlingBuilder.build();
        lagreBehandling(behandling);

        ProsessTaskData taskData = new ProsessTaskData(OpprettOppgaveForBehandlingTask.TASKTYPE);
        taskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        OpprettOppgaveForBehandlingTask task = new OpprettOppgaveForBehandlingTask(tjeneste, repositoryProvider);

        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);
        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        List<OppgaveBehandlingKobling> oppgaver = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
        assertThat(OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(OppgaveÅrsak.BEHANDLE_SAK, oppgaver)).isNotPresent();

        // Act
        task.doTask(taskData);
        repository.flushAndClear();

        // Assert
        behandling = behandlingRepository.hentBehandling(behandling.getId());
        oppgaver = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
        assertThat(OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(OppgaveÅrsak.BEHANDLE_SAK, oppgaver)).isPresent();
    }

    @Test
    public void oppretter_oppgave_for_behandling_av_revurdering() throws Exception {
        // Arrange
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak).medBehandlendeEnhet(new OrganisasjonsEnhet("0234", null));
        Behandling behandling = behandlingBuilder.build();
        lagreBehandling(behandling);

        Behandling revurdering = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING).build();
        lagreBehandling(revurdering);

        ProsessTaskData taskData = new ProsessTaskData(OpprettOppgaveForBehandlingTask.TASKTYPE);
        taskData.setBehandling(revurdering.getFagsakId(), revurdering.getId(), revurdering.getAktørId().getId());
        OpprettOppgaveForBehandlingTask task = new OpprettOppgaveForBehandlingTask(tjeneste, repositoryProvider);

        String gsakOppgaveId = "GSAK11101";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);
        when(oppgavebehandlingConsumer.opprettOppgave(any())).thenReturn(mockResponse);

        // Skal ikke ha en oppgave av typen revurder fra før
        List<OppgaveBehandlingKobling> oppgaver = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
        assertThat(OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(OppgaveÅrsak.REVURDER, oppgaver)).isNotPresent();

        // Act
        task.doTask(taskData);
        repository.flushAndClear();

        // Assert
        oppgaver = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(revurdering.getId());
        assertThat(OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(OppgaveÅrsak.REVURDER, oppgaver)).isPresent();
    }


    private void lagreBehandling(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
    }

    private Fagsak opprettOgLagreFagsak() {
        Fagsak fagsak = FagsakBuilder.nyEngangstønadForMor()
            .medSaksnummer(new Saksnummer("124"))
            .build();
        repositoryProvider.getFagsakRepository().opprettNy(fagsak);
        return fagsak;
    }
}
