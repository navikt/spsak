package no.nav.foreldrepenger.perioder;

import java.time.DayOfWeek;
import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public final class PerioderUtenHelgUtil {

    private PerioderUtenHelgUtil() {
        //Privat constructor for å hindre instanser.
    }

    public static boolean periodeUtenHelgOmslutter(LukketPeriode omsluttendePeriode, LukketPeriode omsluttetPeriode) {
        LocalDate fom1 = justerFom(omsluttendePeriode.getFom());
        LocalDate tom1 = justerTom(omsluttendePeriode.getTom());
        LocalDate fom2 = justerFom(omsluttetPeriode.getFom());
        LocalDate tom2 = justerTom(omsluttetPeriode.getTom());

        return !fom2.isBefore(fom1) && !tom2.isAfter(tom1);
    }

    private static boolean periodeErTom(LocalDate fom1, LocalDate tom1) {
        return tom1.isBefore(fom1);
    }

    public static boolean perioderUtenHelgOverlapper(LukketPeriode periode1, LukketPeriode periode2) {
        return perioderUtenHelgOverlapper(periode1.getFom(), periode1.getTom(), periode2.getFom(), periode2.getTom());
    }

    public static boolean perioderUtenHelgOverlapper(LocalDate fom1, LocalDate tom1, LocalDate fom2, LocalDate tom2) {
        LocalDate justertFom1 = justerFom(fom1);
        LocalDate justertTom1 = justerTom(tom1);
        if (periodeErTom(justertFom1, justertTom1)) {
            return false;
        }
        LocalDate justertFom2 = justerFom(fom2);
        LocalDate justertTom2 = justerTom(tom2);
        if (periodeErTom(justertFom2, justertTom2)) {
            return false;
        }
        return !justertFom2.isAfter(justertTom1) && !justertTom2.isBefore(justertFom1);
    }

    public static boolean likNårHelgIgnoreres(LocalDate fom1, LocalDate tom1, LocalDate fom2, LocalDate tom2) {
        return justerFom(fom1).equals(justerFom(fom2)) && justerTom(tom1).equals(justerTom(tom2));
    }

    private static LocalDate justerFom(LocalDate dato) {
        return helgBlirMandag(dato);
    }

    private static LocalDate justerTom(LocalDate dato) {
        return helgBlirFredag(dato);
    }

    public static LocalDate helgBlirMandag(LocalDate dato) {
        if (dato.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return dato.plusDays(2);
        } else if (dato.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return dato.plusDays(1);
        } else {
            return dato;
        }
    }

    public static LocalDate helgBlirFredag(LocalDate dato) {
        if (dato.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return dato.minusDays(1);
        } else if (dato.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return dato.minusDays(2);
        } else {
            return dato;
        }
    }

    public static LocalDate fredagLørdagBlirSøndag(LocalDate dato) {
        if (dato.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return dato.plusDays(2);
        } else if (dato.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return dato.plusDays(1);
        } else {
            return dato;
        }
    }

}
