package no.nav.foreldrepenger.domene.dokumentarkiv.journal.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.InngåendeJournalAdapter;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalMetadata;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

@ApplicationScoped
public class JournalTjenesteImpl implements JournalTjeneste {

    private InngåendeJournalAdapter inngaaendeJournalAdapter;

    public JournalTjenesteImpl() {
        // NOSONAR: cdi
    }

    @Inject
    public JournalTjenesteImpl(InngåendeJournalAdapter inngaaendeJournalAdapter) {
        this.inngaaendeJournalAdapter = inngaaendeJournalAdapter;
    }

    @Override
    public List<JournalMetadata<DokumentTypeId>> hentMetadata(JournalpostId journalpostId) {
        return inngaaendeJournalAdapter.hentMetadata(journalpostId);
    }

    @Override
    public ArkivJournalPost hentInngåendeJournalpostHoveddokument(JournalpostId journalpostId, DokumentTypeId dokumentTypeId) {
        return inngaaendeJournalAdapter.hentInngåendeJournalpostHoveddokument(journalpostId);
    }
}
