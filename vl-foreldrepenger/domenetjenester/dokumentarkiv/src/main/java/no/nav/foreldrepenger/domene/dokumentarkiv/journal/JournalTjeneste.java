package no.nav.foreldrepenger.domene.dokumentarkiv.journal;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public interface JournalTjeneste {

    List<JournalMetadata<DokumentTypeId>> hentMetadata(JournalpostId journalpostId);

    ArkivJournalPost hentInngåendeJournalpostHoveddokument(JournalpostId journalpostId, DokumentTypeId dokumentTypeId);

}
