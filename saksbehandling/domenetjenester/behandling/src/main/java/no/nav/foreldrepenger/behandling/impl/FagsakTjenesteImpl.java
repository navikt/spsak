package no.nav.foreldrepenger.behandling.impl;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEventPubliserer;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

@ApplicationScoped
public class FagsakTjenesteImpl implements FagsakTjeneste {


    private FagsakRepository fagsakRepository;
    private FagsakStatusEventPubliserer fagsakStatusEventPubliserer;

    FagsakTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public FagsakTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider,
                              FagsakStatusEventPubliserer fagsakStatusEventPubliserer) {
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.fagsakStatusEventPubliserer = fagsakStatusEventPubliserer;
    }


    @Override
    public void opprettFagsak(Fagsak nyFagsak, Personinfo personInfo) {
        validerNyFagsak(nyFagsak);
        fagsakRepository.opprettNy(nyFagsak);
        if (fagsakStatusEventPubliserer != null) {
            fagsakStatusEventPubliserer.fireEvent(nyFagsak, nyFagsak.getStatus());
        }
    }

    private void validerNyFagsak(Fagsak fagsak) {
        if (fagsak.getId() != null || !Objects.equals(fagsak.getStatus(), FagsakStatus.OPPRETTET)) {
            throw new IllegalArgumentException("Kan ikke kalle opprett fagsak med eksisterende: " + fagsak); //$NON-NLS-1$
        }
    }

    private void validerEksisterendeFagsak(Fagsak fagsak) {
        if (fagsak.getId() == null || Objects.equals(fagsak.getStatus(), FagsakStatus.OPPRETTET)) {
            throw new IllegalArgumentException("Kan ikke kalle oppdater med ny fagsak: " + fagsak); //$NON-NLS-1$
        }
    }

    /**
     * kun til test bruk .
     */
    void oppdaterFagsak(Fagsak fagsak) {
        validerEksisterendeFagsak(fagsak);

        fagsakRepository.opprettNy(fagsak);
    }

    @Override
    public Optional<Fagsak> finnFagsakGittSaksnummer(Saksnummer saksnummer, boolean taSkriveLås) {
        return fagsakRepository.hentSakGittSaksnummer(saksnummer, taSkriveLås);
    }

    @Override
    public Fagsak finnEksaktFagsak(long fagsakId) {
        return fagsakRepository.finnEksaktFagsak(fagsakId);
    }

    @Override
    public void oppdaterFagsakMedGsakSaksnummer(Long fagsakId, Saksnummer saksnummer) {
        fagsakRepository.oppdaterSaksnummer(fagsakId, saksnummer);
    }

    @Override
    public void lagreJournalPost(Journalpost journalpost) {
        fagsakRepository.lagre(journalpost);
    }

    @Override
    public Optional<Journalpost> hentJournalpost(JournalpostId journalpostId) {
        return fagsakRepository.hentJournalpost(journalpostId);
    }

}
