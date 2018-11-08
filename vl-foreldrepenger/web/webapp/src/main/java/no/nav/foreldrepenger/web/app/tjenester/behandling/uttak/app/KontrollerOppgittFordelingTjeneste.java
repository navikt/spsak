package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;

public interface KontrollerOppgittFordelingTjeneste {

    /**
     * Brukes i bekreft aksjonspunkt avklar fakta uttak
     */
    void avklarFaktaUttaksperiode(AvklarFaktaUttakDto dto, Behandling behandling);

    /**
     * Brukes i overstyringspunkt som saksbehandler opprettet for kontroll av uttaksperioder med revurdering
     */
    void manuellAvklarFaktaUttaksperiode(ManuellAvklarFaktaUttakDto dto, Behandling behandling);
}
