package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;

public interface FaktaUttakHistorikkTjeneste {

    /**
    * Historikkinnslag for overstyringpunkt som saksbehandler opprettet for kontroll av uttaksperioder med revurdering
    */
    void byggHistorikkinnslagForManuellAvklarFakta(ManuellAvklarFaktaUttakDto manuellAvklarFaktaUttakDto, Behandling behandling);

    /**
     * Historikkinnslag for avklar fakta uttak
     */
    void byggHistorikkinnslagForAvklarFakta(AvklarFaktaUttakDto avklarFaktaUttakDto, Behandling behandling);
}
