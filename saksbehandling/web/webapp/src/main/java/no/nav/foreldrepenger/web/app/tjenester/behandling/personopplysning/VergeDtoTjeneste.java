package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeAggregat;

public interface VergeDtoTjeneste {
    Optional<VergeDto> lagVergeDto(Optional<VergeAggregat> vergeAggregat);
}
