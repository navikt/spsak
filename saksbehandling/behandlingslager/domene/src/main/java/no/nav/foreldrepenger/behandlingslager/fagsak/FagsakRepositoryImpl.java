package no.nav.foreldrepenger.behandlingslager.fagsak;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class FagsakRepositoryImpl implements FagsakRepository {

    private EntityManager entityManager;
    private FagsakLåsRepositoryImpl fagsakLåsRepository;

    FagsakRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public FagsakRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.fagsakLåsRepository = new FagsakLåsRepositoryImpl(entityManager);
    }

    @Override
    public Fagsak finnEksaktFagsak(long fagsakId) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where id=:fagsakId", Fagsak.class);
        query.setParameter("fagsakId", fagsakId);
        Fagsak fagsak = HibernateVerktøy.hentEksaktResultat(query);
        entityManager.refresh(fagsak); // hent alltid på nytt
        return fagsak;
    }

    @Override
    public Optional<Fagsak> finnUnikFagsak(long fagsakId) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where id=:fagsakId", Fagsak.class);
        query.setParameter("fagsakId", fagsakId);
        Optional<Fagsak> opt = HibernateVerktøy.hentUniktResultat(query);
        if (opt.isPresent()) {
            entityManager.refresh(opt.get());
        }
        return opt;
    }

    @Override
    public List<Fagsak> hentForBruker(AktørId aktørId) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where navBruker.aktørId=:aktørId", Fagsak.class);
        query.setParameter("aktørId", aktørId);
        return query.getResultList();
    }

    @Override
    public List<Fagsak> hentForBrukerAktørId(AktørId aktørId) {
        TypedQuery<Fagsak> query = entityManager
            .createQuery("select fagsak from Fagsak fagsak join fagsak.navBruker bruk where bruk.aktørId=:aktoerId", Fagsak.class);
        query.setParameter("aktoerId", aktørId);
        return query.getResultList();
    }

    @Override
    public Optional<Journalpost> hentJournalpost(JournalpostId journalpostId) {
        TypedQuery<Journalpost> query = entityManager.createQuery("from Journalpost where journalpostId=:journalpost",
            Journalpost.class);
        query.setParameter("journalpost", journalpostId);
        List<Journalpost> journalposter = query.getResultList();
        return journalposter.isEmpty() ? Optional.empty() : Optional.ofNullable(journalposter.get(0));
    }

    @Override
    public Optional<Fagsak> hentSakGittSaksnummer(Saksnummer saksnummer) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where saksnummer=:saksnummer", Fagsak.class);
        query.setParameter("saksnummer", saksnummer);

        List<Fagsak> fagsaker = query.getResultList();
        if (fagsaker.size() > 1) {
            throw FagsakFeil.FACTORY.flereEnnEnFagsakForSaksnummer(saksnummer).toException();
        }

        return fagsaker.isEmpty() ? Optional.empty() : Optional.of(fagsaker.get(0));
    }

    @Override
    public Long opprettNy(Fagsak fagsak) {
        if (fagsak.getId() != null) {
            throw new IllegalStateException("Fagsak [" + fagsak.getId() + "] eksisterer. Kan ikke opprette på ny");
        }
        entityManager.persist(fagsak.getNavBruker());
        entityManager.persist(fagsak);
        entityManager.flush();
        return fagsak.getId();
    }

    @Override
    public void oppdaterSaksnummer(Long fagsakId, Saksnummer saksnummer) {
        Fagsak fagsak = finnEksaktFagsak(fagsakId);
        fagsak.setSaksnummer(saksnummer);
        entityManager.persist(fagsak);
        entityManager.flush();

    }

    @Override
    public Optional<Fagsak> hentSakGittSaksnummer(Saksnummer saksnummer, boolean taSkriveLås) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where saksnummer=:saksnummer", Fagsak.class);
        query.setParameter("saksnummer", saksnummer);
        if (taSkriveLås) {
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }

        List<Fagsak> fagsaker = query.getResultList();
        if (fagsaker.size() > 1) {
            throw FagsakFeil.FACTORY.flereEnnEnFagsakForSaksnummer(saksnummer).toException();
        }

        return fagsaker.isEmpty() ? Optional.empty() : Optional.of(fagsaker.get(0));
    }

    @Override
    public Long lagre(Journalpost journalpost) {
        entityManager.persist(journalpost);
        return journalpost.getId();
    }

    @Override
    public void oppdaterFagsakStatus(Long fagsakId, FagsakStatus status) {
        Fagsak fagsak = finnEksaktFagsak(fagsakId);
        fagsak.oppdaterStatus(status);
        entityManager.persist(fagsak);
        entityManager.flush();
    }

    @Override
    public List<Fagsak> hentForStatus(FagsakStatus fagsakStatus) {
        TypedQuery<Fagsak> query = entityManager.createQuery("select fagsak from Fagsak fagsak where fagsak.fagsakStatus=:fagsakStatus", Fagsak.class);
        query.setParameter("fagsakStatus", fagsakStatus);

        return query.getResultList();
    }

    /**
     * Oppretter en skrivelås på Fagsak.
     */
    @Override
    public FagsakLås taSkriveLås(Long fagsakId) {
        return fagsakLåsRepository.taLås(fagsakId);
    }

    /**
     * Verifiserer skrivelås
     */
    @Override
    public void verifiserLås(FagsakLås lås) {
        fagsakLåsRepository.oppdaterLåsVersjon(lås);
    }
}
