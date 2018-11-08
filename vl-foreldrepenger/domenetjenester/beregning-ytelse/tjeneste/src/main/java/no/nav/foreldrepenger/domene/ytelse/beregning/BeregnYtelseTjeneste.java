package no.nav.foreldrepenger.domene.ytelse.beregning;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface BeregnYtelseTjeneste {

    void overstyrTilkjentYtelseForEngangsstønad(Behandling behandling, Long beregnetTilkjentYtelse);

}
