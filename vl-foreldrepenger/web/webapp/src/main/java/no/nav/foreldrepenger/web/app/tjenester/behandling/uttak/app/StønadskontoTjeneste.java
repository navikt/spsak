package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.StønadskontoerDto;

import java.util.Optional;

public interface StønadskontoTjeneste {
    Optional<StønadskontoerDto> lagStønadskontoerDto(Behandling behandling);
}
