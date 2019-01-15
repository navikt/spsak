package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface OpptjeningDtoTjeneste {
    Optional<OpptjeningDto> mapFra(Behandling behandling);
}
