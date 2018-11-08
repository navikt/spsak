package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

class FremoverhoppTransisjon implements StegTransisjon {

    private final String id;
    private final BehandlingStegType målsteg;

    public FremoverhoppTransisjon(String id, BehandlingStegType målsteg) {
        this.id = id;
        this.målsteg = målsteg;
    }

    @Override
    public BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg) {
        Optional<BehandlingStegModell> funnetMålsteg = nåværendeSteg.getBehandlingModell().hvertStegEtter(nåværendeSteg.getBehandlingStegType())
            .filter(s -> s.getBehandlingStegType().equals(målsteg))
            .findFirst();
        if (funnetMålsteg.isPresent()) {
            return funnetMålsteg.get();
        }
        throw new IllegalArgumentException("Finnes ikke noe steg av type " + målsteg + " etter " + nåværendeSteg);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean erFremoverhopp() {
        return true;
    }

    public BehandlingStegType getMålsteg() {
        return målsteg;
    }

    @Override
    public String toString() {
        return "FremoverhoppTransisjon{" +
            "id='" + id + '\'' +
            '}';
    }
}
