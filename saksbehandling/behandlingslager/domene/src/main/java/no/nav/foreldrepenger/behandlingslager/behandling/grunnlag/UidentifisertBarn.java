package no.nav.foreldrepenger.behandlingslager.behandling.grunnlag;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Barn som ikke har en kjent id, men som kun identifiseres ved fødselsdato og et løpenummer i en sak.
 * (eks. før fødsel, eller før registrert i Folkeregisteret/AktørId).
 */
public interface UidentifisertBarn {

    LocalDate getFødselsdato();

    Optional<LocalDate> getDødsdato();

    Integer getBarnNummer();

}
