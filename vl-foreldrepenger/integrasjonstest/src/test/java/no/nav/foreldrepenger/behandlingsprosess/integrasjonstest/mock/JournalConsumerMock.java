package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottakKanal;
import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivDokument;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.Kommunikasjonsretning;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalMetadata;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

@Alternative
@Priority(1)
@Dependent
public class JournalConsumerMock implements JournalTjeneste {

    private static boolean emulerManglendeDokumentTypeId = false;
    private static String emulerJournalFEnhet = null;

    @Override
    public List<JournalMetadata<DokumentTypeId>> hentMetadata(JournalpostId journalpostId) {
        JournalMetadata.Builder<DokumentTypeId> builder = JournalMetadata.builder();
        builder.medJournalpostId(new JournalpostId("389426448"));
        builder.medDokumentId("393894448");
        builder.medVariantFormat(VariantFormat.ARKIV);
        builder.medMottakKanal(MottakKanal.EIA);
        builder.medDokumentType(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        builder.medDokumentKategori(DokumentKategori.SØKNAD);
        builder.medArkivFilType(ArkivFilType.XML);
        builder.medErHoveddokument(true);
        builder.medForsendelseMottatt(LocalDate.now());
        builder.medBrukerIdentListe(Collections.emptyList());
        return Collections.singletonList(builder.build());
    }

    @Override
    public ArkivJournalPost hentInngåendeJournalpostHoveddokument(JournalpostId journalpostId, DokumentTypeId dokumentTypeId) {
        DokumentKategori dokumentKategori;
        DokumentTypeId lokal = dokumentTypeId;
        if (emulerManglendeDokumentTypeId) {
            lokal = DokumentTypeId.UDEFINERT;
            dokumentKategori = DokumentKategori.SØKNAD;
        } else {
            if (DokumentTypeId.getSøknadTyper().contains(dokumentTypeId)) {
                dokumentKategori = DokumentKategori.SØKNAD;
            } else if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
                dokumentKategori = DokumentKategori.ELEKTRONISK_SKJEMA;
            } else if (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)) {
                dokumentKategori = DokumentKategori.KLAGE_ELLER_ANKE;
            } else {
                dokumentKategori = DokumentKategori.UDEFINERT;
            }
        }
        ArkivDokument.Builder dBuilder = ArkivDokument.Builder.ny().medDokumentTypeId(lokal)
            .medDokumentKategori(dokumentKategori).medTittel("Blablabla").medDokumentId("393894448");
        return ArkivJournalPost.Builder.ny().medJournalpostId(journalpostId).medHoveddokument(dBuilder.build())
            .medTidspunkt(LocalDate.now()).medJournalFørendeEnhet(emulerJournalFEnhet)
            .medKommunikasjonsretning(Kommunikasjonsretning.INN)
            .build();
    }

    public static void setEmulerManglendeDokumentTypeId(boolean emuler) {
        emulerManglendeDokumentTypeId = emuler;
    }

    public static void setEmulerJournalFEnhet(String enhet) {
        emulerJournalFEnhet = enhet;
    }
}
