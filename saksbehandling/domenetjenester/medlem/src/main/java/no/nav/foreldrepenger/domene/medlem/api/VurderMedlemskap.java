package no.nav.foreldrepenger.domene.medlem.api;

import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public class VurderMedlemskap {

    private final Set<AksjonspunktDefinisjon> aksjonspunkter;
    private final Set<VurderingsÅrsak> årsaker;

    public VurderMedlemskap(Set<AksjonspunktDefinisjon> aksjonspunkter, Set<VurderingsÅrsak> årsaker) {
        this.aksjonspunkter = aksjonspunkter;
        this.årsaker = årsaker;
    }

    public Set<AksjonspunktDefinisjon> getAksjonspunkter() {
        return aksjonspunkter;
    }

    public Set<VurderingsÅrsak> getÅrsaker() {
        return årsaker;
    }
}
