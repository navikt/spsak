package no.nav.foreldrepenger.domene.uttak.uttaksplan;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface BeregnMorsMaksdatoTjeneste {

    Optional<LocalDate> beregnMorsMaksdato(Behandling behandling);

    Optional<LocalDate> beregnMaksdatoForeldrepenger(Behandling behandling);
}
