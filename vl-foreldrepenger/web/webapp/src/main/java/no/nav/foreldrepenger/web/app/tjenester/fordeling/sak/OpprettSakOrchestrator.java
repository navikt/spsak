package no.nav.foreldrepenger.web.app.tjenester.fordeling.sak;

import no.nav.foreldrepenger.domene.typer.JournalpostId;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

@ApplicationScoped
public class OpprettSakOrchestrator {
    private OpprettSakTjeneste opprettSakTjeneste;
    private FagsakRepository fagsakRepository;

    @Inject
    public OpprettSakOrchestrator(OpprettSakTjeneste opprettSakTjeneste, FagsakRepository fagsakRepository) {
        this.opprettSakTjeneste = opprettSakTjeneste;
        this.fagsakRepository = fagsakRepository;
    }

    public OpprettSakOrchestrator() { // NOSONAR: cdi
    }

    public Saksnummer opprettSak(AktørId aktørId) {
        Fagsak fagsak = opprettSakTjeneste.opprettSakVL(aktørId);
        return opprettEllerFinnGsak(aktørId, fagsak);
    }

    public Saksnummer opprettSak(JournalpostId journalpostId, AktørId aktørId) {
        Saksnummer saksnummer;
        Fagsak fagsak = finnEllerOpprettFagSak(journalpostId, aktørId);
        if (fagsak.getSaksnummer() != null) {
            saksnummer = fagsak.getSaksnummer();
        } else {
            saksnummer = opprettEllerFinnGsak(aktørId, fagsak);
        }
        return saksnummer;
    }

    private Fagsak finnEllerOpprettFagSak(JournalpostId journalpostId, AktørId aktørId) {
        Optional<Journalpost> journalpost = fagsakRepository.hentJournalpost(journalpostId);
        if (journalpost.isPresent()) {
            return journalpost.get().getFagsak();
        }
        return opprettSakTjeneste.opprettSakVL(aktørId, journalpostId);
    }

    private Saksnummer opprettEllerFinnGsak(AktørId aktørId, Fagsak fagsak) {
        Saksnummer saksnummer;
        try {
            saksnummer = opprettSakTjeneste.opprettSakIGsak(fagsak.getId(), aktørId);
        } catch (SakEksistererAlleredeException ignored) { //NOSONAR
            Optional<Saksnummer> gsakId = opprettSakTjeneste.finnGsak(fagsak.getId());
            if (gsakId.isPresent()) {
                saksnummer = gsakId.get();
            } else {
                throw OpprettSakFeil.FACTORY.fantIkkeSakenSomGsakSaAlleredeEksisterer(fagsak.getId()).toException();
            }
        }
        opprettSakTjeneste.oppdaterFagsakMedGsakSaksnummer(fagsak.getId(), saksnummer);
        return saksnummer;
    }

    public void knyttSakOgJournalpost(Saksnummer saksnummer, JournalpostId journalpostId) {
        opprettSakTjeneste.knyttSakOgJournalpost(saksnummer, journalpostId);
    }
}
