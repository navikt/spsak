package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto.SaldoerDto;

public interface SaldoerDtoTjeneste {

    SaldoerDto lagStønadskontoerDto(Behandling behandling);

}
