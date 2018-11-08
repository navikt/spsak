package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;

class SettPåVent implements StegTransisjon {

    @Override
    public String getId() {
        return FellesTransisjoner.SETT_PÅ_VENT.getId();
    }

    @Override
    public BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg) {
        return nåværendeSteg;
    }

    @Override
    public String toString() {
        return "SettPåVent{}";
    }
}
