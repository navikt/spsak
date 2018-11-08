package no.nav.foreldrepenger.domene.kontrollerfakta.omsorg;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;



@ApplicationScoped
public class AksjonspunktUtlederForForeldreansvar implements AksjonspunktUtleder {

    AksjonspunktUtlederForForeldreansvar() {
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_FORELDREANSVAR);
    }

}
