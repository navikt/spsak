package no.nav.foreldrepenger.behandlingskontroll;

/**
 * Visitor som kan benyttes til å traversere en sekvens av {@link BehandlingSteg}.
 * 
 */
interface BehandlingModellVisitor {

    /**
     * Kall på et {@link BehandlingSteg}.
     *
     * @param steg - modell av steg som skal kalles
     * @return {@link BehandlingStegProsesseringResultat}
     */
    BehandlingStegProsesseringResultat prosesser(BehandlingStegModell steg);

}
