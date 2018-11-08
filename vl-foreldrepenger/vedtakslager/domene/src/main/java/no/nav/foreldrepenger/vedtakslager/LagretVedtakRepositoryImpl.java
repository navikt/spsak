package no.nav.foreldrepenger.vedtakslager;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;


import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtak;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtakMedBehandlingType;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class LagretVedtakRepositoryImpl implements LagretVedtakRepository {

    private EntityManager entityManager;

    LagretVedtakRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public LagretVedtakRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    // Vedtak i Vedtakslager
    @Override
    public long lagre(LagretVedtak lagretVedtak) {
        if (entityManager.contains(lagretVedtak)) {
            // Eksisterende og persistent - ikke gjør noe
            @SuppressWarnings("unused")
            int brkpt = 1; // NOSONAR
        } else if (lagretVedtak.getId() != null) {
            // Eksisterende men detached - oppdater
            entityManager.merge(lagretVedtak);
        } else {
            // Ny - insert
            entityManager.persist(lagretVedtak);
        }
        return lagretVedtak.getId();
    }

    @Override
    public LagretVedtak hentLagretVedtak(long lagretVedtakId) {
        TypedQuery<LagretVedtak> query = entityManager.createQuery("from LagretVedtak where id=:lagretVedtakId", LagretVedtak.class); //$NON-NLS-1$
        query.setParameter("lagretVedtakId", lagretVedtakId); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        return hentEksaktResultat(query);
    }

    @Override
    public LagretVedtak hentLagretVedtakForBehandling(long behandlingId) {
        TypedQuery<LagretVedtak> query = entityManager.createQuery("from LagretVedtak where BEHANDLING_ID=:behandlingId", LagretVedtak.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        return hentEksaktResultat(query);
    }

    @Override
    public List<LagretVedtakMedBehandlingType> hentLagreteVedtakPåFagsak(long fagsakId) {
        Objects.requireNonNull(fagsakId, "fagsakId"); //NOSONAR

        String sql = "SELECT " +
            "l.BEHANDLING_ID id, " +
            "b.BEHANDLING_TYPE behandlingType, " +
            "l.opprettet_tid opprettetDato " +
            "FROM LAGRET_VEDTAK l " +
            "JOIN BEHANDLING b ON b.id = l.BEHANDLING_ID " +
            "WHERE l.FAGSAK_ID = :fagsakId";

        Query query = entityManager.createNativeQuery(sql, "LagretVedtakResult");
        query.setParameter("fagsakId", fagsakId);

        @SuppressWarnings("unchecked")
        List<LagretVedtakMedBehandlingType> resultater = query.getResultList();
        return resultater;
    }
}
