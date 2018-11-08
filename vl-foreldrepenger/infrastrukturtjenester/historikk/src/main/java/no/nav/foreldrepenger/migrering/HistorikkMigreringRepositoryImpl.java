package no.nav.foreldrepenger.migrering;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringRepository;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class HistorikkMigreringRepositoryImpl implements HistorikkMigreringRepository {

    private static final int BULK_SIZE = 1000;
    private EntityManager entityManager;

    public HistorikkMigreringRepositoryImpl() {
        // NOSONAR
    }

    @Inject
    public HistorikkMigreringRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void lagre(Historikkinnslag historikkinnslag) {
        entityManager.persist(historikkinnslag);
        for (HistorikkinnslagDel historikkinnslagDel : historikkinnslag.getHistorikkinnslagDeler()) {
            entityManager.persist(historikkinnslagDel);
            for (HistorikkinnslagFelt historikkinnslagFelt : historikkinnslagDel.getHistorikkinnslagFelt()) {
                entityManager.persist(historikkinnslagFelt);
            }
        }
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public Iterator<Historikkinnslag> hentAlleHistorikkinnslag() {
        List<Long> alleHistorikkinnslag = hentIdForAlleHistorikkinnslagUtenDeler();

        return new Iterator<Historikkinnslag>() {
            private final int antallHistorikkinnslag = alleHistorikkinnslag.size();
            int nextIndex = 0;
            Iterator<Historikkinnslag> bulkIterator;

            @Override
            public boolean hasNext() {
                return nextIndex < antallHistorikkinnslag;
            }

            @Override
            public Historikkinnslag next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                if (nextIndex % BULK_SIZE == 0) {
                    int readTo = nextIndex + BULK_SIZE;
                    if (readTo > antallHistorikkinnslag) {
                        readTo = antallHistorikkinnslag;
                    }
                    List<Long> idsToRead = alleHistorikkinnslag.subList(nextIndex, readTo);
                    List<Historikkinnslag> bulkList = hentHistorikkinnslagMedId(idsToRead);
                    this.bulkIterator = bulkList.iterator();
                }
                nextIndex++;
                return bulkIterator.next();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private List<Long> hentIdForAlleHistorikkinnslagUtenDeler() {
        Query query = entityManager.createQuery("SELECT hi.id FROM Historikkinnslag hi WHERE size(hi.historikkinnslagDeler) = 0");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<Historikkinnslag> hentHistorikkinnslagMedId(List<Long> idsToRead) {
        Query query = entityManager.createQuery("SELECT hi FROM Historikkinnslag hi where hi.id in (:list)");
        query.setParameter("list", idsToRead);
        return query.getResultList();
    }
}
