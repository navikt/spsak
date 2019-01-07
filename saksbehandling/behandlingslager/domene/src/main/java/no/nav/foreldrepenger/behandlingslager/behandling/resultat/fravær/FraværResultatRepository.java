package no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;

public interface FraværResultatRepository extends BehandlingslagerRepository {

    /**
     * Henter ut eksisterende resultatstruktur gitt behandlingen
     *
     * @param behandlingsresultat behandlingsresultatet
     * @return Optional.empty() hvis resultatet ikke eksisterer, Optional.of(resultat) hvis det eksisterer
     */
    Optional<FraværResultat> hentHvisEksistererFor(Behandlingsresultat behandlingsresultat);

    /**
     * Henter ut eksisterende resultatstruktur gitt behandlingen
     *
     * @param behandlingsresultat behandlingsresultatet
     * @return resultatet
     * @throws IllegalStateException om resultatstrukturen ikke er der som forventet
     */
    FraværResultat hentFor(Behandlingsresultat behandlingsresultat);

    /**
     * Lagrer resultatet knyttet til behandlingen. Deaktiverer eksisterende resultat hvis det er forskjell i resultatet.
     * Hvis ikke består det eksisterende (da nytt og gammelt er identisk).
     *
     * @param behandlingsresultat behandlingsresultatet
     * @param builder    nytt resultat
     */
    void lagre(Behandlingsresultat behandlingsresultat, FraværPerioderBuilder builder);

    /**
     * Oppretter builder basert på tilstanden slik den er per d.d.
     *
     * @param behandlingsresultatId iden til behandlingsresultatet
     * @return builder
     */
    FraværPerioderBuilder opprettBuilder(Long behandlingsresultatId);
}
