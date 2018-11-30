package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;

public interface StegTransisjon {
    String getId();

    BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg);

    default boolean erFremoverhopp() {
        return false;
    }
}
