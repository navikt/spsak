package no.nav.foreldrepenger.web.app.tjenester.behandling.ytelsefordeling;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

import java.util.Optional;

public interface YtelseFordelingDtoTjeneste {
    Optional<YtelseFordelingDto> mapFra(Behandling behandling);
}
