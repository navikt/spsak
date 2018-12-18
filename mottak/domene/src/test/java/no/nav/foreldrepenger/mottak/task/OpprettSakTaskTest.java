package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import no.nav.foreldrepenger.fordel.kodeverk.*;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.klient.VurderFagsystemResultat;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class OpprettSakTaskTest {

    public static final String FNR = "07078515478";
    public static final String AKTØR_ID = "456";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;

    @Mock
    private FagsakRestKlient fagsakRestKlient;

    @Mock
    private AktørConsumer aktørConsumer;

    private KodeverkRepository kodeverkRepository;

    private OpprettSakTask task;

    @Before
    public void setUp() throws Exception {
        prosessTaskRepositoryMock = mock(ProsessTaskRepository.class);
        aktørConsumer = mock(AktørConsumer.class);
        when(aktørConsumer.hentAktørIdForPersonIdent(FNR)).thenReturn(Optional.of(AKTØR_ID));
        fagsakRestKlient = mock(FagsakRestKlient.class);
        VurderFagsystemResultat vurderFagsystemRespons = new VurderFagsystemResultat();
        vurderFagsystemRespons.setBehandlesIVedtaksløsningen(true);
        when(fagsakRestKlient.vurderFagsystem(any())).thenReturn(vurderFagsystemRespons);
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        task = new OpprettSakTask(prosessTaskRepositoryMock, fagsakRestKlient, kodeverkRepository);
    }

    @Test
    public void test_doTask_fødsel_strukturert() throws Exception {

        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        String filename = "testsoknader/foedsel-mor.xml";
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        String xml = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);
        ptData.setArkivId("123");
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);

        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        final MottattStrukturertDokument<?> soeknadDTO = MeldingXmlParser.unmarshallXml(xml);
        soeknadDTO.kopierTilMottakWrapper(ptData, aktørConsumer::hentAktørIdForPersonIdent);

        String saksnummer = "789";
        SaksnummerDto saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        MottakMeldingDataWrapper result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

    @Test
    public void test_doTask_fødsel_ustrukturert() throws Exception {
        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);
        ptData.setArkivId("123");
        ptData.setAktørId(AKTØR_ID);

        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);
        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        String saksnummer = "789";
        SaksnummerDto saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        MottakMeldingDataWrapper result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper ptData) {
        task.precondition(ptData);
        return task.doTask(ptData);
    }

    @Test
    public void test_doTask_anke_klage() throws Exception {
        ProsessTaskData innData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        innData.setSekvens("1");

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(kodeverkRepository, innData);

        ptData.setArkivId("123");
        ptData.setAktørId(AKTØR_ID);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentTypeId(DokumentTypeId.KLAGE_DOKUMENT);
        ptData.setDokumentKategori(DokumentKategori.KLAGE_ELLER_ANKE);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(ptData);
        assertThat(OpprettGSakOppgaveTask.TASKNAME).isEqualTo(wrapper.getProsessTaskData().getTaskType());
    }

    @Test
    public void test_doTask_uten_dokumentkategori() throws Exception {
        ProsessTaskData innData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        innData.setSekvens("1");

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(kodeverkRepository, innData);

        ptData.setAktørId(AKTØR_ID);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentTypeId(DokumentTypeId.KLAGE_DOKUMENT);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-941984");

        doTaskWithPrecondition(ptData);
    }

    @Test
    public void test_validerDatagrunnlag_skal_feile_ved_manglende_personId() throws Exception {
        MottakMeldingDataWrapper meldingDataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, new ProsessTaskData(OpprettSakTask.TASKNAME));

        expectedException.expect(TekniskException.class);

        task.precondition(meldingDataWrapper);
    }

    @Test
    public void test_validerDatagrunnlag_skal_feile_ved_manglende_behandlingstema() throws Exception {
        MottakMeldingDataWrapper meldingDataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, new ProsessTaskData(OpprettSakTask.TASKNAME));
        meldingDataWrapper.setAktørId("123");

        expectedException.expect(TekniskException.class);

        task.precondition(meldingDataWrapper);
    }

    @Test
    public void test_validerDatagrunnlag_uten_feil() throws Exception {
        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);

        data.setArkivId("123");
        data.setAktørId(AKTØR_ID);
        data.setAktørId(AKTØR_ID);
        data.setDokumentKategori(DokumentKategori.SØKNAD);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        task.precondition(data);
    }
    @Test
    // https://jira.adeo.no/browse/PFP-1730
    public void skalIkkeOppretteNySakHvisDetFinnesEksisterende() {
        String saksnr = "GjenspeilDinSjel";
        VurderFagsystemResultat vurderFagsystemRespons = new VurderFagsystemResultat();
        vurderFagsystemRespons.setSaksnummer(saksnr);
        vurderFagsystemRespons.setBehandlesIVedtaksløsningen(true);
        when(fagsakRestKlient.vurderFagsystem(any())).thenReturn(vurderFagsystemRespons);

        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");
        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);
        ptData.setAktørId("1");
        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        MottakMeldingDataWrapper res = task.doTask(ptData);
        verify(fagsakRestKlient, never()).opprettSak(any());
        assertEquals(saksnr, res.getSaksnummer().get());
    }

}
