package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask.TASKNAME;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.TEMA_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.codahale.metrics.MetricRegistry;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumer;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.OppgaveKodeType;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class OpprettGSakOppgaveTjenesteTaskTest {

    private static final String SAKSNUMMER = "9876543";

    private BehandleoppgaveConsumer mockService;
    private MetricRegistry metricRegistry;
    private DokumentRepository dokumentRepository;
    private KodeverkRepository kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
    private AktørConsumerMedCache aktørConsumer;

    private String fordelingsOppgaveEnhetsId = "4825";

    private OpprettGSakOppgaveTask task;
    private EnhetsTjeneste enhetsidTjeneste;
    private JournalTjeneste journalTjeneste;
    private FagsakRestKlient fagsakRestKlient;
    private TilJournalføringTjeneste tilJournalføringTjeneste;

    @Before
    public void setup() {
        mockService = Mockito.mock(BehandleoppgaveConsumer.class);
        aktørConsumer = mock(AktørConsumerMedCache.class);
        enhetsidTjeneste = mock(EnhetsTjeneste.class);
        dokumentRepository = mock(DokumentRepository.class);
        journalTjeneste = mock(JournalTjeneste.class);
        fagsakRestKlient = mock(FagsakRestKlient.class);
        tilJournalføringTjeneste = new TilJournalføringTjeneste(journalTjeneste, fagsakRestKlient, dokumentRepository);
        metricRegistry = new MetricRegistry();
        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), any(), any())).thenReturn(fordelingsOppgaveEnhetsId);
        task = new OpprettGSakOppgaveTask(mockService, enhetsidTjeneste, kodeverkRepository, dokumentRepository, tilJournalføringTjeneste, aktørConsumer);
    }

    @Test
    public void testServiceTask_journalforingsoppgave() {

        final String fodselsnummer = "19069004957";
        final String aktørId = "1234";
        final BehandlingTema behandlingTema = BehandlingTema.ENGANGSSTØNAD_FØDSEL;

        ProsessTaskData taskData = new ProsessTaskData(TASKNAME);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, behandlingTema.getKode());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getKode());
        taskData.setAktørId(aktørId);
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId("MOCK");

        String enhetId = "9999";
        String beskrivelse = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).getNavn();

        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);

        when(mockService.opprettOppgave(captor.capture())).thenReturn(mockResponse);
        when(aktørConsumer.hentPersonIdentForAktørId(aktørId)).thenReturn(Optional.of(fodselsnummer));

        task.doTask(taskData);

        OpprettOppgaveRequest serviceRequest = captor.getValue();

        assertEquals(serviceRequest.getBeskrivelse(), beskrivelse);
        assertThat(serviceRequest.getOppgavetypeKode()).as("Forventer at oppgavekode er journalføring foreldrepenger").isEqualTo(OppgaveKodeType.JFR_FOR.toString());
    }

    @Test
    public void testServiceTask_uten_aktørId_fordelingsoppgave() {
        String enhet = "4205";
        ProsessTaskData taskData = new ProsessTaskData(TASKNAME);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.ENGANGSSTØNAD_FØDSEL.getKode());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.UDEFINERT.getKode());
        taskData.setProperty(JOURNAL_ENHET, enhet);
        String beskrivelse = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD_FØDSEL).getNavn();

        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId("MOCK");

        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);

        when(mockService.opprettOppgave(captor.capture())).thenReturn(mockResponse);
        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), eq(Optional.of(enhet)), any())).thenReturn(enhet);

        task.doTask(taskData);

        OpprettOppgaveRequest serviceRequest = captor.getValue();
        assertThat(serviceRequest.getBeskrivelse()).as("Forventer at beskrivelse er fordelingsoppgave når vi ikke har aktørId.").isEqualTo(beskrivelse);
        assertThat(serviceRequest.getOppgavetypeKode()).as("Forventer at oppgavekode er fordeling foreldrepenger").isEqualTo(OppgaveKodeType.JFR_FOR.toString());
        assertThat(serviceRequest.getAnsvarligEnhetId()).as("Forventer journalførende enhet").isEqualTo(enhet);
    }

    @Test
    public void testSkalJournalføreDokumentForsendelse() {
        UUID forsendelseId = UUID.randomUUID();
        ProsessTaskData taskData = new ProsessTaskData(TASKNAME);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.FORELDREPENGER.getKode());
        taskData.setProperty(MottakMeldingDataWrapper.FORSENDELSE_ID_KEY, forsendelseId.toString());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getKode());
        taskData.setProperty(RETRY_KEY, "J");

        List<Dokument> dokumenter = new ArrayList<>();
        dokumenter.addAll(DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL));

        when(journalTjeneste.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class))).thenReturn(DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(JournalTilstand.MIDLERTIDIG_JOURNALFØRT, 3));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAKSNUMMER));
        when(dokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);

        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId("MOCK");
        String beskrivelse = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL).getNavn();

        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);

        when(mockService.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        ArgumentCaptor<DokumentforsendelseRequest> dokCapture = ArgumentCaptor.forClass(DokumentforsendelseRequest.class);

        task.doTask(taskData);

        verify(journalTjeneste).journalførDokumentforsendelse(dokCapture.capture());
        DokumentforsendelseRequest request = dokCapture.getValue();
        OpprettOppgaveRequest serviceRequest = captor.getValue();
        assertThat(request.isRetrying()).isTrue();
        assertThat(request.getForsendelseId()).isEqualTo(forsendelseId.toString());
        assertThat(serviceRequest.getBeskrivelse()).as("Forventer at beskrivelse er fordelingsoppgave når vi ikke har aktørId.").isEqualTo(beskrivelse);
        assertThat(request.getForsøkEndeligJF()).isFalse();
    }
}
