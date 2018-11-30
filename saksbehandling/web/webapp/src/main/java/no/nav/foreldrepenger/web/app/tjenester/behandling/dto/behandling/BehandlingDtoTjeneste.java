package no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;

public interface BehandlingDtoTjeneste {

    List<BehandlingDto> lagBehandlingDtoer(List<Behandling> behandlinger);

    UtvidetBehandlingDto lagUtvidetBehandlingDto(Behandling behandling, AsyncPollingStatus asyncPollingStatus);

}