package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

class HenleggelseTransisjon implements StegTransisjon {

    @Override
    public String getId() {
        return FellesTransisjoner.HENLAGT.getId();
    }

    @Override
    public BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg) {
        Optional<BehandlingStegModell> funnetMålsteg = nåværendeSteg.getBehandlingModell().hvertStegEtter(nåværendeSteg.getBehandlingStegType())
            .filter(s -> s.getBehandlingStegType().equals(BehandlingStegType.IVERKSETT_VEDTAK))
            .findFirst();
        if (funnetMålsteg.isPresent()) {
            return funnetMålsteg.get();
        }
        throw new IllegalArgumentException("Finnes ikke noe steg av type " + BehandlingStegType.IVERKSETT_VEDTAK + " etter " + nåværendeSteg);
    }

    @Override
    public boolean erFremoverhopp() {
        return true;
    }

    @Override
    public String toString() {
        return "Henleggelse{}";
    }
}
