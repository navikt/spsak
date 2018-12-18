package no.nav.foreldrepenger.mottak.tjeneste;

import static no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId.ANNET;
import static no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.KanIkkeFerdigstilleJournalFøringException;
import no.nav.foreldrepenger.mottak.journal.JournalPost;
import no.nav.foreldrepenger.mottak.journal.JournalPostMangler;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;

public class TilJournalføringTjenesteTest {

    private TilJournalføringTjeneste tilJournalføringTjeneste; // objektet vi tester
    private JournalTjeneste mockJournalTjeneste;
    private JournalPostMangler mockJournalføringsbehov;
    private DokumentRepository mockDokumentRepository;

    private FagsakRestKlient fagsakRestKlient;

    private static final String ARKIV_ID = "123";
    private static final String SAK_ID = "456";
    private static final String ENHET_ID = "en003";
    private static final String AKTØR_ID = "123";
    public static final String AVSENDER_ID = "3000";

    @Before
    public void setup() {
        mockJournalføringsbehov = mock(JournalPostMangler.class);
        mockJournalTjeneste = mock(JournalTjeneste.class);
        mockDokumentRepository = mock(DokumentRepository.class);
        fagsakRestKlient = mock(FagsakRestKlient.class);
        when(mockJournalTjeneste.utledJournalføringsbehov(anyString())).thenReturn(mockJournalføringsbehov);
        tilJournalføringTjeneste = new TilJournalføringTjeneste(mockJournalTjeneste, fagsakRestKlient, mockDokumentRepository);
    }

    @Test
    public void skal_ferdigstille_journalføring_hvis_ikke_har_mangler() {

        when(mockJournalføringsbehov.harMangler()).thenReturn(false);

        tilJournalføringTjeneste.tilJournalføring(ARKIV_ID, SAK_ID, AKTØR_ID, ENHET_ID);

        ArgumentCaptor<JournalpostKnyttningDto> captorJournalpostknyttning = ArgumentCaptor.forClass(JournalpostKnyttningDto.class);
        verify(fagsakRestKlient).knyttSakOgJournalpost(captorJournalpostknyttning.capture());
        JournalpostKnyttningDto dto = captorJournalpostknyttning.getValue();
        assertThat(dto.getSaksnummer()).isEqualTo(SAK_ID);
        assertThat(dto.getJournalpostId()).isEqualTo(ARKIV_ID);

        verify(mockJournalTjeneste).utledJournalføringsbehov(eq(ARKIV_ID));
        verify(mockJournalTjeneste).ferdigstillJournalføring(eq(ARKIV_ID), eq(ENHET_ID));
    }

    @Test
    public void skal_ferdigstille_journalføring_hvis_kan_rette_mangler() {

        when(mockJournalføringsbehov.harMangler()).thenReturn(true).thenReturn(false);
        List<JournalPostMangler.JournalMangelType> mockMangler = Collections.singletonList(JournalPostMangler.JournalMangelType.ARKIVSAK);
        when(mockJournalføringsbehov.getMangler()).thenReturn(mockMangler);

        tilJournalføringTjeneste.tilJournalføring(ARKIV_ID, SAK_ID, AKTØR_ID, ENHET_ID);

        ArgumentCaptor<JournalpostKnyttningDto> captorJournalpostknyttning = ArgumentCaptor.forClass(JournalpostKnyttningDto.class);
        verify(fagsakRestKlient).knyttSakOgJournalpost(captorJournalpostknyttning.capture());
        JournalpostKnyttningDto dto = captorJournalpostknyttning.getValue();
        assertThat(dto.getSaksnummer()).isEqualTo(SAK_ID);
        assertThat(dto.getJournalpostId()).isEqualTo(ARKIV_ID);

        verify(mockJournalTjeneste).utledJournalføringsbehov(eq(ARKIV_ID));

        ArgumentCaptor<JournalPost> captor = ArgumentCaptor.forClass(JournalPost.class);
        verify(mockJournalTjeneste).oppdaterJournalpost(captor.capture());
        JournalPost journalPost = captor.getValue();
        assertThat(journalPost.getArkivSakId()).isEqualTo(SAK_ID);

        verify(mockJournalTjeneste).ferdigstillJournalføring(eq(ARKIV_ID), eq(ENHET_ID));
    }

