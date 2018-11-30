package no.nav.foreldrepenger.behandlingslager.aktør;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;

public interface NavBrukerKodeverkRepository extends BehandlingslagerRepository {
    NavBrukerKjønn finnBrukerKjønn(String kode);

    PersonstatusType finnPersonstatus(String kode);
}
