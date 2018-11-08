package no.nav.foreldrepenger.domene.uttak.beregnkontoer;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;

public interface BeregnStønadskontoerTjeneste {

    /**
     * Beregner og lagrer stønadskontoer gitt en behandling.
     *
     * @param behandling behandling som det skal opprettes stønadskontoer utifra.
     *
     * @return sett av stønadskontoer som ble lagret.
     */
    Stønadskontoberegning beregnStønadskontoer(Behandling behandling);

}
