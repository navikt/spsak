package no.nav.foreldrepenger.mottak.task.dokumentforsendelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class BehandleDokumentforsendelseTaskTest {

    // TODO (Humle): enhetstestene/test XML må oppdateres når vi har reell søknads XML tilgjengelig

    private static final UUID FORSENDELSE_ID = UUID.randomUUID();
    private static final String AKTØR_ID = "9000000000002";
    private static final String PERSON_IDENT = "12345678901";
    private static final String FIL_SØKNAD_ENGST = "engangsstoenad-termin-soeknad.xml";
    private static final String FIL_SØKNAD_FORP = "selvb-soeknad-forp.xml";
    private static final String FIL_SØKNAD_FORP_UTTAK_FØR_KONFIGVERDI = "selvb-soeknad-forp-uttak-før-konfigverdi.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ProsessTaskRepository prosessTaskRepository = mock(ProsessTaskRepository.class);
    private AktørConsumer aktørConsumer = mock(AktørConsumer.class);
    private FagsakRestKlient fagsakRestKlient = mock(FagsakRestKlient.class);
    private DokumentRepository dokumentRepository = mock(DokumentRepository.class);
    private KodeverkRepository kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
    private String konfigVerdiStartdatoForeldrepenger = LocalDate.of(2019, 01, 01).toString();

    private BehandleDokumentforsendelseTask fordelDokTask;
    private ProsessTaskData ptd;

    @Before
    public void setup() {
        fordelDokTask = new BehandleDokumentforsendelseTask(prosessTaskRepository, kodeverkRepository, aktørConsumer, fagsakRestKlient, dokumentRepository, konfigVerdiStartdatoForeldrepenger);
        ptd = new ProsessTaskData(BehandleDokumentforsendelseTask.TASKNAME);
        ptd.setSekvens("1");

        when(aktørConsumer.hentPersonIdentForAktørId(any())).thenReturn(Optional.of(PERSON_IDENT));
    }

    @Test
    public void testAvPrecondition() {
        MottakMeldingDataWrapper innData = new MottakMeldingDataWrapper(kodeverkRepository, ptd);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-941984");

        fordelDokTask.precondition(innData);
    }

    @Test
    public void testAvPostCondition() {
        MottakMeldingDataWrapper utData = new MottakMeldingDataWrapper(kodeverkRepository, ptd);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-638068");

        fordelDokTask.postcondition(utData);
    }

    @Test
    @Ignore
    public void skalReturnereWrapperMedNesteTaskHentOgVurderVLSak() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.of(genDokument(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, FIL_SØKNAD_ENGST, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata(null));

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
        assertThat(utdata.getDokumentTypeId()).hasValue(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
    }

    @Test
    public void skalReturnereHentOgVurderVLSakTask() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata(null));

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
        assertThat(utdata.getDokumentTypeId()).hasValue(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
    }

    @Test
    @Ignore
    public void skalReturnereOpprettGSakOppgaveTaskHvisSøknadDatoForForeldrependerStartDato() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP_UTTAK_FØR_KONFIGVERDI, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata(null));

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skalSendeTilJournalføringHvisSaksnummerFinnesPåMetadataOgIVL() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123"));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class))).thenReturn(genFagsakInformasjon("ab0047"));

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
    }

    @Test
    public void skalEttersendtVedleggTilJournalføring() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);
        inndata.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.empty());
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123"));
        when(dokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(Collections.singletonList(genDokument(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, FIL_SØKNAD_FORP, false)));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class))).thenReturn(genFagsakInformasjon("ab0047"));

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(utdata.getDokumentTypeId()).hasValue(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL);
    }

    @Test
    public void skalSendeTilOpprettOppgaveHvisSaksnummerFinnesPåMetadataMenIkkeIVL() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123"));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class))).thenReturn(Optional.empty());

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skalFeileHvisBehandlingstemaIkkeMatcher() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123"));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class))).thenReturn(genFagsakInformasjon("ab0072"));

        assertThatThrownBy(() -> kjørMedPreOgPostcondition(inndata))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FP-756353");
    }

    private Optional<FagsakInfomasjonDto> genFagsakInformasjon(String behTemaOffisiellKode) {
        FagsakInfomasjonDto dto = new FagsakInfomasjonDto(AKTØR_ID, behTemaOffisiellKode);

        return Optional.of(dto);
    }

    private MottakMeldingDataWrapper kjørMedPreOgPostcondition(MottakMeldingDataWrapper inndata) {
        MottakMeldingDataWrapper utdata;
        fordelDokTask.precondition(inndata);
        utdata = fordelDokTask.doTask(inndata);
        fordelDokTask.postcondition(utdata);
        return utdata;
    }

    private Dokument genDokument(DokumentTypeId dokumentTypeId, String testXmlFilnavn, boolean hovedDokument) {
        String xml = "";
        try {
            xml = readFile("testsoknader/" + testXmlFilnavn);
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        Dokument dokument = Dokument.builder()
                .setForsendelseId(FORSENDELSE_ID)
                .setDokumentInnhold(xml.getBytes(Charset.forName("UTF-8")), ArkivFilType.XML)
                .setDokumentTypeId(dokumentTypeId)
                .setHovedDokument(hovedDokument)
                .build();
        return dokument;

    }

    private DokumentMetadata genMetadata(String saksnummer) {
        DokumentMetadata metadata = DokumentMetadata.builder()
                .setForsendelseId(FORSENDELSE_ID)
                .setBrukerId(AKTØR_ID)
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
        if (saksnummer != null) {
            metadata.setSaksnummer(saksnummer);
        }
        return metadata;
    }

    protected String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
