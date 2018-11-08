package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Oppgaveinfo;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.modig.core.test.LogSniffer;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import no.nav.tjeneste.virksomhet.oppgave.v3.informasjon.oppgave.Oppgave;
import no.nav.tjeneste.virksomhet.oppgave.v3.informasjon.oppgave.Oppgavetype;
import no.nav.tjeneste.virksomhet.oppgave.v3.informasjon.oppgave.Status;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeResponse;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumer;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.PrioritetKode;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;
import no.nav.vedtak.felles.integrasjon.oppgave.FinnOppgaveListeRequestMal;
import no.nav.vedtak.felles.integrasjon.oppgave.OppgaveConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class OppgaveTjenesteImplTest {

    private static final String FNR = "000000000000";

    private static final LocalDate FØDSELSDATO = LocalDate.now().minusYears(20);

    @Rule
    public LogSniffer logSniffer = new LogSniffer();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private Repository repository = repoRule.getRepository();

    private OppgaveTjenesteImpl tjeneste;
    private BehandleoppgaveConsumer oppgavebehandlingConsumer;
    private TpsTjeneste tpsTjeneste;
    private OppgaveConsumer oppgaveConsumer;
    private ProsessTaskRepository prosessTaskRepository;

    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);

    private Behandling behandling;

    @Before
    public void oppsett() {

        oppgavebehandlingConsumer = mock(BehandleoppgaveConsumer.class);
        tpsTjeneste = mock(TpsTjeneste.class);
        oppgaveConsumer = mock(OppgaveConsumer.class);
        prosessTaskRepository = mock(ProsessTaskRepository.class);

        oppgaveBehandlingKoblingRepository = spy(new OppgaveBehandlingKoblingRepositoryImpl(entityManager));
        tjeneste = new OppgaveTjenesteImpl(repositoryProvider, oppgaveBehandlingKoblingRepository, oppgavebehandlingConsumer,
            oppgaveConsumer, prosessTaskRepository, tpsTjeneste);
        lagBehandling();

        // Sett opp default mock-oppførsel
        Personinfo personinfo = new Personinfo.Builder()
            .medAktørId(behandling.getAktørId())
            .medPersonIdent(new PersonIdent(FNR))
            .medNavn("Fornavn Etternavn")
            .medFødselsdato(FØDSELSDATO)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .build();
        when(tpsTjeneste.hentBrukerForAktør(behandling.getAktørId())).thenReturn(Optional.of(personinfo));
        logSniffer.clearLog();
    }

    private void lagBehandling() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now()).medAntallBarn(1);
        behandling = scenario.lagre(repositoryProvider);
        behandling.setBehandlendeEnhet(new OrganisasjonsEnhet("4802", null));
    }

    @Test
    public void skal_opprette_oppgave_når_det_ikke_finnes_fra_før() {
        // Arrange
        Long behandlingId = behandling.getId();
        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);
        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        // Act
        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.BEHANDLE_SAK);

        // Assert
        List<OppgaveBehandlingKobling> oppgaver = repository.hentAlle(OppgaveBehandlingKobling.class);
        assertThat(oppgaver).hasSize(1);
        Behandling behandling = oppgaver.get(0).getBehandling();
        assertThat(behandling.getId()).isEqualTo(behandlingId);
        List<OppgaveBehandlingKobling> oppgaveBehandlingKoblinger = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandlingId);
        OppgaveBehandlingKobling oppgaveBehandlingKobling = OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(OppgaveÅrsak.BEHANDLE_SAK, oppgaveBehandlingKoblinger).orElseThrow(
            () -> new IllegalStateException("Mangler AktivOppgaveMedÅrsak"));
        assertThat(oppgaveBehandlingKobling.getOppgaveÅrsak()).isEqualTo(OppgaveÅrsak.BEHANDLE_SAK);
        assertThat(oppgaveBehandlingKobling.getOppgaveId()).isEqualTo(gsakOppgaveId);

        verify(tpsTjeneste).hentBrukerForAktør(behandling.getAktørId());
    }

    @Test
    public void skal_ikke_opprette_en_ny_oppgave_av_samme_type_når_det_finnes_fra_før_og_den_ikke_er_ferdigstilt() {
        // Arrange
        Long behandlingId = behandling.getId();
        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);
        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.BEHANDLE_SAK);
        List<OppgaveBehandlingKobling> oppgaver = repository.hentAlle(OppgaveBehandlingKobling.class);
        assertThat(oppgaver).hasSize(1);
        oppgaver.get(0).setFerdigstilt(false);
        repository.lagre(oppgaver.get(0));

        // Act
        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.BEHANDLE_SAK);

        // Assert
        oppgaver = repository.hentAlle(OppgaveBehandlingKobling.class);
        assertThat(oppgaver).hasSize(1);
    }

    @Test
    public void skal_opprette_en_ny_oppgave_når_det_finnes_fra_før_og_den_er_ferdigstilt() {
        // Arrange
        Long behandlingId = behandling.getId();
        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);
        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.BEHANDLE_SAK);
        List<OppgaveBehandlingKobling> oppgaver = repository.hentAlle(OppgaveBehandlingKobling.class);
        assertThat(oppgaver).hasSize(1);
        oppgaver.get(0).setFerdigstilt(true);
        repository.lagre(oppgaver.get(0));

        // Act
        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.GODKJENNE_VEDTAK);

        // Assert
        oppgaver = repository.hentAlle(OppgaveBehandlingKobling.class);
        assertThat(oppgaver).hasSize(2);
    }

    @Test
    public void skal_kunne_opprette_en_ny_oppgave_med_en_annen_årsak_selv_om_det_finnes_en_aktiv_oppgave() throws Exception {
        // Arrange
        Long behandlingId = behandling.getId();
        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);
        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        // Act
        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.BEHANDLE_SAK);
        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.BEHANDLE_SAK);
        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.GODKJENNE_VEDTAK);
        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.GODKJENNE_VEDTAK);

        // Assert
        List<OppgaveBehandlingKobling> aktiveOppgaver = repository.hentAlle(OppgaveBehandlingKobling.class).stream()
            .filter(oppgave -> !oppgave.isFerdigstilt())
            .collect(Collectors.toList());
        assertThat(aktiveOppgaver).hasSize(2);
    }

    @Test
    public void skal_avslutte_oppgave() {
        // Arrange
        Long behandlingId = behandling.getId();
        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);
        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);
        tjeneste.opprettBasertPåBehandlingId(behandlingId, OppgaveÅrsak.BEHANDLE_SAK);

        // Act
        tjeneste.avslutt(behandlingId, OppgaveÅrsak.BEHANDLE_SAK);

        // Assert
        List<OppgaveBehandlingKobling> oppgaver = repository.hentAlle(OppgaveBehandlingKobling.class);
        OppgaveBehandlingKobling behandlingKobling = oppgaver.get(0);
        assertThat(behandlingKobling.isFerdigstilt()).isTrue();
    }

    @Test
    public void skal_logge_warning_når_oppgave_som_skal_avsluttes_ikke_finnes() {
        // Arrange
        Long behandlingId = behandling.getId();
        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);

        // Act
        tjeneste.avslutt(behandlingId, OppgaveÅrsak.BEHANDLE_SAK);

        // Assert
        logSniffer.assertHasWarnMessage(behandlingId.toString());
    }

    @Test
    public void skal_opprette_oppgave_basert_på_fagsakId() {
        // Arrange
        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);

        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        // Act
        String oppgaveId = tjeneste.opprettMedPrioritetOgBeskrivelseBasertPåFagsakId(behandling.getFagsakId(), OppgaveÅrsak.VURDER_DOKUMENT, "2010", "bla bla", false);

        // Assert
        verify(tpsTjeneste).hentBrukerForAktør(behandling.getAktørId());
        OpprettOppgaveRequest request = captor.getValue();
        assertThat(new Saksnummer(request.getSaksnummer())).isEqualTo(behandling.getFagsak().getSaksnummer());
        assertThat(request.getOppgavetypeKode()).isEqualTo(OppgaveÅrsak.VURDER_DOKUMENT.getKode());
        assertThat(oppgaveId).isEqualTo(gsakOppgaveId);
    }

    @Test
    public void skal_avslutte_oppgave_og_starte_task() {
        // Arrange
        String oppgaveId = "1";
        OppgaveBehandlingKobling kobling = new OppgaveBehandlingKobling(OppgaveÅrsak.BEHANDLE_SAK, oppgaveId, behandling.getFagsak().getSaksnummer(), behandling);
        when(oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(anyLong())).thenReturn(Collections.singletonList(kobling));

        // Act
        tjeneste.avsluttOppgaveOgStartTask(behandling, OppgaveÅrsak.BEHANDLE_SAK, OpprettOppgaveGodkjennVedtakTask.TASKTYPE);

        // Assert
        ArgumentCaptor<ProsessTaskGruppe> captor = ArgumentCaptor.forClass(ProsessTaskGruppe.class);
        verify(prosessTaskRepository).lagre(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        ProsessTaskGruppe gruppe = captor.getValue();
        List<ProsessTaskGruppe.Entry> tasks = gruppe.getTasks();
        assertThat(tasks.get(0).getTask().getTaskType()).isEqualTo(AvsluttOppgaveTaskProperties.TASKTYPE);
        assertThat(tasks.get(0).getTask().getFagsakId()).isEqualTo(behandling.getFagsakId());
        assertThat(tasks.get(0).getTask().getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(String.valueOf(tasks.get(0).getTask().getAktørId())).isEqualTo(behandling.getAktørId().getId());
        assertThat(tasks.get(1).getTask().getTaskType()).isEqualTo(OpprettOppgaveGodkjennVedtakTask.TASKTYPE);
        assertThat(tasks.get(1).getTask().getFagsakId()).isEqualTo(behandling.getFagsakId());
        assertThat(tasks.get(1).getTask().getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(String.valueOf(tasks.get(1).getTask().getAktørId())).isEqualTo(behandling.getAktørId().getId());
    }

    @Test
    public void skal_hente_oppgave_liste() {
        // Arrange
        FinnOppgaveListeResponse mockResponse = mock(FinnOppgaveListeResponse.class);
        when(mockResponse.getTotaltAntallTreff()).thenReturn(2);

        Oppgave oppgave1 = new Oppgave();
        Oppgavetype oppgavetype1 = new Oppgavetype();
        oppgavetype1.setKode("VUR_KONS_YTE_DAG");
        Status status1 = new Status();
        status1.setKode("A");
        oppgave1.setOppgavetype(oppgavetype1);
        oppgave1.setStatus(status1);
        oppgave1.setAnsvarligEnhetNavn("Ola Normann");

        Oppgave oppgave2 = new Oppgave();
        Oppgavetype oppgavetype2 = new Oppgavetype();
        oppgavetype2.setKode("VUR_VL");
        Status status2 = new Status();
        status2.setKode("A");
        oppgave2.setOppgavetype(oppgavetype2);
        oppgave2.setStatus(status2);
        oppgave2.setSaksnummer("821710131052953");

        LinkedList<Oppgave> oppgaveListe = new LinkedList<>();
        oppgaveListe.add(oppgave1);
        oppgaveListe.add(oppgave2);
        when(mockResponse.getOppgaveListe()).thenReturn(oppgaveListe);

        ArgumentCaptor<FinnOppgaveListeRequestMal> captor = ArgumentCaptor.forClass(FinnOppgaveListeRequestMal.class);
        List<String> oppgaveÅrsaker = Arrays.asList(OppgaveÅrsak.VURDER_DOKUMENT.getKode(),
            Oppgaveinfo.VURDER_KONST_YTELSE_FORELDREPENGER.getOppgaveType());
        when(oppgaveConsumer.finnOppgaveListe(captor.capture())).thenReturn(mockResponse);

        // Act
        List<Oppgaveinfo> oppgaveinfos = tjeneste.hentOppgaveListe(behandling.getAktørId(), oppgaveÅrsaker);

        // Assert
        FinnOppgaveListeRequestMal request = captor.getValue();
        assertThat(request.getSok().getBrukerId()).isEqualTo(FNR);
        assertThat(request.getFilter().getOppgavetypeKodeListe()).isEqualTo(oppgaveÅrsaker);
        assertThat(oppgaveinfos.size()).isEqualTo(2);
        assertThat(oppgaveinfos.get(0).getOppgaveType()).isEqualTo(oppgave1.getOppgavetype().getKode());
        assertThat(oppgaveinfos.get(0).getStatus()).isEqualTo(oppgave1.getStatus().getKode());
        assertThat(oppgaveinfos.get(1).getOppgaveType()).isEqualTo(oppgave2.getOppgavetype().getKode());
        assertThat(oppgaveinfos.get(1).getStatus()).isEqualTo(oppgave2.getStatus().getKode());
    }

    @Test
    public void skal_opprette_oppgave_vurder_konsekvens_basert_på_fagsakId() {
        // Arrange
        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);

        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        // Act
        String oppgaveId = tjeneste.opprettMedPrioritetOgBeskrivelseBasertPåFagsakId(behandling.getFagsakId(), OppgaveÅrsak.VURDER_KONS_FOR_YTELSE, "2010", "bla bla", false);

        // Assert
        OpprettOppgaveRequest request = captor.getValue();
        assertThat(new Saksnummer(request.getSaksnummer())).isEqualTo(behandling.getFagsak().getSaksnummer());
        assertThat(request.getOppgavetypeKode()).isEqualTo(OppgaveÅrsak.VURDER_KONS_FOR_YTELSE.getKode());
        assertThat(oppgaveId).isEqualTo(gsakOppgaveId);
    }

    @Test
    public void skal_lage_request_som_inneholder_verdier_i_forbindelse_med_manglende_regler() {
        // Arrange
        String gsakOppgaveId = "GSAK1110";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);

        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        // Act
        String oppgaveId = tjeneste.opprettOppgaveSakSkalTilInfotrygd(behandling.getId());

        // Assert
        OpprettOppgaveRequest request = captor.getValue();
        assertThat(request.getOppgavetypeKode()).isEqualTo("BEH_SAK_FOR");
        assertThat(request.getPrioritetKode()).isEqualTo(PrioritetKode.NORM_FOR);
        assertThat(oppgaveId).isEqualTo(gsakOppgaveId);
        assertThat(request.getBeskrivelse()).isEqualTo("Foreldrepengesak må flyttes til Infotrygd");
    }

    @Test
    public void skal_opprette_oppgave_med_prioritet_og_beskrivelse() {
        // Arrange
        String gsakOppgaveId = "GSAK1115";
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId(gsakOppgaveId);

        LocalDate forventetFrist = helgeJustert(LocalDate.now().plusDays(1));
        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);
        when(oppgavebehandlingConsumer.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        // Act
        String oppgaveId = tjeneste.opprettMedPrioritetOgBeskrivelseBasertPåFagsakId(behandling.getFagsakId(), OppgaveÅrsak.GODKJENNE_VEDTAK,
            "4321", "noe tekst", true);

        // Assert
        assertThat(oppgaveId).isEqualTo(gsakOppgaveId);
        OpprettOppgaveRequest request = captor.getValue();
        assertThat(new Saksnummer(request.getSaksnummer())).isEqualTo(behandling.getFagsak().getSaksnummer());
        assertThat(request.getOppgavetypeKode()).isEqualTo(OppgaveÅrsak.GODKJENNE_VEDTAK.getKode());
        assertThat(request.getBeskrivelse()).isEqualTo("noe tekst");
        assertThat(request.getPrioritetKode()).isEqualTo(PrioritetKode.HOY_FOR);
        assertThat(request.getAktivTil()).isEqualTo(Optional.of(forventetFrist));
    }

    private LocalDate helgeJustert(LocalDate dato) {
        if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            return dato.plusDays(1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue());
        }
        return dato;
    }
}
