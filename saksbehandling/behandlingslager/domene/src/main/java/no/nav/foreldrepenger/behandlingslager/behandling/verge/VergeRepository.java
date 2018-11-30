package no.nav.foreldrepenger.behandlingslager.behandling.verge;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface VergeRepository extends BehandlingslagerRepository {

    Optional<VergeAggregat> hentAggregat(Behandling behandling);

    void lagreOgFlush(Behandling behandling, VergeBuilder vergeBuilder);

    Optional<VergeAggregat> hentAggregat(Long behandlingId);

}
