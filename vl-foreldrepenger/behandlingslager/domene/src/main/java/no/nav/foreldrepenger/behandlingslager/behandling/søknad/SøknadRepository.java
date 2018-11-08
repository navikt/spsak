package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface SøknadRepository extends BehandlingslagerRepository {

    /**
     * Gir søknaden som var årsaken til behandlingen
     *
     * Hvis det ikke eksisterer noen søknad på behandlingen prøver den hente ut den søknaden som henger på
     * den orginale behandlingen.
     *
     * @param behandling behandlingen
     * @return søknaden
     */
    Søknad hentSøknad(Behandling behandling);

    /**
     * Gir søknaden som var årsaken til behandlingen
     *
     * Hvis det ikke eksisterer noen søknad på behandlingen prøver den hente ut den søknaden som henger på
     * den orginale behandlingen.
     *
     * @param behandlingId iden til behandlingen
     * @return søknaden
     */
    Søknad hentSøknad(Long behandlingId);

    Optional<Søknad> hentSøknadHvisEksisterer(Behandling behandling);

    Optional<Søknad> hentSøknadHvisEksisterer(Long behandlingId);

    Søknad hentFørstegangsSøknad(Behandling behandling);

    Søknad hentFørstegangsSøknad(Long behandlingId);

    void lagreOgFlush(Behandling behandling, Søknad søknad);
}
