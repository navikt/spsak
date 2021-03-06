package no.nav.foreldrepenger.behandlingslager.kodeverk;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;

/**
 * Få tilgang til kodeverk.
 */
public interface KodeverkRepository extends BehandlingslagerRepository {

    /**
     * Finn instans av Kodeliste innslag for angitt kode verdi.
     */
    <V extends Kodeliste> V finn(Class<V> cls, String kode);

    /**
     * Finn instans av Kodeliste innslag for angitt Kodeliste fra databasen.
     * For oppslag av fulle instanser fra de ufullstendige i hver konkrete subklasse av Kodeliste.
     */
    <V extends Kodeliste> V finn(Class<V> cls, V kodelisteKonstant);

    /**
     * Finn instans av Kodeliste innslag for angitt offisiell kode verdi.
     */
    <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode);

    /**
     * Finn instans av Kodeliste innslag for angitt offisiell kode verdi, eller en default value hvis offisiell kode ikke git treff.
     */
    <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode, V defaultValue);

    /**
     * Finn instans av Kodeliste innslag for angitt offisiell kode verdi.
     */
    <V extends Kodeliste> List<V> finnForKodeverkEiersKoder(Class<V> cls, String... offisiellKoder);

    /**
     * Hent alle innslag for en gitt kodeliste og gitte koder.
     */
    <V extends Kodeliste> List<V> finnListe(Class<V> cls, List<String> koder);

    /**
     * Hent alle innslag for en gitt Kodeliste.
     */
    <V extends Kodeliste> List<V> hentAlle(Class<V> cls);

    Map<String, List<Kodeliste>> hentAlle(List<Class<? extends Kodeliste>> cls);

    Map<String, String> hentLandkoderTilLandkodeISO2Map();

    Map<String, String> hentLandkodeISO2TilLandkoderMap();

    boolean brukerErNordiskStatsborger(String landkode);

    boolean brukerErBorgerAvEøsLand(String landkode);

    boolean brukerErBorgerAvEuLand(String landkode);

    /**
     * Henter et map med kobling mellom kodeverk, NB! benytter refleksjon for å utlede DISCRIMINATOR til hvilket kodeverk den skal slå opp.
     * Det må ligge innslag i KODELISTE_RELASJON for å få treff, støtter bare mapping fra kodeliste1 til kodeliste2.
     */
    <V extends Kodeliste, K extends Kodeliste> Map<V, Set<K>> hentKodeRelasjonForKodeverk(Class<V> kodeliste1, Class<K> kodeliste2);

    /**
     * Finn kode, return er optional empty hvis ikke finnes.
     */
    <V extends Kodeliste> Optional<V> finnOptional(Class<V> cls, String kode);

    /**
     * Kodeverk med separate tabeller.
     */
    KodeverkTabellRepository getKodeverkTabellRepository();

    /**
     * Finn instans av Kodeliste innslag for angitt offisielt navn for koden, eller en default value hvis offisielt navn ikke gir treff.
     */
    <V extends Kodeliste> V finnForKodeverkEiersNavn(Class<V> cls, String navn, V defaultValue);

    /** Finner kodeverk klasse for angitt kodeverk kode. */
    Optional<Class<Kodeliste>> finnKodelisteForKodeverk(String kodeverkKode);

}
