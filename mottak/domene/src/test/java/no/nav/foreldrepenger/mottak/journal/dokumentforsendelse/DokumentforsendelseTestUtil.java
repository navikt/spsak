package no.nav.foreldrepenger.mottak.journal.dokumentforsendelse;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;

public class DokumentforsendelseTestUtil {

    public static final String BRUKER_ID = "1234";
    public static final String JOURNALPOST_ID = "234567";
    public static final LocalDateTime FORSENDELSE_MOTTATT = LocalDateTime.now();
    public static final byte[] BLOB = "Bare litt testing".getBytes(Charset.forName("UTF-8"));

    public static DokumentMetadata lagMetadata(UUID forsendelseId, String saksnummer) {
        return DokumentMetadata.builder()
                .setForsendelseMottatt(FORSENDELSE_MOTTATT)
                .setBrukerId(BRUKER_ID)
                .setForsendelseId(forsendelseId)
                .setSaksnummer(saksnummer)
                .build();
    }

    public static Dokument lagDokument(UUID forsendelseId, DokumentTypeId dokumentTypeId, ArkivFilType arkivFilType, boolean erHoveddokument) {
        return Dokument.builder()
                .setForsendelseId(forsendelseId)
                .setDokumentInnhold(BLOB, arkivFilType)
                .setHovedDokument(erHoveddokument)
                .setDokumentTypeId(dokumentTypeId)
                .build();
    }

    public static List<Dokument> lagHoveddokumentMedXmlOgPdf(UUID forsendelseId, DokumentTypeId dokumentTypeId) {
        List<Dokument> dokumenter = new ArrayList<>();
        dokumenter.add(lagDokument(forsendelseId, dokumentTypeId, ArkivFilType.XML, true));
        dokumenter.add(lagDokument(forsendelseId, dokumentTypeId, ArkivFilType.PDFA, true));
        return dokumenter;
    }

    public static DokumentforsendelseResponse lagDokumentforsendelseRespons(JournalTilstand journalTilstand, int antallDokumenter) {
        List<String> dokIdListe = new ArrayList<>();
        while (antallDokumenter > 0) {
            dokIdListe.add("1234" + antallDokumenter);
            --antallDokumenter;
        }
        return DokumentforsendelseResponse.builder()
                .medJournalpostId(JOURNALPOST_ID)
                .medJournalTilstand(journalTilstand)
                .medDokumentIdListe(dokIdListe)
                .build();
    }
}
