package no.nav.foreldrepenger.behandlingskontroll;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

public class BehandlingStegUtfall {
    private final BehandlingStegType behandlingStegType;
    private final BehandlingStegStatus resultat;

    public BehandlingStegUtfall(BehandlingStegType behandlingStegType, BehandlingStegStatus resultat) {
        this.behandlingStegType = behandlingStegType;
        this.resultat = resultat;
    }

    public BehandlingStegType getBehandlingStegType() {
        return behandlingStegType;
    }

    public BehandlingStegStatus getResultat() {
        return resultat;
    }
}
