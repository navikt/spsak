package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;

public class AksjonspunktTestutfall {
   private AksjonspunktDefinisjon aksjonspunktDefinisjon;
   private AksjonspunktStatus status;

    private AksjonspunktTestutfall(AksjonspunktDefinisjon aksjonspunktDefinisjon, AksjonspunktStatus status) {
        this.aksjonspunktDefinisjon = aksjonspunktDefinisjon;
        this.status = status;
    }

    AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }

    AksjonspunktStatus getStatus() {
        return status;
    }

    public static AksjonspunktTestutfall resultat(AksjonspunktDefinisjon definisjon, AksjonspunktStatus status) {
        return new AksjonspunktTestutfall(definisjon, status);
    }
}
