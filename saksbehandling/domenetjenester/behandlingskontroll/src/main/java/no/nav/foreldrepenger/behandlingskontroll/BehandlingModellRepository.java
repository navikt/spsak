package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTypeStegSekvens;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BehandlingModellRepository implements AutoCloseable, BehandlingslagerRepository {

    private EntityManager entityManager; // NOSONAR

    private final ConcurrentMap<Object, BehandlingModellImpl> cachedModell =  new ConcurrentHashMap<>();

    BehandlingModellRepository() {
        // for CDI proxy
    }

    @Inject
    public BehandlingModellRepository(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public KodeverkRepository getKodeverkRepository() {
        return new KodeverkRepositoryImpl(entityManager);
    }

    public BehandlingStegKonfigurasjon getBehandlingStegKonfigurasjon() {
        List<BehandlingStegStatus> list = getKodeverkRepository().hentAlle(BehandlingStegStatus.class);
        return new BehandlingStegKonfigurasjon(list);
    }

    /**
     * Finn modell for angitt behandling type.
     * <p>
     * Når modellen ikke lenger er i bruk må {@link BehandlingModellImpl#close()}
     * kalles slik at den ikke fortsetter å holde på referanser til objekter. (DETTE KAN DROPPES OM VI FÅR CACHET
     * MODELLENE!)
     */
    public BehandlingModellImpl getModell(BehandlingType behandlingType, FagsakYtelseType fagsakYtelseType) {
        Object key = cacheKey(behandlingType, fagsakYtelseType);
        cachedModell.computeIfAbsent(key, (kode) -> byggModell(behandlingType,fagsakYtelseType));
        return cachedModell.get(key);
    }

    private Object cacheKey(BehandlingType behandlingType, FagsakYtelseType fagsakYtelseType) {
        // lager en key av flere sammensatte elementer.
        return Arrays.asList(behandlingType, fagsakYtelseType);
    }

    protected BehandlingModellImpl byggModell(BehandlingType type, FagsakYtelseType fagsakYtelseType) {
        BehandlingModellImpl modell = nyModell(type, fagsakYtelseType);

        List<BehandlingTypeStegSekvens> stegSekvens = finnBehandlingStegSekvens(type, fagsakYtelseType);
        modell.leggTil(stegSekvens);

        return modell;
    }

    protected BehandlingModellImpl nyModell(BehandlingType type, FagsakYtelseType fagsakYtelseType) {
        return new BehandlingModellImpl(type, fagsakYtelseType, false);
    }

    private List<BehandlingTypeStegSekvens> finnBehandlingStegSekvens(BehandlingType type, FagsakYtelseType fagsakYtelseType) {
        String jpql = "from BehandlingTypeStegSekvens btss where btss.behandlingType.kode=:behandlingType and btss.fagsakYtelseType.kode=:fagsakYtelseType ORDER BY btss.sekvensNr ASC"; //$NON-NLS-1$
        TypedQuery<BehandlingTypeStegSekvens> query = entityManager.createQuery(jpql, BehandlingTypeStegSekvens.class);
        query.setParameter("behandlingType", type.getKode()); //$NON-NLS-1$
        query.setParameter("fagsakYtelseType", fagsakYtelseType.getKode()); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public void close() throws Exception {
        cachedModell.clear();
    }
}
