package no.nav.foreldrepenger.behandlingslager.behandling;

import no.nav.foreldrepenger.domene.typer.JournalpostId;

public interface InnsynDokument {
    JournalpostId getJournalpostId();

    String getDokumentId();

    boolean isFikkInnsyn();
}