    @Test(expected = KanIkkeFerdigstilleJournalFøringException.class)
    public void skal_feile_hvis_ikke_kan_rette_mangler() {

        when(mockJournalføringsbehov.harMangler()).thenReturn(true).thenReturn(true);
        List<JournalPostMangler.JournalMangelType> mockMangler = Arrays.asList(JournalPostMangler.JournalMangelType.ARKIVSAK, JournalPostMangler.JournalMangelType.BRUKER);
        when(mockJournalføringsbehov.getMangler()).thenReturn(mockMangler);

        tilJournalføringTjeneste.tilJournalføring(ARKIV_ID, SAK_ID, AKTØR_ID, ENHET_ID);

        ArgumentCaptor<JournalpostKnyttningDto> captorJournalpostknyttning = ArgumentCaptor.forClass(JournalpostKnyttningDto.class);
        verify(fagsakRestKlient).knyttSakOgJournalpost(captorJournalpostknyttning.capture());
        JournalpostKnyttningDto dto = captorJournalpostknyttning.getValue();
        assertThat(dto.getSaksnummer()).isEqualTo(Long.parseLong(SAK_ID));
        assertThat(dto.getJournalpostId()).isEqualTo(ARKIV_ID);

        verify(mockJournalTjeneste).utledJournalføringsbehov(eq(ARKIV_ID));

        ArgumentCaptor<JournalPost> captor = ArgumentCaptor.forClass(JournalPost.class);
        verify(mockJournalTjeneste).oppdaterJournalpost(captor.capture());
        JournalPost journalPost = captor.getValue();
        assertThat(journalPost.getArkivSakId()).isEqualTo(SAK_ID);

        verify(mockJournalTjeneste, never()).ferdigstillJournalføring(any(), any());
    }

    @Test
    public void skal_sende_hoveddokument_med_xml_og_pdf_versjon_med_ett_vedlegg() {
        UUID forsendelseId = UUID.randomUUID();
        JournalTilstand journalTilstand = JournalTilstand.ENDELIG_JOURNALFØRT;
        DokumentforsendelseResponse dokumentforsendelseRespons = DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(journalTilstand, 2);
        when(mockJournalTjeneste.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class)))
                .thenReturn(dokumentforsendelseRespons);

        DokumentMetadata metadata = DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAK_ID);
        List<Dokument> dokumenter = DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, SØKNAD_FORELDREPENGER_FØDSEL);
        dokumenter.add(DokumentforsendelseTestUtil.lagDokument(forsendelseId, ANNET, ArkivFilType.PDFA, false));

        when(mockDokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(metadata);
        when(mockDokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);
        ArgumentCaptor<DokumentforsendelseRequest> captor = ArgumentCaptor.forClass(DokumentforsendelseRequest.class);

        tilJournalføringTjeneste.journalførDokumentforsendelse(forsendelseId, Optional.of(SAK_ID), Optional.of(AVSENDER_ID), true, false);

        verify(mockJournalTjeneste).journalførDokumentforsendelse(captor.capture());
        DokumentforsendelseRequest captured = captor.getValue();
        assertThat(captured.getForsendelseId()).isEqualTo(forsendelseId.toString());
        assertThat(captured.getHoveddokument()).hasSize(2);
        assertThat(captured.getVedlegg()).hasSize(1);
        assertThat(captured.getTittel()).isNullOrEmpty();
    }

    @Test
    public void skal_returnere_respons_med_journalTilstand_endelig() {
        UUID forsendelseId = UUID.randomUUID();
        JournalTilstand journalTilstand = JournalTilstand.ENDELIG_JOURNALFØRT;
        DokumentforsendelseResponse dokumentforsendelseRespons = DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(journalTilstand, 2);
        when(mockJournalTjeneste.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class)))
                .thenReturn(dokumentforsendelseRespons);

        DokumentMetadata metadata = DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAK_ID);
        List<Dokument> dokumenter = DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, SØKNAD_FORELDREPENGER_FØDSEL);

        when(mockDokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(metadata);
        when(mockDokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);
        DokumentforsendelseResponse response = tilJournalføringTjeneste.journalførDokumentforsendelse(forsendelseId, Optional.of(SAK_ID), Optional.of(AVSENDER_ID), true, false);

        assertThat(response).isNotNull();
        assertThat(response.getDokumentIdListe()).hasSize(2);
        assertThat(response.getJournalpostId()).isNotNull();
        assertThat(response.getJournalpostId()).isEqualTo(DokumentforsendelseTestUtil.JOURNALPOST_ID);
        assertThat(response.getJournalTilstand()).isEqualByComparingTo(JournalTilstand.ENDELIG_JOURNALFØRT);
    }

}
