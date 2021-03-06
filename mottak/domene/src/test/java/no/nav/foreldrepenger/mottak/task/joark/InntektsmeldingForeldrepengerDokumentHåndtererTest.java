package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.task.joark.JoarkTestsupport.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.fordel.kodeverk.VariantFormat;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@RunWith(MockitoJUnitRunner.class)
public class InntektsmeldingForeldrepengerDokumentHåndtererTest {

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ProsessTaskData taskData;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;
    private JoarkDokumentHåndterer håndterer;
    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();
    private String fastsattInntektsmeldingStartdatoFristForManuellBehandling = "2019-01-01";
    private JournalMetadata<DokumentTypeId> journalMetadata;
    private AktørConsumer aktørConsumer = mock(AktørConsumer.class);

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        KodeverkRepository kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.INNTEKTSMELDING);
        ProsessTaskRepository ptr = mock(ProsessTaskRepository.class);
        håndterer = mock(JoarkDokumentHåndterer.class);
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, kodeverkRepository, håndterer, fastsattInntektsmeldingStartdatoFristForManuellBehandling, aktørConsumer));
        when(håndterer.hentGyldigAktørFraMetadata(any())).thenReturn(Optional.of(AKTØR_ID));
        when(håndterer.hentGyldigAktørFraPersonident(any())).thenReturn(Optional.of(AKTØR_ID));
        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
        dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, taskData);
        dataWrapper.setArkivId(ARKIV_ID);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        journalMetadata = joarkTestsupport.lagJournalMetadataStrukturert(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        when(aktørConsumer.hentPersonIdentForAktørId(any())).thenReturn(Optional.ofNullable(JoarkTestsupport.BRUKER_FNR));
    }


    @Test
    public void skal_håndtere_dokument_som_har_ikke_en_inntektsmelding() throws Exception {
        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        String xml = joarkTestsupport.readFile("testsoknader/foedsel-mor.xml");
        JournalDokument journalDokument = new JournalDokument(journalMetadata, xml);
        doReturn(Collections.singletonList(journalMetadata)).when(håndterer).hentJoarkDokumentMetadata(any());
        doReturn(journalDokument).when(håndterer).hentJournalDokument(any());

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(wrapper.getAktørId()).hasValue(AKTØR_ID);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }


    @Test
    public void skalHåndtereIntekksmeldingForeldrepengerManuellJournalføringDokumentHåndterer() throws Exception {
        journalMetadata = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ArkivFilType.XML)
                .medVariantFormat(VariantFormat.ORIGINAL)
                .medDokumentType(DokumentTypeId.INNTEKTSMELDING)
                .medForsendelseMottatt(LocalDate.now())
                .medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA)
                .medBrukerIdentListe(joarkTestsupport.brukerListe)
                .build();

        List<JournalMetadata> strukturertJournalMetadataSkanningMetaList = Arrays.asList(journalMetadata);
        doReturn(strukturertJournalMetadataSkanningMetaList).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);


        String xml = joarkTestsupport.readFile("testsoknader/inntektsmelding-manual-sample.xml");
        JournalDokument journalDokument = new JournalDokument(journalMetadata, xml);

        doReturn(journalDokument).when(håndterer).hentJournalDokument(Collections.singletonList(journalMetadata));
        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).hasValue(joarkTestsupport.AKTØR_ID);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skalHåndtereIntekksmeldingForeldrepengerElektronikJournalføringDokumentHåndterer() throws Exception {


        journalMetadata = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ArkivFilType.XML)
                .medVariantFormat(VariantFormat.ORIGINAL)
                .medDokumentType(DokumentTypeId.INNTEKTSMELDING)
                .medForsendelseMottatt(LocalDate.now())
                .medForsendelseMottattTidspunkt(LocalDateTime.now())
                .medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA)
                .medBrukerIdentListe(joarkTestsupport.brukerListe)
                .build();

        List<JournalMetadata> strukturertJournalMetadataSkanningMetaList = Arrays.asList(journalMetadata);

        String xml = joarkTestsupport.readFile("testsoknader/inntektsmelding-elektronisk-sample.xml");
        JournalDokument journalDokument = new JournalDokument(journalMetadata, xml);

        doReturn(journalDokument).when(håndterer).hentJournalDokument(Collections.singletonList(journalMetadata));
        doReturn(strukturertJournalMetadataSkanningMetaList).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skalHåndtereInntektsmeldingUtenStartdatoMedManuellJournalføring() throws Exception {
        journalMetadata = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ArkivFilType.XML)
                .medVariantFormat(VariantFormat.ORIGINAL)
                .medDokumentType(DokumentTypeId.INNTEKTSMELDING)
                .medForsendelseMottatt(LocalDate.now())
                .medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA)
                .medBrukerIdentListe(joarkTestsupport.brukerListe)
                .build();

        List<JournalMetadata> strukturertJournalMetadataSkanningMetaList = Arrays.asList(journalMetadata);
        doReturn(strukturertJournalMetadataSkanningMetaList).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        String xml = joarkTestsupport.readFile("testsoknader/inntektsmelding-manual-uten-startdato-foreldrepenger-periode-sample.xml");
        JournalDokument journalDokument = new JournalDokument(journalMetadata, xml);

        doReturn(journalDokument).when(håndterer).hentJournalDokument(Collections.singletonList(journalMetadata));

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).hasValue(joarkTestsupport.AKTØR_ID);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }
}
