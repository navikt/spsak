package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.GjenopptaBehandlingDto;

public class GjenopptaBehandlingDtoBuilder {

    public static GjenopptaBehandlingDto build(Behandling behandling) {
        GjenopptaBehandlingDto gjenopptaBehandlingDto = new GjenopptaBehandlingDto();
        gjenopptaBehandlingDto.setBehandlingId(behandling.getId());
        gjenopptaBehandlingDto.setBehandlingVersjon(behandling.getVersjon());
        return gjenopptaBehandlingDto;
    }
}
