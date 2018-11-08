package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;

class Startet implements StegTransisjon {
    @Override
    public String getId() {
        return FellesTransisjoner.STARTET.getId();
    }

    @Override
    public BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg) {
        throw new IllegalArgumentException("Utvikler feil: skal ikke kalles for " + getId());
    }

    @Override
    public String toString() {
        return "Startet{}";
    }
}
