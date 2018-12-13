package no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;

import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;

/**
 * Støtte for enhetstester som trenger kodeverk/liste instanser med mer fullstendig tilstand enn konstantene
 * (de statiske instansene i hver konkrete Kodeliste), men uten å måtte lese fra databasen.
 */
public class KodeverkTestHelper {

    public static KodeverkRepository getKodeverkRepository() {
        return new MockKodeverkRepository();
    }

    private static class MockKodeverkRepository implements KodeverkRepository {

        private static final List<Class<?>> initialisert = new ArrayList<>(); // NOSONAR
        private static Map<Class<? extends Kodeliste>, Map<String, Kodeliste>> kodelistePrKodePrKlasse = new HashMap<>(); // NOSONAR
        private static Map<Class<? extends Kodeliste>, Map<String, Kodeliste>> kodelistePrOffisiellKodePrKlasse = new HashMap<>(); // NOSONAR
        private static Kodeliste navneOppslag = null;

        private KodeverkTabellRepository kodeverkTabellRepository;

        MockKodeverkRepository() {
        }

        private static synchronized void lazyLoadKodeliste(Class<? extends Kodeliste> cls) {

            if (initialisert.contains(cls)) {
                return;
            }

            List<? extends Kodeliste> kodelister = new KodeverkFraJson().lesKodeverkFraFil(cls);
            Map<String, Kodeliste> kodelistePrKode = new HashMap<>();
            Map<String, Kodeliste> kodelistePrOffisiellKode = new HashMap<>();
            kodelister.forEach(kodeliste -> {
                kodelistePrKode.put(kodeliste.getKode(), kodeliste);
                kodelistePrOffisiellKode.put(kodeliste.getOffisiellKode(), kodeliste);
            });
            kodelistePrKodePrKlasse.put(cls, kodelistePrKode);
            kodelistePrOffisiellKodePrKlasse.put(cls, kodelistePrOffisiellKode);
        }

        private KodeverkTabellRepository initMockKodeverkTabellRepository() {
            KodeverkTabellRepository mock = Mockito.mock(KodeverkTabellRepository.class);

            // returnerer alltid en verdi (mao, ingen filter for ugyldige verdier)
            Mockito.when(mock.finnBehandlingStegType(Mockito.anyString())).thenAnswer(invocation -> {
                String kode = invocation.getArgument(0);
                Constructor<BehandlingStegType> ctor = BehandlingStegType.class.getDeclaredConstructor(String.class);
                ctor.setAccessible(true);
                return ctor.newInstance(kode);
            });
            Mockito.when(mock.finnMedlemskapManuellVurderingType(Mockito.anyString())).thenAnswer(invocation -> {
                String kode = invocation.getArgument(0);
                Constructor<MedlemskapManuellVurderingType> ctor = MedlemskapManuellVurderingType.class
                    .getDeclaredConstructor(String.class);
                ctor.setAccessible(true);
                return ctor.newInstance(kode);
            });
            Mockito.when(mock.finnVenteårsak(Mockito.anyString())).thenAnswer(invocation -> {
                String kode = invocation.getArgument(0);
                Constructor<Venteårsak> ctor = Venteårsak.class.getDeclaredConstructor(String.class);
                ctor.setAccessible(true);
                return ctor.newInstance(kode);
            });
            Mockito.when(mock.finnVurderÅrsaker(Mockito.anyCollection())).thenAnswer(invocation -> {
                Collection<String> koder = invocation.getArgument(0);
                Constructor<VurderÅrsak> ctor = VurderÅrsak.class.getDeclaredConstructor(String.class);
                ctor.setAccessible(true);

                return koder.stream().map(k -> {
                    try {
                        return ctor.newInstance(k);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new IllegalArgumentException("Kan ikke opprette " + VurderÅrsak.class.getName() + " med kode " + k);
                    }
                }).collect(Collectors.toSet());
            });
            Mockito.when(mock.finnAksjonspunktDefinisjon(Mockito.anyString())).thenAnswer(invocation -> {
                String kode = invocation.getArgument(0);
                Constructor<AksjonspunktDefinisjon> ctor = AksjonspunktDefinisjon.class.getDeclaredConstructor(String.class);
                ctor.setAccessible(true);
                return ctor.newInstance(kode);
            });
            return mock;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V extends Kodeliste> V finn(Class<V> cls, String kode) {
            V kodeliste = null;
            Map<String, Kodeliste> kodelisterForKlasse = getKodelistePrKodePrKlasse(cls);
            if (kodelisterForKlasse != null) {
                kodeliste = (V) kodelisterForKlasse.get(kode);
            }
            if (kodeliste != null) {
                return kodeliste;
            } else {
                throw new NoResultException("ingen kode \"" + kode + "\" for " + cls.getName());
            }
        }

        @Override
        public <V extends Kodeliste> V finn(Class<V> cls, V kodelisteKonstant) {
            return finn(cls, kodelisteKonstant.getKode());
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode) {
            V kodeliste = null;
            Map<String, Kodeliste> kodelisterForKlasse = getKodelistePrOffisiellKodePrKlasse(cls);
            if (kodelisterForKlasse != null) {
                kodeliste = (V) kodelisterForKlasse.get(offisiellKode);
            }
            if (kodeliste != null) {
                return kodeliste;
            } else {
                throw new NoResultException("ingen offisiellKode \"" + offisiellKode + "\" for " + cls.getName());
            }
        }

        @Override
        public <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode, V defaultValue) {
            Objects.requireNonNull(defaultValue, "defaultValue kan ikke være null"); //$NON-NLS-1$
            V kodeliste;
            try {
                kodeliste = finnForKodeverkEiersKode(cls, offisiellKode);
            } catch (NoResultException e) {
                // Vi skal tåle ukjent offisiellKode
                kodeliste = finn(cls, defaultValue);
            }
            return kodeliste;
        }

