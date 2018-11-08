package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface FamiliehendelseDataDtoTjeneste {

    Optional<FamiliehendelseDto> mapFra(Behandling behandling);

}
