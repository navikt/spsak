package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

interface SøknadDtoTjeneste {

    Optional<SoknadDto> mapFra(Behandling behandling);
}
