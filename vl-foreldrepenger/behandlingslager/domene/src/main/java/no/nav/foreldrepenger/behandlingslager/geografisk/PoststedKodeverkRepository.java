package no.nav.foreldrepenger.behandlingslager.geografisk;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;

public interface PoststedKodeverkRepository extends BehandlingslagerRepository {

    Optional<Poststed> finnPoststed(String postnummer);

    Optional<AdresseType> finnAdresseType(String kode);

}
