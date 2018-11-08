package no.nav.foreldrepenger.domene.dokumentarkiv;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface DokumentArkivTjeneste {

    byte[] hentDokumnet(JournalpostId journalpostId, String dokumentId);

    List<ArkivJournalPost> hentAlleDokumenterForVisning(Saksnummer saksnummer);

    List<ArkivJournalPost> hentAlleJournalposterForSak(Saksnummer saksnummer);

    Optional<ArkivJournalPost> hentJournalpostForSak(Saksnummer saksnummer, JournalpostId journalpostId);

    Set<DokumentTypeId> hentDokumentTypeIdForSak(Saksnummer saksnummer, LocalDate mottattEtterDato, List<DokumentTypeId> eksisterende);

    DokumentTypeId utledDokumentTypeFraTittel(Saksnummer saksnummer, JournalpostId journalpostId);
}
