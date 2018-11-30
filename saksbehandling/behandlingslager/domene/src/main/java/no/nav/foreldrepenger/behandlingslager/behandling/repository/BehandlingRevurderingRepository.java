package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface BehandlingRevurderingRepository extends BehandlingslagerRepository {
    /**
     * Hent første henlagte endringssøknad etter siste innvilgede behandlinger for en fagsak
     */
    List<Behandling> finnHenlagteBehandlingerEtterSisteInnvilgedeIkkeHenlagteBehandling(Long fagsakId);

    Optional<Behandling> hentSisteYtelsesbehandling(Long fagsakId);

    Optional<Behandling> finnÅpenYtelsesbehandling(Long fagsakId);

    Optional<Behandling> finnKøetYtelsesbehandling(Long fagsakId);

    Optional<LocalDate> finnSøknadsdatoFraHenlagtBehandling(Behandling behandling);
}
