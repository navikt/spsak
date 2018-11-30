package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.util.Tuple;

@ApplicationScoped
public class VilkårKodeverkRepositoryImpl implements VilkårKodeverkRepository {

    private EntityManager entityManager;
    private KodeverkRepository kodeverkRepository;

    VilkårKodeverkRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public VilkårKodeverkRepositoryImpl(@VLPersistenceUnit EntityManager entityManager, KodeverkRepository kodeverkRepository) {
        this.entityManager = entityManager;
        this.kodeverkRepository = kodeverkRepository;
    }

    public VilkårKodeverkRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        if (entityManager != null) {
            this.kodeverkRepository = new KodeverkRepositoryImpl(entityManager);
        }
    }

    @Override
    public VilkårResultatType finnVilkårResultatType(String kode) {
        return kodeverkRepository.finn(VilkårResultatType.class, kode);
    }

    @Override
    public VilkårType finnVilkårType(String kode) {
        return kodeverkRepository.finn(VilkårType.class, kode);
    }

    @Override
    public VilkårUtfallType finnVilkårUtfallType(String kode) {
        return kodeverkRepository.finn(VilkårUtfallType.class, kode);
    }

    @Override
    public AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode) {
        TypedQuery<AksjonspunktDefinisjon> query = entityManager.createQuery("from AksjonspunktDef where kode=:kode",
                AksjonspunktDefinisjon.class);
        query.setParameter("kode", kode);
        return hentEksaktResultat(query);
    }

    @Override
    public VilkårUtfallMerknad finnVilkårUtfallMerknad(String kode) {
        return kodeverkRepository.finn(VilkårUtfallMerknad.class, kode);
    }

    @Override
    public List<Avslagsårsak> finnAvslagÅrsakListe(String vilkårType) {
        // Vi skal støtte at relasjon mellom kode1 og kode2 kan settes inn i begge rekkefølger. Dvs at både avslagsårsak
        // og vilkårtype kan være kode1(eller kode2).
        final Query query = entityManager.createNativeQuery(
                "SELECT k.kode2 FROM KODELISTE_RELASJON k WHERE k.kode1 =:vilkar_type UNION SELECT k.kode1 FROM KODELISTE_RELASJON k WHERE k.kode2 =:vilkar_type");
        query.setParameter("vilkar_type", vilkårType);
        @SuppressWarnings("unchecked")
        List<String> koder = query.getResultList();
        List<String> unikeKoder = koder.stream().distinct().collect(Collectors.toList());
        return kodeverkRepository.finnListe(Avslagsårsak.class, unikeKoder);
    }

    @Override
    public Map<VilkårType, List<Avslagsårsak>> finnAvslagårsakerGruppertPåVilkårType() {
        // Vi skal støtte at relasjon mellom kode1 og kode2 kan settes inn i begge rekkefølger. Dvs at både avslagårsak
        // og vilkårtype kan være både kode1 og kode2.
        final Query query = entityManager.createNativeQuery(
                "SELECT k.kode1 AS vilkarTypeKode, k.kode2 AS avslagsarsakKode FROM KODELISTE_RELASJON k WHERE k.kodeverk2 =:avslagsarsak AND k.kodeverk1=:vilkar_type "
                        +
                        "UNION SELECT k.kode2 AS vilkarTypeKode, k.kode1 AS avslagsarsakKode FROM KODELISTE_RELASJON k WHERE k.kodeverk1 =:avslagsarsak AND k.kodeverk2=:vilkar_type");
        query.setParameter("avslagsarsak", "AVSLAGSARSAK");
        query.setParameter("vilkar_type", "VILKAR_TYPE");
        @SuppressWarnings("unchecked")
        List<Object[]> resultatListe = query.getResultList();

        Map<VilkårType, List<String>> avslagsårsakKoderGruppertPåVilkårType = resultatListe.stream()
                .map(resultat -> new Tuple<VilkårType, String>(new VilkårType((String) resultat[0]), (String) resultat[1])) // NOSONAR
                .distinct()
                .collect(Collectors.groupingBy(Tuple::getElement1, Collectors.mapping(
                        Tuple::getElement2, Collectors.toList())));

        Map<VilkårType, List<Avslagsårsak>> avslagsårsakerGruppertPåVilkårType = new HashMap<>();
        for (Entry<VilkårType, List<String>> entry : avslagsårsakKoderGruppertPåVilkårType.entrySet()) {
            avslagsårsakerGruppertPåVilkårType.put(entry.getKey(), kodeverkRepository.finnListe(Avslagsårsak.class, entry.getValue()));
        }
        return avslagsårsakerGruppertPåVilkårType;
    }

    @Override
    public List<VilkårType> finnVilkårTypeListe(String avslagsårsakKode) {
        // Vi skal støtte at relasjon mellom kode1 og kode2 kan settes inn i begge rekkefølger. Dvs at både avslagårsak
        // og vilkårtype kan være både kode1 og kode2.
        final Query query = entityManager
                .createNativeQuery("SELECT k.kode1 FROM KODELISTE_RELASJON k WHERE k.kode2 =:avslagsarsak AND k.kodeverk1=:vilkar_type " +
                        "UNION SELECT k.kode2 FROM KODELISTE_RELASJON k WHERE k.kode1 =:avslagsarsak  AND k.kodeverk2=:vilkar_type");
        query.setParameter("avslagsarsak", avslagsårsakKode);
        query.setParameter("vilkar_type", "VILKAR_TYPE");
        @SuppressWarnings("unchecked")
        List<String> koder = query.getResultList();
        List<String> unikeKoder = koder.stream().distinct().collect(Collectors.toList());
        return kodeverkRepository.finnListe(VilkårType.class, unikeKoder);
    }

    @Override
    public Avslagsårsak finnAvslagÅrsak(String kode) {
        return kodeverkRepository.finn(Avslagsårsak.class, kode);
    }

    @Override
    public Optional<Avslagsårsak> finnEnesteAvslagÅrsak(String vilkårType) {
        List<Avslagsårsak> resultat = finnAvslagÅrsakListe(vilkårType);
        if (resultat.size() == 1) {
            return Optional.of(resultat.get(0));
        }
        return Optional.empty();
    }

    @Override
    public KodeverkRepository getKodeverkRepository() {
        return kodeverkRepository;
    }
}