        @Override
        public <V extends Kodeliste> List<V> finnForKodeverkEiersKoder(Class<V> cls, String... offisiellKoder) {
            Set<String> offisiellListe = new HashSet<>(Arrays.asList(offisiellKoder));
            Map<String, Kodeliste> kodelisterForKlasse = getKodelistePrOffisiellKodePrKlasse(cls);
            @SuppressWarnings("unchecked")
            List<V> resultList = kodelisterForKlasse.entrySet().stream().filter(e -> {
                String offisiellKode = e.getValue().getOffisiellKode();
                return offisiellListe.contains(offisiellKode);
            })
                .map(e -> (V) e.getValue())
                .collect(Collectors.toList());
            return resultList;
        }

        @Override
        public <V extends Kodeliste> List<V> finnListe(Class<V> cls, List<String> koder) {
            Map<String, Kodeliste> kodelisterForKlasse = getKodelistePrKodePrKlasse(cls);
            @SuppressWarnings("unchecked")
            List<V> resultList = kodelisterForKlasse.entrySet().stream().filter(e -> koder.contains(e.getKey()))
                .map(e -> (V) e.getValue())
                .collect(Collectors.toList());
            return resultList;
        }

        @Override
        public <V extends Kodeliste> List<V> hentAlle(Class<V> cls) {
            Map<String, Kodeliste> kodelisterForKlasse = getKodelistePrOffisiellKodePrKlasse(cls);
            @SuppressWarnings("unchecked")
            List<V> resultList = kodelisterForKlasse.entrySet().stream()
                .map(e -> (V) e.getValue())
                .collect(Collectors.toList());
            return resultList;
        }

        @Override
        public Map<String, List<Kodeliste>> hentAlle(List<String> cls) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String> hentLandkoderTilLandkodeISO2Map() {
            // impl ved behov
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String> hentLandkodeISO2TilLandkoderMap() {
            // impl ved behov
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V extends Kodeliste> V finnForKodeverkEiersNavn(Class<V> cls, String navn, V defaultValue) {
            // impl ved behov
            return navneOppslag != null ? (V) navneOppslag : defaultValue; // NOSONAR
        }

        @Override
        public boolean brukerErNordiskStatsborger(String landkode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean brukerErBorgerAvEøsLand(String landkode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean brukerErBorgerAvEuLand(String landkode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <V extends Kodeliste, K extends Kodeliste> Map<V, Set<K>> hentKodeRelasjonForKodeverk(Class<V> cls, Class<K> cls2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <V extends Kodeliste> Optional<V> finnOptional(Class<V> cls, String kode) {
            return Optional.of(finn(cls, kode));
        }

        @Override
        public KodeverkTabellRepository getKodeverkTabellRepository() {
            if (this.kodeverkTabellRepository == null) {
                this.kodeverkTabellRepository = initMockKodeverkTabellRepository();
            }
            return kodeverkTabellRepository;
        }

        public synchronized Map<String, Kodeliste> getKodelistePrOffisiellKodePrKlasse(Class<? extends Kodeliste> cls) {
            lazyLoadKodeliste(cls);
            return kodelistePrOffisiellKodePrKlasse.get(cls);
        }

        public synchronized Map<String, Kodeliste> getKodelistePrKodePrKlasse(Class<? extends Kodeliste> cls) {
            lazyLoadKodeliste(cls);
            return kodelistePrKodePrKlasse.get(cls);
        }
    }

}
