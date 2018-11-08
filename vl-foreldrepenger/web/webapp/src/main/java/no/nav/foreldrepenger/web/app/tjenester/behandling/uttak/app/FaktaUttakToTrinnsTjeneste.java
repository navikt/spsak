package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.ManuellAvklarFaktaUttakDto;

public interface FaktaUttakToTrinnsTjeneste {

    void oppdaterTotrinnskontrollVedEndringerFaktaUttak(AvklarFaktaUttakDto dto, Behandling behandling);

}
