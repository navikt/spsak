package no.nav.foreldrepenger.behandling;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface FagsakTjeneste {

    void opprettFagsak(Fagsak nyFagsak, Personinfo personInfo);

    Optional<Fagsak> finnFagsakGittSaksnummer(Saksnummer saksnummer, boolean taSkriveLås);

    Fagsak finnEksaktFagsak(long fagsakId);

    void oppdaterFagsakMedGsakSaksnummer(Long fagsakId, Saksnummer saksnummer);

    void lagreJournalPost(Journalpost journalpost);

    Optional<Journalpost> hentJournalpost(JournalpostId journalpostId);

}
