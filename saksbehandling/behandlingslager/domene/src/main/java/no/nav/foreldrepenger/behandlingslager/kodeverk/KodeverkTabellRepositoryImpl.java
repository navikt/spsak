package no.nav.foreldrepenger.behandlingslager.kodeverk;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class KodeverkTabellRepositoryImpl implements KodeverkTabellRepository {

    private EntityManager entityManager;

    KodeverkTabellRepositoryImpl() {
        //CDI
    }

    @Inject
    public KodeverkTabellRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public BehandlingStegType finnBehandlingStegType(String kode) {
        TypedQuery<BehandlingStegType> query = entityManager.createQuery("from BehandlingStegType where kode=:kode", BehandlingStegType.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }

    @Override
    public Venteårsak finnVenteårsak(String kode) {
        TypedQuery<Venteårsak> query = entityManager.createQuery("from Venteårsak where kode=:kode", Venteårsak.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }

    @Override
    public MedlemskapManuellVurderingType finnMedlemskapManuellVurderingType(String kode) {
        TypedQuery<MedlemskapManuellVurderingType> query = entityManager.createQuery("from MedlemskapManuellVurderingType where kode=:kode", MedlemskapManuellVurderingType.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }

    @Override
    public Set<VurderÅrsak> finnVurderÅrsaker(Collection<String> koder) {
        if (koder.isEmpty()){
            return Collections.emptySet();
        }
        TypedQuery<VurderÅrsak> query = entityManager.createQuery("from VurderÅrsak where kode in (:kode)", VurderÅrsak.class);
        query.setParameter("kode", koder);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return new HashSet<>(query.getResultList());
    }

    @Override
    public StartpunktType finnStartpunktType(String kode) {
        TypedQuery<StartpunktType> query = entityManager.createQuery("from StartpunktType where kode=:kode", StartpunktType.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }

    @Override
    public AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode) {
        TypedQuery<AksjonspunktDefinisjon> query = entityManager.createQuery("from AksjonspunktDef where kode=:kode", AksjonspunktDefinisjon.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }
}
