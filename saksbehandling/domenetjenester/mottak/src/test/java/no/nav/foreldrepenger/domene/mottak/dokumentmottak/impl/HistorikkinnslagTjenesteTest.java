package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottakKanal;
import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalMetadata;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public class HistorikkinnslagTjenesteTest {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("5");
    private static final String HOVEDDOKUMENT_DOKUMENT_ID = "1";
    private static final String VEDLEGG_DOKUMENT_ID = "2";

    private HistorikkRepository historikkRepository;
    private JournalTjeneste journalTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    @Before
    public void before() {
        historikkRepository = mock(HistorikkRepository.class);
        journalTjeneste = mock(JournalTjeneste.class);
        historikkinnslagTjeneste = new HistorikkinnslagTjenesteImpl(historikkRepository, journalTjeneste);
    }

    @Test
    public void skal_lagre_historikkinnslag_for_elektronisk_søknad() throws Exception {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        Behandling behandling = scenario.lagMocked();
        // Arrange

        JournalMetadata<DokumentTypeId> journalMetadataHoveddokumentXml = byggJournalMetadata(JOURNALPOST_ID, HOVEDDOKUMENT_DOKUMENT_ID, ArkivFilType.XML, true, VariantFormat.FULLVERSJON);
        JournalMetadata<DokumentTypeId> journalMetadataHoveddokumentPdf = byggJournalMetadata(JOURNALPOST_ID, HOVEDDOKUMENT_DOKUMENT_ID, ArkivFilType.PDF, true, VariantFormat.ARKIV);
        JournalMetadata<DokumentTypeId> journalMetadataVedlegg = byggJournalMetadata(JOURNALPOST_ID, VEDLEGG_DOKUMENT_ID, ArkivFilType.XML, false, VariantFormat.FULLVERSJON);

        when(journalTjeneste.hentMetadata(JOURNALPOST_ID)).thenReturn(Arrays.asList(journalMetadataHoveddokumentXml, journalMetadataHoveddokumentPdf, journalMetadataVedlegg));

        // Act
        historikkinnslagTjeneste.opprettHistorikkinnslag(behandling, JOURNALPOST_ID);

        // Assert
        ArgumentCaptor<Historikkinnslag> captor = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkRepository, times(1)).lagre(captor.capture());
        Historikkinnslag historikkinnslag = captor.getValue();
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.SØKER);
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.BEH_STARTET);
        assertThat(historikkinnslag.getHistorikkinnslagDeler()).isNotEmpty();

        List<HistorikkinnslagDokumentLink> dokumentLinker = historikkinnslag.getDokumentLinker();
        assertThat(dokumentLinker.size()).isEqualTo(2);
        assertThat(dokumentLinker.get(0).getDokumentId()).isEqualTo(HOVEDDOKUMENT_DOKUMENT_ID);
        assertThat(dokumentLinker.get(0).getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(dokumentLinker.get(0).getLinkTekst()).isEqualTo("Søknad");
        assertThat(dokumentLinker.get(1).getDokumentId()).isEqualTo(VEDLEGG_DOKUMENT_ID);
        assertThat(dokumentLinker.get(1).getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(dokumentLinker.get(1).getLinkTekst()).isEqualTo("Vedlegg");
    }

    @Test
    public void skal_lagre_historikkinnslag_for_papir_søknad() throws Exception {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        Behandling behandling = scenario.lagMocked();
        // Arrange

        JournalMetadata<DokumentTypeId> journalMetadataHoveddokument = byggJournalMetadata(JOURNALPOST_ID, HOVEDDOKUMENT_DOKUMENT_ID, ArkivFilType.PDF, true, VariantFormat.ARKIV);

        when(journalTjeneste.hentMetadata(JOURNALPOST_ID)).thenReturn(Collections.singletonList(journalMetadataHoveddokument));

        // Act
        historikkinnslagTjeneste.opprettHistorikkinnslag(behandling, JOURNALPOST_ID);

        // Assert
        ArgumentCaptor<Historikkinnslag> captor = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkRepository, times(1)).lagre(captor.capture());
        Historikkinnslag historikkinnslag = captor.getValue();
        List<HistorikkinnslagDokumentLink> dokumentLinker = historikkinnslag.getDokumentLinker();
        assertThat(dokumentLinker.size()).isEqualTo(1);
        assertThat(dokumentLinker.get(0).getDokumentId()).isEqualTo(HOVEDDOKUMENT_DOKUMENT_ID);
        assertThat(dokumentLinker.get(0).getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(dokumentLinker.get(0).getLinkTekst()).isEqualTo("Papirsøknad");
    }

    @Test
    public void skal_lagre_historikkinnslag_for_papir_søknad_med_skanning_meta_xml() throws Exception {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        Behandling behandling = scenario.lagMocked();
        // Arrange

        JournalMetadata<DokumentTypeId> journalMetadataHoveddokumentPdf = byggJournalMetadata(JOURNALPOST_ID, HOVEDDOKUMENT_DOKUMENT_ID, ArkivFilType.PDF, true, VariantFormat.ARKIV);
        JournalMetadata<DokumentTypeId> journalMetadataHoveddokumentSkanningXml = byggJournalMetadata(JOURNALPOST_ID, HOVEDDOKUMENT_DOKUMENT_ID, ArkivFilType.XML, true, VariantFormat.SKANNING_META);

        when(journalTjeneste.hentMetadata(JOURNALPOST_ID))
                .thenReturn(Arrays.asList(journalMetadataHoveddokumentPdf, journalMetadataHoveddokumentSkanningXml));

        // Act
        historikkinnslagTjeneste.opprettHistorikkinnslag(behandling, JOURNALPOST_ID);

        // Assert
        ArgumentCaptor<Historikkinnslag> captor = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkRepository, times(1)).lagre(captor.capture());
        Historikkinnslag historikkinnslag = captor.getValue();
        List<HistorikkinnslagDokumentLink> dokumentLinker = historikkinnslag.getDokumentLinker();
        assertThat(dokumentLinker.size()).isEqualTo(1);
        assertThat(dokumentLinker.get(0).getDokumentId()).isEqualTo(HOVEDDOKUMENT_DOKUMENT_ID);
        assertThat(dokumentLinker.get(0).getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(dokumentLinker.get(0).getLinkTekst()).isEqualTo("Papirsøknad");
    }

    @Test
    public void skal_ikke_lagre_historikkinnslag_når_det_allerede_finnes() throws Exception {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        Behandling behandling = scenario.lagMocked();

        Historikkinnslag eksisterendeHistorikkinnslag = new Historikkinnslag();
        eksisterendeHistorikkinnslag.setType(HistorikkinnslagType.BEH_STARTET);
        when(historikkRepository.hentHistorikk(behandling.getId())).thenReturn(Collections.singletonList(eksisterendeHistorikkinnslag));

        // Act
        historikkinnslagTjeneste.opprettHistorikkinnslag(behandling, JOURNALPOST_ID);

        // Assert
        verify(historikkRepository, times(0)).lagre(any(Historikkinnslag.class));
    }

    @Test
    public void skal_støtte_at_journalpostId_er_null_og_ikke_kalle_journalTjeneste() throws Exception {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        Behandling behandling = scenario.lagMocked();

        // Act
        historikkinnslagTjeneste.opprettHistorikkinnslag(behandling, null);

        // Assert
        verify(journalTjeneste, times(0)).hentMetadata(any(JournalpostId.class));
        ArgumentCaptor<Historikkinnslag> captor = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkRepository, times(1)).lagre(captor.capture());
        Historikkinnslag historikkinnslag = captor.getValue();
        assertThat(historikkinnslag.getDokumentLinker()).isEmpty();
    }



    private JournalMetadata<DokumentTypeId> byggJournalMetadata(JournalpostId journalpostId, String dokumentId, ArkivFilType arkivFiltype, boolean hoveddokument, VariantFormat variantFormat) {
        JournalMetadata.Builder<DokumentTypeId> builderHoveddok = JournalMetadata.builder();
        builderHoveddok.medJournalpostId(journalpostId);
        builderHoveddok.medDokumentId(dokumentId);
        builderHoveddok.medVariantFormat(variantFormat);
        builderHoveddok.medMottakKanal(MottakKanal.EIA);
        builderHoveddok.medDokumentType(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        builderHoveddok.medDokumentKategori(DokumentKategori.SØKNAD);
        builderHoveddok.medArkivFilType(arkivFiltype);
        builderHoveddok.medErHoveddokument(hoveddokument);
        builderHoveddok.medForsendelseMottatt(LocalDate.now());
        builderHoveddok.medBrukerIdentListe(Collections.singletonList("01234567890"));
        JournalMetadata<DokumentTypeId> journalMetadataHoveddokument = builderHoveddok.build();
        return journalMetadataHoveddokument;
    }
}
