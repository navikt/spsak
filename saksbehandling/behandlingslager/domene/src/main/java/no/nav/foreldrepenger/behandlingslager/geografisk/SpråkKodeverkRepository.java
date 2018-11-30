package no.nav.foreldrepenger.behandlingslager.geografisk;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;

import java.util.Optional;

public interface SpråkKodeverkRepository extends BehandlingslagerRepository  {

    Optional<Språkkode> finnSpråkMedKodeverkEiersKode(String språkkode);
}
