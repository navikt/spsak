package no.nav.foreldrepenger.behandlingslager.aktør;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;

public interface NavBrukerKodeverkRepository extends BehandlingslagerRepository {
    Optional<RelasjonsRolleType> finnBrukerRolle(String kode);

    NavBrukerKjønn finnBrukerKjønn(String kode);

    PersonstatusType finnPersonstatus(String kode);
}
