package no.nav.foreldrepenger.domene.feed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.jpa.QueryHints;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class FeedRepositoryImpl implements FeedRepository {

    private EntityManager entityManager;

    FeedRepositoryImpl() {
        // CDI
    }

    @Inject
    public FeedRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }


    @Override
    public<V extends UtgåendeHendelse> Long lagre(V utgåendeHendelse) {
        Objects.requireNonNull(utgåendeHendelse);
        if (utgåendeHendelse.getSekvensnummer() == 0) {
            utgåendeHendelse.setSekvensnummer(hentNesteSekvensnummer(utgåendeHendelse.getClass()));
        }
        entityManager.persist(utgåendeHendelse);
        entityManager.flush();
        return utgåendeHendelse.getId();

    }

    @Override
    public <V extends UtgåendeHendelse> boolean harHendelseMedKildeId(Class<V> cls, String kildeId) {
        Objects.requireNonNull(kildeId);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<V> criteria = entityManager.getCriteriaBuilder().createQuery(cls);
        Root<V> from = criteria.from(cls);
        criteria.select(criteria.from(cls));
        criteria.where(builder.and(
                builder.equal(from.get("kildeId"), kildeId))); // $NON-NLS-1$

        List<V> resultList = entityManager.createQuery(criteria)
                .setHint(QueryHints.HINT_READONLY, "true")
                .getResultList();

        return !resultList.isEmpty();
    }


    @Override
    public Optional<UtgåendeHendelse> hentUtgåendeHendelse(Long hendelseId) {
        return Optional.ofNullable(entityManager.find(UtgåendeHendelse.class, hendelseId));
    }

    @Override
    public <V extends UtgåendeHendelse> List<V> hentAlle(Class<V> cls) {
        CriteriaQuery<V> criteria = entityManager.getCriteriaBuilder().createQuery(cls);
        criteria.select(criteria.from(cls));
        return entityManager.createQuery(criteria)
            .setHint(QueryHints.HINT_READONLY, "true")
            .getResultList();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <V extends UtgåendeHendelse> List<V> hentUtgåendeHendelser(Class<V> cls, HendelseCriteria hendelseCriteria) {
        DiscriminatorValue discVal = cls.getDeclaredAnnotation(DiscriminatorValue.class);
        Objects.requireNonNull(discVal, "Mangler @DiscriminatorValue i klasse:" + cls); //$NON-NLS-1$
        String outputFeedKode  = discVal.value();

        List results = createScrollableResult(outputFeedKode, hendelseCriteria);
        List<V> hendelser = new ArrayList<>();
        for (Object object : results) {
            final Object[] resultObjects = (Object[]) object;

            if (resultObjects.length > 0) {
                Optional<UtgåendeHendelse> hendelse = hentUtgåendeHendelse(((BigDecimal) resultObjects[0]).longValue()); // NOSONAR
                hendelse.ifPresent(h -> hendelser .add((V) h));
            }
        }

        return hendelser;
    }


    private String createNativeSql(String type, String aktørId) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM (");
        sb.append(" SELECT uh.id, ROW_NUMBER() OVER (ORDER BY uh.SEKVENSNUMMER ASC) AS R");
        sb.append(" FROM UTGAAENDE_HENDELSE uh");
        sb.append(" WHERE uh.sekvensnummer > :sistLestSekvensnummer");
        sb.append(" AND output_feed_kode = :outputFeedKode");

        if(type != null) {
            sb.append(" AND uh.type = :type");
        }

        if(aktørId != null) {
            sb.append(" AND uh.aktoer_id = :aktørId");
        }

        sb.append(" ORDER BY uh.SEKVENSNUMMER ASC");

        sb.append(") WHERE R BETWEEN 1 AND :maxAntall");

        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
    private List createScrollableResult(String outputFeedKode, HendelseCriteria hendelseCriteria) {
        Query q = entityManager.createNativeQuery(createNativeSql(hendelseCriteria.getType(), hendelseCriteria.getAktørId()))
                .setParameter("outputFeedKode", outputFeedKode)
                .setParameter("maxAntall", hendelseCriteria.getMaxAntall())
                .setParameter("sistLestSekvensnummer", hendelseCriteria.getSisteLestSekvensId());

        if (hendelseCriteria.getType() != null) {
            q.setParameter("type", hendelseCriteria.getType());
        }
        if (hendelseCriteria.getAktørId() != null) {
            q.setParameter("aktørId", hendelseCriteria.getAktørId());
        }


        return q.getResultList();
    }

    public <V extends UtgåendeHendelse> long hentNesteSekvensnummer(Class<V> cls) {
        SekvensnummerNavn sekVal = cls.getDeclaredAnnotation(SekvensnummerNavn.class);
        Objects.requireNonNull(sekVal, "Mangler @SekvensnummerGeneratorNavn i klasse:" + cls); //$NON-NLS-1$
        String sql  = "select " + sekVal.value() + ".nextval as num from dual";

        Query query = entityManager.createNativeQuery(sql); //NOSONAR Her har vi full kontroll på sql
        BigDecimal singleResult = (BigDecimal) query.getSingleResult();
        return singleResult.longValue();
    }
}
