package no.nav.foreldrepenger.domene.beregning.ytelse;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface BeregnYtelseTjeneste {

    void overstyrTilkjentYtelseForEngangsstønad(Behandling behandling, Long beregnetTilkjentYtelse);

}
