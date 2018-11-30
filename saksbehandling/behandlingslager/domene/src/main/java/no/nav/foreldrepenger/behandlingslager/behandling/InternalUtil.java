package no.nav.foreldrepenger.behandlingslager.behandling;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;

/**
 * Kun til invortes bruk, er ikke en del av API og all bruk (inkl. tester) frabes utenfor denne modulen.
 * <p>
 * Brukes til å skille ut metoder som ellers vil bryte enkapsulering mellom ulike pakker her inntil de selv klarer det
 * (eller Java9).
 * "Encapsulation-by-Obscurity".
 */
public final class InternalUtil {
    private InternalUtil() {
    }

    public static void leggTilAksjonspunkt(Behandling behandling, Aksjonspunkt aksjonspunkt) {
        behandling.addAksjonspunkt(aksjonspunkt);
    }

    public static void fjernAksjonspunkt(Behandling behandling, Aksjonspunkt aksjonspunkt) {
        behandling.fjernAksjonspunkt(aksjonspunkt);
    }

}
