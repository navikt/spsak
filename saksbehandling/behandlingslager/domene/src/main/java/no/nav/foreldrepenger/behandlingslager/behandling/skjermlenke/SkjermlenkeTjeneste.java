package no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public class SkjermlenkeTjeneste {


    private SkjermlenkeTjeneste() {
    }

    /**
     * Returnerer skjermlenketype for eit aksjonspunkt. Inneheld logikk for spesialbehandling av aksjonspunkt som ikkje ligg på aksjonspunktdefinisjonen.
     */
    public static SkjermlenkeType finnSkjermlenkeType(AksjonspunktDefinisjon aksjonspunktDefinisjon, Behandling behandling) {
        return aksjonspunktDefinisjon.getSkjermlenkeType();
    }

}
