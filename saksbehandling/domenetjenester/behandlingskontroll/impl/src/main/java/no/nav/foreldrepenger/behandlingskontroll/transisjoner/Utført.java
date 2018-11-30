package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;

class Utført implements StegTransisjon {

    @Override
    public String getId() {
        return FellesTransisjoner.UTFØRT.getId();
    }

    @Override
    public BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg) {
        return nåværendeSteg.getBehandlingModell().finnNesteSteg(nåværendeSteg.getBehandlingStegType());
    }

    @Override
    public String toString() {
        return "Utført{}";
    }
}
