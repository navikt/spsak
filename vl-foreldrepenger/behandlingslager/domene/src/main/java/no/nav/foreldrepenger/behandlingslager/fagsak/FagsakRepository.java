package no.nav.foreldrepenger.behandlingslager.fagsak;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface FagsakRepository extends BehandlingslagerRepository {

    Fagsak finnEksaktFagsak(long fagsakId);

    Optional<Fagsak> finnUnikFagsak(long fagsakId);

    Long opprettNy(Fagsak fagsak);

    List<Fagsak> hentForBruker(AktørId aktørId);

    Optional<Fagsak> hentSakGittSaksnummer(Saksnummer saksnummer);

    List<Fagsak> hentForBrukerAktørId(AktørId aktørId);

    Long lagre(Journalpost journalpost);

    Optional<Journalpost> hentJournalpost(JournalpostId journalpostId);

    /**
     * Oppderer status på fagsak.
     *
     * @param fagsakId - id på fagsak
     * @param status   - ny status
     */
    void oppdaterFagsakStatus(Long fagsakId, FagsakStatus status);

    List<Fagsak> hentForStatus(FagsakStatus fagsakStatus);

    void fagsakSkalBehandlesAvInfotrygd(Long fagsakId);

    FagsakLås taSkriveLås(Long fagsakId);

    void verifiserLås(FagsakLås lås);

    void oppdaterRelasjonsRolle(Long fagsakId, RelasjonsRolleType relasjonsRolleType);

    void oppdaterSaksnummer(Long fagsakId, Saksnummer saksnummer);

    Optional<Fagsak> hentSakGittSaksnummer(Saksnummer saksnummer, boolean taSkriveLås);
}
