package no.nav.foreldrepenger.web.app.tjenester.fordeling.sak;

import no.nav.foreldrepenger.domene.typer.JournalpostId;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface OpprettSakTjeneste {

    String FORELDREPENGER_KODE = "FOR";
    String VL_FAGSYSTEM_KODE = Fagsystem.FPSAK.getOffisiellKode();
    String MED_FAGSAK_KODE = "MFS";

    Fagsak opprettSakVL(AktørId aktørId);

    Fagsak opprettSakVL(AktørId aktørId, JournalpostId journalpostId);

    Saksnummer opprettSakIGsak(Long fagsakId, AktørId aktørId);

    Optional<Saksnummer> finnGsak(Long fagsakId);

    void knyttSakOgJournalpost(Saksnummer saksnummer, JournalpostId journalPostId);

    void oppdaterFagsakMedGsakSaksnummer(Long fagsakId, Saksnummer saksnummer);

}
