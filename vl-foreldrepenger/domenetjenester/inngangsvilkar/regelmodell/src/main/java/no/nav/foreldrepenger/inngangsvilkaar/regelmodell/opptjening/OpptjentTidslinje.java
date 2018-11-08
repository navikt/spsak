package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening;

import java.time.Period;

import no.nav.fpsak.tidsserie.LocalDateTimeline;

/** Beskriver opptjente dager og totalt beregnet periode opptjent. */
public class OpptjentTidslinje {
    /**
     * Opptjent periode innenfor tidslinjen. Kan avvike noe fra dager i tidslinjen pga. spesielle regler rundt telling av måneder (eks. regel om
     * 26 opptjente dager = 1 oppjent måned).
     */

    private Period opptjentPeriode;

    /**
     * Tidslinje for opptjente dager.
     */
    private LocalDateTimeline<Boolean> tidslinje;

    public OpptjentTidslinje(Period opptjentPeriode, LocalDateTimeline<Boolean> tidslinje) {
        this.opptjentPeriode = opptjentPeriode;
        this.tidslinje = tidslinje;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<opptjent=" + getOpptjentPeriode() + ", tidslinje=" + tidslinje + ">";
    }

    public Period getOpptjentPeriode() {
        return opptjentPeriode;
    }

    public LocalDateTimeline<Boolean> getTidslinje() {
        return tidslinje;
    }
}
