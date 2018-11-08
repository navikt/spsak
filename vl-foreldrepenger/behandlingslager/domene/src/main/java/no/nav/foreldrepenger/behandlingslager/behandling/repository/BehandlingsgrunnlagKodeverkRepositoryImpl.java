package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodelisteRelasjon;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BehandlingsgrunnlagKodeverkRepositoryImpl implements BehandlingsgrunnlagKodeverkRepository {

    private EntityManager entityManager;

    private KodeverkRepository kodeverkRepository;

    BehandlingsgrunnlagKodeverkRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingsgrunnlagKodeverkRepositoryImpl(@VLPersistenceUnit EntityManager entityManager,
                                                     KodeverkRepository kodeverkRepository) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.kodeverkRepository = kodeverkRepository;
    }

    public BehandlingsgrunnlagKodeverkRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        if (entityManager != null) {
            this.kodeverkRepository = new KodeverkRepositoryImpl(entityManager);
        }
    }

    @Override
    public FarSøkerType finnFarSøkerType(String kode) {
        return kodeverkRepository.finn(FarSøkerType.class, kode);
    }

    @Override
    public SivilstandType finnSivilstandType(String kode) {
        return kodeverkRepository.finn(SivilstandType.class, kode);
    }

    @Override
    public Landkoder finnLandkode(String kode) {
        return kodeverkRepository.finn(Landkoder.class, kode);
    }

    @Override
    public List<PersonstatusType> personstatusTyperFortsattBehandling() {
        return kodeverkRepository.finnListe(PersonstatusType.class, Arrays.asList("DØD", "BOSA", "UTVA"));
    }

    @Override
    public Region finnHøyestRangertRegion(List<String> statsborgerskap) {
        Set<Region> regioner = new HashSet<>();
        for (String skap : statsborgerskap) {
            regioner.addAll(finnRegioner(skap));
        }
        return regioner.stream().min(Comparator.comparing(this::rangerRegion)).get();
    }

    @Override
    public Map<Landkoder, Region> finnRegionForStatsborgerskap(List<Landkoder> statsborgerskap) {
        final HashMap<Landkoder, Region> landRegion = new HashMap<>();
        for (Landkoder landkode : statsborgerskap) {
            landRegion.put(landkode, finnRegioner(landkode.getKode()).stream().min(Comparator.comparing(this::rangerRegion)).orElse(Region.TREDJELANDS_BORGER));
        }
        return landRegion;
    }

    // Det finnes ingen definert rangering for regioner. Men venter med å generalisere til det finnes use-caser som
    // krever en annen rangering enn nedenfor.
    private Integer rangerRegion(Region region) {
        if (region.equals(Region.NORDEN)) {
            return 1;
        }
        if (region.equals(Region.EOS)) {
            return 2;
        }
        return 3;
    }

    @Override
    public List<Region> finnRegioner(String kode) {
        TypedQuery<KodelisteRelasjon> query = entityManager.createQuery("from KodelisteRelasjon where kodeverk1 = 'REGION' AND kodeverk2 = 'LANDKODER' AND kode2=:kode", KodelisteRelasjon.class);
        query.setParameter("kode", kode);
        List<String> regionKoder = query.getResultList().stream()
            .map(KodelisteRelasjon::getKode1)
            .collect(toList());
        if (regionKoder.isEmpty()) {
            return Arrays.asList(Region.TREDJELANDS_BORGER);
        }

        return kodeverkRepository.finnListe(Region.class, regionKoder);
    }

}
