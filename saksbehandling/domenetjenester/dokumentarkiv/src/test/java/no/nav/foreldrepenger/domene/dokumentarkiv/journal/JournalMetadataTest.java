package no.nav.foreldrepenger.domene.dokumentarkiv.journal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottakKanal;
import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public class JournalMetadataTest {

    @Test
    public void skal_lage_fullt_populert_metadata() {
        // Arrange

        JournalMetadata.Builder<DokumentTypeId> builder = JournalMetadata.builder();

        builder.medJournalpostId(new JournalpostId("jpId"));
        builder.medDokumentId("dokId");
        builder.medVariantFormat(VariantFormat.ARKIV);
        builder.medMottakKanal(MottakKanal.EIA);
        builder.medDokumentType(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        builder.medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA);
        builder.medArkivFilType(ArkivFilType.PDFA);
        builder.medJournaltilstand(JournalMetadata.Journaltilstand.ENDELIG);
        builder.medErHoveddokument(true);
        final LocalDate naa = LocalDate.now();
        builder.medForsendelseMottatt(naa);
        final List<String> brukerIdentListe = Arrays.asList("brId1", "brId2");
        builder.medBrukerIdentListe(brukerIdentListe);

        // Act

        JournalMetadata<DokumentTypeId> jmd = builder.build();

        // Assert

        assertThat(jmd).isNotNull();
        assertThat(jmd.getJournalpostId().getVerdi()).isEqualTo("jpId");
        assertThat(jmd.getDokumentId()).isEqualTo("dokId");
        assertThat(jmd.getVariantFormat()).isEqualTo(VariantFormat.ARKIV);
        assertThat(jmd.getDokumentType()).isEqualTo(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        assertThat(jmd.getDokumentKategori()).isEqualTo(DokumentKategori.ELEKTRONISK_SKJEMA);
        assertThat(jmd.getArkivFilType()).isEqualTo(ArkivFilType.PDFA);
        assertThat(jmd.getJournaltilstand()).isEqualTo(JournalMetadata.Journaltilstand.ENDELIG);
        assertThat(jmd.getErHoveddokument()).isTrue();
        assertThat(jmd.getForsendelseMottatt()).isEqualTo(naa);
        assertThat(jmd.getBrukerIdentListe()).isEqualTo(brukerIdentListe);
    }

    @Test
    public void skal_kompensere_for_null_brukerIdentListe() {
        // Arrange

        JournalMetadata.Builder<DokumentTypeId> builder = JournalMetadata.builder();

        // Act

        JournalMetadata<DokumentTypeId> jmd = builder.build();

        // Assert

        assertThat(jmd).isNotNull();
        assertThat(jmd.getBrukerIdentListe()).isEqualTo(Collections.emptyList());
    }
}
