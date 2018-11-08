package no.nav.foreldrepenger.domene.uttak.saldo;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface StønadskontoSaldoTjeneste {

    Saldoer finnSaldoer(Behandling behandling);

}
