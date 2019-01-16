package no.nav.foreldrepenger.behandlingskontroll;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

public interface BehandlingSteg {

    /**
     * Returner statuskode med ev nye aksjonspunkter funnet i dette steget.
     */
    BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst);

    /**
     * Template Method - transisjoner utover normal flyt (utførSteg) for opprydding el.
     * @param kontekst
     *            - overordnet kontekst informasjon og lås for å gjøre endringer på behandlingen.
     * @param behandling
     * @param modell
*            - BehandlingStegModell som kan benyttes til oppslag av hvordan flyten skal være
     * @param førsteSteg
*       - Det første steget av stegene det hoppes mellom. Vil være steget det hoppes til ved bakoverhopp.
     * @param sisteSteg
*       - Det siste steget av stegene det hoppes mellom. Vil være steget det hoppes fra ved bakoverhopp.
     * @param inngangUtgang
*       - Forteller om det hoppes til inngang eller utgang.
     */
    default void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell,
                               TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType inngangUtgang) {
        switch (transisjonType) {
            case FØR_INNGANG:
                vedInngang(kontekst, behandling, modell);
                break;
            case ETTER_UTGANG:
                vedUtgang(kontekst, behandling, modell);
                break;
            case HOPP_OVER_BAKOVER:
                vedHoppOverBakover(kontekst, behandling, modell, førsteSteg, sisteSteg);
                break;
            case HOPP_OVER_FRAMOVER:
                vedHoppOverFramover(kontekst, behandling, modell);
                break;
            default:
                throw new IllegalArgumentException("Uhåndtert transisjonType: " + transisjonType //$NON-NLS-1$
                        + " i steg: " + modell.getBehandlingStegType()); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("unused")
    default void vedInngang(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell) {
        // TEMPLATE METHOD
    }

    @SuppressWarnings("unused")
    default void vedUtgang(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell) {
        // TEMPLATE METHOD
    }

    @SuppressWarnings("unused")
    default void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        // TEMPLATE METHOD
    }

    @SuppressWarnings("unused")
    default void vedHoppOverFramover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell) {
        // TEMPLATE METHOD
    }

     enum TransisjonType {
        /** TODO (FC): Ikke implementert ennå. */
        FØR_INNGANG,
        /** TODO (FC): Ikke implementert ennå. */
        ETTER_UTGANG,

        /**
         * Kalles for steg som hoppes over når behandlingen skrider frem. Kan f.eks. brukes til å avbryte
         *
         * resultater/vilkår som ikke kan settes når et steg hoppes over. Aksjonspunkt vil håndteres automatisk avhengig
         * av hvilket vurderingspunkt de tilhører og hvor de ble identifisert.
         */
        HOPP_OVER_FRAMOVER,

        /**
         * Kalles for steg som hoppes over ved tilbakeføring av behandling. Kan f.eks. brukes til å rydde vekk
         *
         * resultater/vilkår som bør resettes. Aksjonspunkt vil håndteres automatisk avhengig av hvilket vurderingspunkt
         * de tilhører.
         */
        HOPP_OVER_BAKOVER
    }
}
