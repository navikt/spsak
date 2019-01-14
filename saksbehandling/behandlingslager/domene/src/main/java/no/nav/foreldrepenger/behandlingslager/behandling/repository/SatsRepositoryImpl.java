package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;

import java.time.LocalDate;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.SatsType;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class SatsRepositoryImpl implements SatsRepository {

    private EntityManager entityManager;

    SatsRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public SatsRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Sats finnEksaktSats(SatsType type, LocalDate dato) {
        TypedQuery<Sats> query = entityManager.createQuery("from Sats where satsType=:satsType" + //$NON-NLS-1$
            " and periode.fomDato<=:dato" + //$NON-NLS-1$
            " and periode.tomDato>=:dato", Sats.class); //$NON-NLS-1$

        query.setParameter("satsType", type); //$NON-NLS-1$
        query.setParameter("dato", dato); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        query.getResultList();
        return hentEksaktResultat(query);
    }
}
