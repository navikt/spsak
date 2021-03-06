package no.nav.foreldrepenger.behandlingslager.kodeverk;

import static java.util.stream.Collectors.joining;
import static no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkFeil.FEILFACTORY;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.hibernate.jpa.QueryHints;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class KodeverkRepositoryImpl implements KodeverkRepository {

    private EntityManager entityManager;
    private KodeverkTabellRepositoryImpl kodeverkTabellRepository;

    private Map<String, Class<?>> kodeverkImplClasses;

    KodeverkRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public KodeverkRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.kodeverkTabellRepository = new KodeverkTabellRepositoryImpl(entityManager);
    }

    @Override
    public KodeverkTabellRepository getKodeverkTabellRepository() {
        return kodeverkTabellRepository;
    }

    protected Map<String, Class<?>> getKodeverkImplClasses() {
        // leser alle kodeverk entity classes fra EntityManagerFactory
        if (kodeverkImplClasses == null) {
            synchronized (this) {
                Map<String, Class<?>> kodeverk = getKodeverkImplClassesFraEntityManagerFactory(this.entityManager.getEntityManagerFactory());
                kodeverkImplClasses = Map.copyOf(kodeverk);
            }
            
        }
        return kodeverkImplClasses;
    }

    protected Map<String, Class<?>> getKodeverkImplClassesFraEntityManagerFactory(EntityManagerFactory emf) {
        // cache Kodeverk entity classes per Kodeverk kode.
        Map<String, Class<?>> kodeverk = new TreeMap<>();
        for (EntityType<?> et : emf.getMetamodel().getEntities()) {
            Class<?> entityClass = et.getJavaType();
            if (Kodeliste.class.isAssignableFrom(entityClass)) {
                DiscriminatorValue desc = entityClass.getAnnotation(DiscriminatorValue.class);
                if (desc != null) {
                    // Kodeverk fra Kodeliste. Cache by DiscriminatorValue
                    kodeverk.putIfAbsent(desc.value(), entityClass);
                }
            } else if (KodeverkTabell.class.isAssignableFrom(entityClass)) {
                // kodeverk som ligger i egne tabeller.  Cache by tabellnavn
                Table desc = entityClass.getAnnotation(Table.class);
                kodeverk.put(desc.name(), entityClass);
            }
        }
        return kodeverk;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Class<Kodeliste>> finnKodelisteForKodeverk(String kodeverkKode) {
        Class<?> kodeverkClass = getKodeverkImplClasses().get(kodeverkKode);
        if(kodeverkClass!=null && Kodeliste.class.isAssignableFrom(kodeverkClass)) {
            return Optional.of((Class<Kodeliste>)kodeverkClass);
        } else {
            return Optional.empty();
        }
    }
    
    @Override
    public <V extends Kodeliste, K extends Kodeliste> Map<V, Set<K>> hentKodeRelasjonForKodeverk(Class<V> kodeliste1, Class<K> kodeliste2) {
        String kodeverk;
        try {
            Method getKodeverk = kodeliste1.getMethod("getKodeverk");
            kodeverk = ((String) getKodeverk.invoke(kodeliste1.getDeclaredConstructor().newInstance()));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Fant ikke kodeverk for kodeverk=" + kodeliste1, e);
        }
        List<KodelisteRelasjon> relasjoner = hentKodelisteRelasjonFor(kodeverk);
        Map<String, List<V>> vMap = hentAlle(kodeliste1).stream().collect(Collectors.groupingBy(Kodeliste::getKode));
        Map<String, List<K>> kMap = hentAlle(kodeliste2).stream().collect(Collectors.groupingBy(Kodeliste::getKode));

        Map<V, Set<K>> map = new HashMap<>();
        // støtter bare mapping en vei atm!!
        for (KodelisteRelasjon relasjon : relasjoner) {
            List<V> v = vMap.get(relasjon.getKode1());
            List<K> k = kMap.get(relasjon.getKode2());
            if (v != null && k != null) {
                final Set<K> values = map.getOrDefault(v.get(0), new HashSet<>(k));
                values.addAll(k);
                map.put(v.get(0), values);
            }
        }
        return map;
    }

    @Override
    public <V extends Kodeliste> Optional<V> finnOptional(Class<V> cls, String kode) {
        CriteriaQuery<V> criteria = createCriteria(cls, Collections.singletonList(kode));
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(Kodeliste.ENTITYGRAPH);
        List<V> list = entityManager.createQuery(criteria)
            .setHint(QueryHints.HINT_READONLY, "true")
            .setHint(QueryHints.HINT_FETCHGRAPH, entityGraph)
            .getResultList();
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0)); // NOSONAR
        }
    }

    @Override
    public <V extends Kodeliste> V finn(Class<V> cls, String kode) {
        return finnFraEM(cls, kode);
    }

    @Override
    public <V extends Kodeliste> V finn(Class<V> cls, V kodelisteKonstant) {
        return finn(cls, kodelisteKonstant.getKode());
    }

    @Override
    public <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode) {
        return finnForKodeverkEiersKodeFraEM(cls, offisiellKode);
    }

    @Override
    public <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode, V defaultValue) {
        Objects.requireNonNull(defaultValue, "defaultValue kan ikke være null"); //$NON-NLS-1$
        V kodeliste;
        try {
            kodeliste = finnForKodeverkEiersKode(cls, offisiellKode);
        } catch (TekniskException e) { // NOSONAR
            // Vi skal tåle ukjent offisiellKode
            kodeliste = finn(cls, defaultValue);
        }
        return kodeliste;
    }

    @Override
    public <V extends Kodeliste> List<V> finnForKodeverkEiersKoder(Class<V> cls, String... offisiellKoder) {
        List<String> koderIkkeICache = List.of(offisiellKoder);
        // Traverserer alle koder vi skal finne kodeverk for. Kodene som finnes i cache hentes derfra. De som ikke finnes i cache hentes fra DB.
        return finnForKodeverkEiersKoderFraEm(cls, koderIkkeICache);
    }

    @Override
    public <V extends Kodeliste> List<V> finnListe(Class<V> cls, List<String> koder) {
        return finnListeFraEm(cls, koder);
    }

    @Override
    public <V extends Kodeliste> List<V> hentAlle(Class<V> cls) {
        CriteriaQuery<V> criteria = entityManager.getCriteriaBuilder().createQuery(cls);
        criteria.select(criteria.from(cls));
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(Kodeliste.ENTITYGRAPH);
        return detachKodeverk(entityManager.createQuery(criteria)
            .setHint(QueryHints.HINT_READONLY, "true")
            .setHint(QueryHints.HINT_FETCHGRAPH, entityGraph)
            .getResultList());
    }

    @Override
    public Map<String, List<Kodeliste>> hentAlle(List<Class<? extends Kodeliste>> kodeverkClasses) {
        List<String> kodeverk = mapTilKodeverk(kodeverkClasses);
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(Kodeliste.ENTITYGRAPH);

        TypedQuery<Kodeliste> query = entityManager.createQuery("FROM Kodeliste kl " +
            "WHERE kl.kodeverk IN (:kodeverk) " +
            "AND kl.kode != '-'", Kodeliste.class);
        query.setParameter("kodeverk", kodeverk)
            .setHint(QueryHints.HINT_READONLY, "true")
            .setHint(QueryHints.HINT_FETCHGRAPH, entityGraph);

        return query
            .getResultStream()
            .peek(it -> entityManager.detach(it))
            .collect(Collectors.groupingBy(en -> en.getClass().getSimpleName()));
    }

    private List<String> mapTilKodeverk(List<Class<? extends Kodeliste>> kodeverkClasses) {
        return kodeverkClasses.stream().map(cl -> cl.getAnnotation(DiscriminatorValue.class).value()).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> hentLandkoderTilLandkodeISO2Map() {
        Query query = getLandkodeMappingQuery();

        int land3BokstavNr = 0;
        int land2BokstavNr = 1;

        HashMap<String, String> retval = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<Object[]> koder = query.getResultList();

        for (Object[] k : koder) {
            retval.put((String) k[land3BokstavNr], (String) k[land2BokstavNr]);
        }
        return retval;
    }

    @Override
    public Map<String, String> hentLandkodeISO2TilLandkoderMap() {
        Query query = getLandkodeMappingQuery();

        int land3BokstavNr = 0;
        int land2BokstavNr = 1;

        HashMap<String, String> retval = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<Object[]> koder = query.getResultList();
        for (Object[] k : koder) {
            retval.put((String) k[land2BokstavNr], (String) k[land3BokstavNr]);
        }
        return retval;
    }

    @Override
    public <V extends Kodeliste> V finnForKodeverkEiersNavn(Class<V> cls, String navn, V defaultValue) {
        String språk = Kodeliste.hentLoggedInBrukerSpråk();
        DiscriminatorValue discVal = cls.getDeclaredAnnotation(DiscriminatorValue.class);
        Objects.requireNonNull(discVal, "Mangler @DiscriminatorValue i klasse:" + cls); //$NON-NLS-1$
        String kodeverk = discVal.value();

        Query query = entityManager.createNativeQuery(
            "SELECT kl_kode " +
                "FROM kodeliste_navn_i18n " +
                "WHERE kl_kodeverk = ? " +
                "AND navn = ? " +
                "AND sprak = ?");
        query.setParameter(1, kodeverk);
        query.setParameter(2, navn);
        query.setParameter(3, språk);

        @SuppressWarnings("unchecked")
        List<String> rowList = query.getResultList();

        for (String row : rowList) {
            return finn(cls, row);
        }
        return defaultValue;
    }

    private <V extends Kodeliste> List<V> finnListeFraEm(Class<V> cls, List<String> koder) {
        CriteriaQuery<V> criteria = createCriteria(cls, koder);
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(Kodeliste.ENTITYGRAPH);
        List<V> result = entityManager.createQuery(criteria)
            .setHint(QueryHints.HINT_READONLY, "true")
            .setHint(QueryHints.HINT_FETCHGRAPH, entityGraph)
            .getResultList();
        return detachKodeverk(result);
    }

    private <V extends Kodeliste> List<V> finnForKodeverkEiersKoderFraEm(Class<V> cls, List<String> offisiellKoder) {
        try {
            CriteriaQuery<V> criteria = createCriteria(cls, "offisiellKode", offisiellKoder);
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(Kodeliste.ENTITYGRAPH);
            List<V> list = entityManager.createQuery(criteria)
                .setHint(QueryHints.HINT_READONLY, "true")
                .setHint(QueryHints.HINT_FETCHGRAPH, entityGraph)
                .getResultList();
            return detachKodeverk(list);
        } catch (NoResultException e) {
            String koder = offisiellKoder.stream().collect(joining(","));
            throw FEILFACTORY.kanIkkeFinneKodeverkForKoder(cls.getSimpleName(), koder, e).toException();
        }
    }

    private <V extends Kodeliste> V finnFraEM(Class<V> cls, String kode) {
        CriteriaQuery<V> criteria = createCriteria(cls, Collections.singletonList(kode));
        try {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(Kodeliste.ENTITYGRAPH);
            V result = entityManager.createQuery(criteria)
                .setHint(QueryHints.HINT_READONLY, "true")
                .setHint(QueryHints.HINT_FETCHGRAPH, entityGraph)
                .getSingleResult();
            return detachKodeverk(result);
        } catch (NoResultException e) {
            throw FEILFACTORY.kanIkkeFinneKodeverk(cls.getSimpleName(), kode, e).toException();
        }
    }

    private <V extends Kodeliste> V finnForKodeverkEiersKodeFraEM(Class<V> cls, String offisiellKode) {
        CriteriaQuery<V> criteria = createCriteria(cls, "offisiellKode", Collections.singletonList(offisiellKode));
        try {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(Kodeliste.ENTITYGRAPH);
            V result = entityManager.createQuery(criteria)
                .setHint(QueryHints.HINT_READONLY, "true")
                .setHint(QueryHints.HINT_FETCHGRAPH, entityGraph)
                .getSingleResult();
            return detachKodeverk(result);
        } catch (NoResultException e) {
            throw FEILFACTORY.kanIkkeFinneKodeverkForOffisiellKode(cls.getSimpleName(), offisiellKode, e).toException();
        }
    }

    protected <V extends Kodeliste> CriteriaQuery<V> createCriteria(Class<V> cls, List<String> koder) {
        return createCriteria(cls, "kode", koder);
    }

    protected <V extends Kodeliste> CriteriaQuery<V> createCriteria(Class<V> cls, String felt, List<String> koder) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<V> criteria = builder.createQuery(cls);

        DiscriminatorValue discVal = cls.getDeclaredAnnotation(DiscriminatorValue.class);
        Objects.requireNonNull(discVal, "Mangler @DiscriminatorValue i klasse:" + cls); //$NON-NLS-1$
        String kodeverk = discVal.value();
        Root<V> from = criteria.from(cls);
        criteria.where(builder.and(
            builder.equal(from.get("kodeverk"), kodeverk), //$NON-NLS-1$
            from.get(felt).in(koder))); // $NON-NLS-1$
        return criteria;
    }

    private Query getLandkodeMappingQuery() {
        String språk = Kodeliste.hentLoggedInBrukerSpråk();
        // FIXME (maur) Bør trolig gjøres om til ql og kan trolig lages litt enklere.
        return entityManager.createNativeQuery(
            "SELECT k3.kl_kode AS land_3bokstav, " +
                "(SELECT k2.kl_kode FROM kodeliste_navn_i18n k2 " +
                "WHERE k2.kl_kodeverk = 'LANDKODE_ISO2' " +
                "AND k2.sprak = :sprakParam1 " +
                "AND k2.navn = k3.navn ) AS land_2bokstav " +
                "FROM kodeliste_navn_i18n k3 " +
                "WHERE k3.kl_kodeverk = 'LANDKODER' " +
                "AND k3.sprak = :sprakParam2 " +
                "AND EXISTS (SELECT k2.kl_kode " +
                "FROM kodeliste_navn_i18n k2 " +
                "WHERE k2.kl_kodeverk = 'LANDKODE_ISO2' " +
                "AND k2.navn = k3.navn)")
            .setParameter("sprakParam1", språk)
            .setParameter("sprakParam2", språk);
    }

    @Override
    public boolean brukerErNordiskStatsborger(String landkode) {
        return brukerErBorgerAvGeopolitiskRegion("NORDEN", landkode);
    }

    @Override
    public boolean brukerErBorgerAvEøsLand(String landkode) {
        return brukerErBorgerAvGeopolitiskRegion("EØS", landkode);
    }

    @Override
    public boolean brukerErBorgerAvEuLand(String landkode) {
        return brukerErBorgerAvGeopolitiskRegion("EU", landkode);
    }

    private boolean brukerErBorgerAvGeopolitiskRegion(String region, String landkode) {
        Query query = entityManager.createNativeQuery(
            "SELECT 1 FROM KODELISTE_RELASJON " +
                "WHERE kodeverk1='GEOPOLITISK' AND kode1=? " +
                "AND kodeverk2=? AND kode2=?");
        query.setParameter(1, region);
        query.setParameter(2, landkode.length() == 3 ? "LANDKODER" : "LANDKODE_ISO2");
        query.setParameter(3, landkode);
        return !query.getResultList().isEmpty();
    }

    private List<KodelisteRelasjon> hentKodelisteRelasjonFor(String kodeverk) {
        Query query = entityManager.createNativeQuery(
            "SELECT kodeverk1, kode1, kodeverk2, kode2, gyldig_fom, gyldig_tom " +
                "FROM kodeliste_relasjon " +
                "WHERE gyldig_fom <= now() AND gyldig_tom > now() " +
                "AND KODEVERK1 = ?");

        query.setParameter(1, kodeverk);

        int kodeverk1Nr = 0;
        int kode1Nr = 1;
        int kodeverk2Nr = 2;
        int kode2Nr = 3;
        int fomNr = 4;
        int tomNr = 5;

        List<KodelisteRelasjon> retval = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> koderelasjoner = query.getResultList();

        for (Object[] kr : koderelasjoner) {
            retval.add(new KodelisteRelasjon((String) kr[kodeverk1Nr], (String) kr[kode1Nr],
                (String) kr[kodeverk2Nr], (String) kr[kode2Nr],
                ((Date) kr[fomNr]).toLocalDate(),
                ((Date) kr[tomNr]).toLocalDate()));
        }
        return retval;
    }

    private <V extends Kodeliste> V detachKodeverk(V result) {
        entityManager.detach(result);
        return result;
    }

    private <V extends Kodeliste> List<V> detachKodeverk(List<V> result) {
        for (V res : result) {
            detachKodeverk(res);
        }
        return result;
    }
}
