package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPerioderDto;

public interface UttakPerioderDtoTjeneste {
    Optional<UttakResultatPerioderDto> mapFra(Behandling behandling);
}
