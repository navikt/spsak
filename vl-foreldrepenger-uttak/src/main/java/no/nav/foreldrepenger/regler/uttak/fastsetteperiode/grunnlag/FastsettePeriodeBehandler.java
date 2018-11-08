package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

/**
 * Dette interfacet er beregnet til å brukes i orkestrerer for å innvilge/avslå perioder, og for
 * å flytte aktuell periode til neste periode.
 */
public interface FastsettePeriodeBehandler {

    /**
     * Hvis knekkpunkt er gitt, knekk opp aktuell periode og innvilg første del av perioden.
     * Hvis knekkpunkt er null, innvilg hele perioden.
     * Sett aktuell periode til neste periode.
     *  @param knekkpunkt dato når den avknekte perioden skal starte.
     * @param innvilgetÅrsak
     * @param avslåGradering avlå gradering på perioden dersom det er søkt om gradering.
     * @param graderingIkkeInnvilgetÅrsak årsak til at graderinger ikke ble innvilget. Null dersom gradering ble innvilget.
     * @param arbeidsprosenter arbeidsprosenter som skal brukes for å beregne utbetalingsgrad.
     * @param utbetal true dersom perioden skal utbetales, false dersom perioden ikke skal utbetales.
     */
    void innvilgAktuellPeriode(LocalDate knekkpunkt, Årsak innvilgetÅrsak, boolean avslåGradering, GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak, Arbeidsprosenter arbeidsprosenter, boolean utbetal);

    /**
     * Hvis knekkpunkt er gitt, knekk opp aktuell periode og avslå første del av perioden.
     * Hvis knekkpunkt er null, avslå hele perioden.
     * Sett aktuell periode til neste periode.
     *
     * @param knekkpunkt dato når den avknekte perioden skal starte.
     * @param årsak grunn til at perioden ikke ble oppfylt.
     * @param arbeidsprosenter arbeidsprosenter som skal brukes for å beregne utbetalingsgrad.
     * @param trekkDagerFraSaldo trekk dager fra saldo dersom true, ellers uendret saldo ved ikke oppfylt periode.
     * @param utbetal true dersom perioden skal utbetales, false dersom perioden ikke skal utbetales.
     */
    void avslåAktuellPeriode(LocalDate knekkpunkt, Årsak årsak, Arbeidsprosenter arbeidsprosenter, boolean trekkDagerFraSaldo, boolean utbetal);

    /**
     * Sett en periode til manuell behandling med en årsak.
     *
     * @param manuellbehandlingårsak årsak til at manuell behandling ble trigget.
     * @param ikkeOppfyltÅrsak foreslått årsak til at perioden ikke er oppfylt.
     * @param arbeidsprosenter arbeidsprosenter som skal brukes for å beregne utbetalingsgrad.
     * @param utbetal true dersom perioden skal utbetales, false dersom perioden ikke skal utbetales.
     */
    void manuellBehandling(Manuellbehandlingårsak manuellbehandlingårsak, Årsak ikkeOppfyltÅrsak, Arbeidsprosenter arbeidsprosenter, boolean utbetal);

    /**
     * Sett en periode til manuell behandling uten oppgitt årsak. Bruk når tidligere periode ble satt til manuell behahandling.
     */
    void manuellBehandling();


    /**
     * Hent regelgrunnlag for behandler.
     *
     * @return fastsette periode grunnlag.
     */
    FastsettePeriodeGrunnlag grunnlag();

}
