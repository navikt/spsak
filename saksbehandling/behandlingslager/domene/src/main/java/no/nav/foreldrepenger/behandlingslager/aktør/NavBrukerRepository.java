package no.nav.foreldrepenger.behandlingslager.aktør;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;

public interface NavBrukerRepository extends BehandlingslagerRepository {

    Optional<NavBruker> hent(AktørId aktorId);
    NavBruker opprett(NavBruker bruker);

}
