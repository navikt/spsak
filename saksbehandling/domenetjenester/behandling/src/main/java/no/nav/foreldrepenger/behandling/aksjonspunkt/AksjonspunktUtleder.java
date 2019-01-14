package no.nav.foreldrepenger.behandling.aksjonspunkt;

import java.util.List;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface AksjonspunktUtleder {

    List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling);
}
