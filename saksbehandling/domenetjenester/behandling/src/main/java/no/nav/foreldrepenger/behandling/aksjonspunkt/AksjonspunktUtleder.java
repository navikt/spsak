package no.nav.foreldrepenger.behandling.aksjonspunkt;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

import java.util.List;

public interface AksjonspunktUtleder {

    List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling);
}
