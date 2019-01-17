package no.nav.foreldrepenger.fordel.dokument.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.fordel.kodeverk.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverk.VariantFormat;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.KonfigVerdiTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.feil.WSUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.meldinger.WSOppdaterOgFerdigstillJournalfoeringRequest;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;


public class BehandleDokumentServiceTest {

    private BehandleDokumentService behandleDokumentService;

    private static final String JOURNALPOST_ID = "123";
    private static final String ENHETID = "4567";
    private static final String SAKSNUMMER = "789";
    private static final Long FAGSAK_ID = 246L;
    private static final String AKTØR_ID = "9000000000002";
    private static final String BRUKER_FNR = "01234567890";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private TilJournalføringTjeneste tilJournalføringTjenesteMock;
    private HentDataFraJoarkTjeneste hentDataFraJoarkTjenesteMock;
    private KlargjørForVLTjeneste klargjørForVLTjenesteMock;
    private FagsakRestKlient fagsakRestKlientMock;
    private KodeverkRepository kodeverkRepository;
    private KonfigVerdiTjeneste konfigVerdiTjeneste;
    private AktørConsumer aktørConsumer;

    private BehandlingTema engangsstønadFødsel;
    private BehandlingTema foreldrepengerFødsel;
    private BehandlingTema foreldrepenger;
    private BehandlingTema engangsstønad;
    private JournalMetadata<DokumentTypeId> journalMetadata;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        engangsstønadFødsel = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        foreldrepengerFødsel = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.FORELDREPENGER_FØDSEL);
        engangsstønad = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD);
        foreldrepenger = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.FORELDREPENGER);
        DokumentTypeId dokumentTypeId = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        fagsakRestKlientMock = mock(FagsakRestKlient.class);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, engangsstønad.getOffisiellKode())));

        journalMetadata = mock(JournalMetadata.class);
        when(journalMetadata.getJournaltilstand()).thenReturn(JournalMetadata.Journaltilstand.MIDLERTIDIG);
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);

        hentDataFraJoarkTjenesteMock = mock(HentDataFraJoarkTjeneste.class);
        when(hentDataFraJoarkTjenesteMock.hentHoveddokumentMetadata(JOURNALPOST_ID)).thenReturn(Optional.of(journalMetadata));

        JournalDokument<?> journalDokument = mock(JournalDokument.class);
        when(hentDataFraJoarkTjenesteMock.hentStrukturertJournalDokument(any(JournalMetadata.class))).thenReturn(Optional.of(journalDokument));

        tilJournalføringTjenesteMock = mock(TilJournalføringTjeneste.class);
        klargjørForVLTjenesteMock = mock(KlargjørForVLTjeneste.class);

        konfigVerdiTjeneste = mock(KonfigVerdiTjeneste.class);
        when(konfigVerdiTjeneste.getKonfigVerdiStartdatoForeldrepenger()).thenReturn(LocalDate.of(2019, 1, 1));
        aktørConsumer = mock(AktørConsumer.class);
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.empty());
        when(aktørConsumer.hentAktørIdForPersonIdent(BRUKER_FNR)).thenReturn(Optional.of(AKTØR_ID));

        behandleDokumentService = new BehandleDokumentService(tilJournalføringTjenesteMock, hentDataFraJoarkTjenesteMock, klargjørForVLTjenesteMock,
                fagsakRestKlientMock, kodeverkRepository, konfigVerdiTjeneste, aktørConsumer);
    }

    @Test
    public void skalValiderePåkrevdInput_enhetId() throws Exception {
        expectedException.expect(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class);
        expectedException.expectMessage(BehandleDokumentService.ENHET_MANGLER);

        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(null, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalValiderePåkrevdInput_journalpostId() throws Exception {
        expectedException.expect(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class);
        expectedException.expectMessage(BehandleDokumentService.JOURNALPOST_MANGLER);

        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, null, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalValiderePåkrevdInput_saksnummer() throws Exception {
        expectedException.expect(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class);
        expectedException.expectMessage(BehandleDokumentService.SAKSNUMMER_UGYLDIG);

        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, null);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalValidereAtFagsakFinnes() throws Exception {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("finner ikke fagsak");
        when(fagsakRestKlientMock.finnFagsakInfomasjon(any()))
                .thenReturn(Optional.empty());

        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test(expected = OppdaterOgFerdigstillJournalfoeringUgyldigInput.class)
    public void skalIkkeJournalføreKlagerPåSakUtenBehandling() throws Exception {

        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.UDEFINERT).getOffisiellKode())));

        when(journalMetadata.getDokumentTypeId()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        when(journalMetadata.getDokumentKategori()).thenReturn(DokumentKategori.KLAGE_ELLER_ANKE);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalKunneJournalføreKlagerPåSakMedBehandling() throws Exception {

        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        when(journalMetadata.getDokumentTypeId()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        when(journalMetadata.getDokumentKategori()).thenReturn(DokumentKategori.KLAGE_ELLER_ANKE);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalKjøreHeltIgjennomNaarJournaltilstandIkkeErEndelig() throws Exception {
        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock).tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(engangsstønadFødsel), any(), any(), any());
    }

    @Test
    public void skalTillateJournalførinAvInntektsmeldingForeldrepender() throws Exception {
        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        DokumentTypeId dokumentTypeId = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.INNTEKTSMELDING);
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepengerFødsel.getOffisiellKode())));

        JournalMetadata<DokumentTypeId> dokument = lagJournalMetadata(DokumentTypeId.INNTEKTSMELDING);
        List<JournalMetadata<DokumentTypeId>> metadata = Collections.singletonList(dokument);
        String xml = readFile("testdata/inntektsmelding-foreldrepenger.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock).tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(foreldrepengerFødsel), any(), any(), any());
    }

    @Test(expected = OppdaterOgFerdigstillJournalfoeringUgyldigInput.class)
    public void skalIkkeTillateJournalførinAvInntektsmeldingSvangerskapspenger() throws Exception {
        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        DokumentTypeId dokumentTypeId = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.INNTEKTSMELDING);
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepengerFødsel.getOffisiellKode())));

        JournalMetadata<DokumentTypeId> dokument = lagJournalMetadata(DokumentTypeId.INNTEKTSMELDING);
        List<JournalMetadata<DokumentTypeId>> metadata = Collections.singletonList(dokument);
        String xml = readFile("testdata/inntektsmelding-svangerskapspenger.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test(expected = OppdaterOgFerdigstillJournalfoeringUgyldigInput.class)
    public void skalIkkeTillateJournalførinAvSøknadMedUttakFørGrense() throws Exception {
        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        DokumentTypeId dokumentTypeId = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));

        JournalMetadata<DokumentTypeId> dokument = lagJournalMetadata(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        List<JournalMetadata<DokumentTypeId>> metadata = Collections.singletonList(dokument);
        String xml = readFile("testdata/selvb-soeknad-forp-uttak-før-konfigverdi.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        try {
            behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
        } catch (OppdaterOgFerdigstillJournalfoeringUgyldigInput e) {
            assertThat(e.getMessage()).contains("2018");
            throw e;
        }
    }

    @Test
    public void skalTillateJournalførinAvSøknadMedUttakEtterGrense() throws Exception {
        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        DokumentTypeId dokumentTypeId = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));

        JournalMetadata<DokumentTypeId> dokument = lagJournalMetadata(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        List<JournalMetadata<DokumentTypeId>> metadata = Collections.singletonList(dokument);
        String xml = readFile("testdata/selvb-soeknad-forp.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock).tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(foreldrepengerFødsel), any(), any(), any());
    }

    @Test
    public void skalKjøreHeltIgjennomNaarJournaltilstandErEndelig() throws Exception {
        when(journalMetadata.getJournaltilstand()).thenReturn(JournalMetadata.Journaltilstand.ENDELIG);

        WSOppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock, never()).tilJournalføring(any(), any(), any(), any());
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(engangsstønadFødsel), any(), any(), any());
    }

    private WSOppdaterOgFerdigstillJournalfoeringRequest lagRequest(String enhetid, String journalpostId, String sakId) {
        WSOppdaterOgFerdigstillJournalfoeringRequest request = new WSOppdaterOgFerdigstillJournalfoeringRequest();
        request.setEnhetId(enhetid);
        request.setJournalpostId(journalpostId);
        request.setSakId(sakId);
        return request;
    }

    JournalMetadata<DokumentTypeId> lagJournalMetadata(DokumentTypeId dokumentTypeId) {
        JournalMetadata.Builder<DokumentTypeId> builder = JournalMetadata.builder();
        builder.medJournalpostId(JOURNALPOST_ID);
        builder.medDokumentId(ENHETID);
        builder.medVariantFormat(VariantFormat.FULLVERSJON);
        builder.medMottakKanal(MottakKanal.EIA);
        builder.medDokumentType(dokumentTypeId);
        builder.medDokumentKategori(DokumentKategori.UDEFINERT);
        builder.medArkivFilType(ArkivFilType.XML);
        builder.medErHoveddokument(true);
        builder.medForsendelseMottatt(LocalDate.now());
        builder.medForsendelseMottattTidspunkt(LocalDateTime.now());
        builder.medBrukerIdentListe(Collections.singletonList(BRUKER_FNR));
        return builder.build();
    }

    String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
