package no.nav.foreldrepenger.behandlingskontroll;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;

public class AksjonspunktTilbakeførtEvent extends AksjonspunktEvent {

    public AksjonspunktTilbakeførtEvent(BehandlingskontrollKontekst kontekst, List<Aksjonspunkt> aksjonspunkter,
            BehandlingStegType behandlingStegType) {
        super(kontekst, aksjonspunkter, behandlingStegType);
    }
}
