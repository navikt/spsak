package no.nav.foreldrepenger.domene.uttak.perioder;

import java.time.DayOfWeek;
import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public final class PerioderUtenHelgUtil {

    private PerioderUtenHelgUtil() {
    }

    public static boolean periodeUtenHelgOmslutter(LukketPeriode omsluttendePeriode, LukketPeriode omsluttetPeriode) {
        LocalDateInterval omsluttetende = new LocalDateInterval(omsluttendePeriode.getFom(), omsluttendePeriode.getTom());
        LocalDateInterval omsluttet = new LocalDateInterval(omsluttetPeriode.getFom(), omsluttetPeriode.getTom());
        return periodeUtenHelgOmslutter(omsluttetende, omsluttet);
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

    private static LocalDate justerFom(LocalDate fom) {
        if (fom.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return fom.plusDays(2);
        } else if (fom.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return fom.plusDays(1);
        } else {
            return fom;
        }
    }

    private static LocalDate justerTom(LocalDate tom) {
        if (tom.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return tom.minusDays(1);
        } else if (tom.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return tom.minusDays(2);
        } else {
            return tom;
        }
    }

    public static boolean periodeUtenHelgOmslutter(LocalDate omsluttendeFom, LocalDate omsluttendeTom,
                                                   LocalDate omsluttetFom, LocalDate omsluttetTom) {
        return periodeUtenHelgOmslutter(new LocalDateInterval(omsluttendeFom, omsluttendeTom),
            new LocalDateInterval(omsluttetFom, omsluttetTom));
    }

    public static boolean periodeUtenHelgOmslutter(LocalDateInterval omsluttendePeriode,
                                                   LocalDateInterval omsluttetPeriode) {
        LocalDate fom1 = justerFom(omsluttendePeriode.getFomDato());
        LocalDate tom1 = justerTom(omsluttendePeriode.getTomDato());
        LocalDate fom2 = justerFom(omsluttetPeriode.getFomDato());
        LocalDate tom2 = justerTom(omsluttetPeriode.getTomDato());

        return !fom2.isBefore(fom1) && !tom2.isAfter(tom1);
    }
}
