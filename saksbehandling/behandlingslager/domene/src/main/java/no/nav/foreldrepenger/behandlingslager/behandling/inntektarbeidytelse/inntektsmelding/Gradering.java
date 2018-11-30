package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import java.math.BigDecimal;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Gradering {
    /**
     * Perioden
     * @return perioden
     */
    DatoIntervallEntitet getPeriode();

    /**
     * En arbeidstaker kan kombinere foreldrepenger med deltidsarbeid.
     *
     * Når arbeidstakeren jobber deltid, utgjør foreldrepengene differansen mellom deltidsarbeidet og en 100 prosent stilling.
     * Det er ingen nedre eller øvre grense for hvor mye eller lite arbeidstakeren kan arbeide.
     *
     * Eksempel
     * Arbeidstaker A har en 100 % stilling og arbeider fem dager i uken. Arbeidstakeren ønsker å arbeide to dager i uken i foreldrepengeperioden.
     * Arbeidstids- prosenten blir da 40 %.
     *
     * Arbeidstaker B har en 80 % stilling og arbeider fire dager i uken. Arbeidstakeren ønsker å arbeide to dager i uken i foreldrepengeperioden.
     * Arbeidstidprosenten blir også her 40 %.
     *
     * @return prosentsats
     */
    BigDecimal getArbeidstidProsent();
}
