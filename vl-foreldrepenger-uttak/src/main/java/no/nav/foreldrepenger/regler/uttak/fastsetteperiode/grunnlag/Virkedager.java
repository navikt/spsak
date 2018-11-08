package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static java.lang.Math.toIntExact;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;

public class Virkedager {

    private static int DAGER_PR_UKE = 7;
    private static int VIRKEDAGER_PR_UKE = 5;
    private static int HELGEDAGER_PR_UKE = DAGER_PR_UKE - VIRKEDAGER_PR_UKE;

    private Virkedager() {
        // For å unngå instanser
    }


    public static int beregnAntallVirkedager(Periode periode) {
        Objects.requireNonNull(periode);
        return beregnAntallVirkedager(periode.getFom(), periode.getTom());
    }

    public static int beregnAntallVirkedager(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom);
        Objects.requireNonNull(tom);
        if (fom.isAfter(tom)) {
            throw new IllegalArgumentException("Utviklerfeil: fom " + fom + " kan ikke være før tom " + tom);
        }

        try {
            // Utvid til nærmeste mandag tilbake i tid fra og med begynnelse (fom) (0-6 dager)
            int padBefore = fom.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            // Utvid til nærmeste søndag fram i tid fra og med slutt (tom) (0-6 dager)
            int padAfter = DayOfWeek.SUNDAY.getValue() - tom.getDayOfWeek().getValue();
            // Antall virkedager i perioden utvidet til hele uker
            int virkedagerPadded = toIntExact(ChronoUnit.WEEKS.between(fom.minusDays(padBefore), tom.plusDays(padAfter).plusDays(1)) * VIRKEDAGER_PR_UKE);
            // Antall virkedager i utvidelse
            int virkedagerPadding = Math.min(padBefore, VIRKEDAGER_PR_UKE) + Math.max(padAfter - HELGEDAGER_PR_UKE, 0);
            // Virkedager i perioden uten virkedagene fra utvidelse
            return virkedagerPadded - virkedagerPadding;
        } catch (ArithmeticException e) {
            throw new UnsupportedOperationException("Perioden er for lang til å beregne virkedager.", e);
        }
    }

    public static LocalDate plusVirkedager(LocalDate fom, int virkedager) {
        int uker = virkedager / VIRKEDAGER_PR_UKE;
        int dager = virkedager % VIRKEDAGER_PR_UKE;

        LocalDate resultat = fom.plusWeeks(uker);

        while (dager > 0 || erHelg(resultat)) {
            if (!erHelg(resultat)) {
                dager--;
            }
            resultat = resultat.plusDays(1);
        }
        return resultat;
    }
    private static boolean erHelg(LocalDate dato) {
        return dato.getDayOfWeek().equals(DayOfWeek.SATURDAY) || dato.getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

}
