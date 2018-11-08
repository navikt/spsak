package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

public class Periode {
    private static final LocalDate MIN_DATO = LocalDate.of(2000, Month.JANUARY, 1);
    private static final LocalDate MAX_DATO = LocalDate.of(9999, Month.DECEMBER, 31);

    private LocalDate fom;
    private LocalDate tom;

    public Periode(LocalDate fom, LocalDate tom) {
        this.fom = (fom == null ? MIN_DATO : fom);
        this.tom = (tom == null ? MAX_DATO : tom);
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public LocalDate getFomOrNull() {
        return MIN_DATO.equals(fom) ? null : fom;
    }

    public LocalDate getTomOrNull() {
        return MAX_DATO.equals(tom) ? null : tom;
    }

    public static Periode månederFør(LocalDate dato, long måneder) {
        if (måneder < 1) {
            throw new IllegalArgumentException("Perioden må inneholde minst 1 måned");
        }
        LocalDate tom = dato.withDayOfMonth(1).minusDays(1);
        LocalDate fom = tom.withDayOfMonth(1).minusMonths(måneder - 1);
        return new Periode(fom, tom);
    }

    public boolean inneholder(LocalDate dato) {
        Objects.requireNonNull(dato, "inneholder dato");
        return (!(dato.isBefore(fom) || dato.isAfter(tom)));
    }

    public static Periode of(LocalDate fom, LocalDate tom) {
        return new Periode(fom, tom);
    }

    public static Periode heleÅrFør(LocalDate date, long år) {
        if (år < 1) {
            throw new IllegalArgumentException("Perioden må inneholde minst 1 år");
        }
        LocalDate fom = date.minusYears(år).withDayOfYear(1);
        LocalDate tom = fom.plusYears(år - 1).withMonth(12).withDayOfMonth(31);
        return new Periode(fom, tom);
    }

    @Override
    public boolean equals(Object annen) {
        if (!(annen instanceof Periode)) {
            return false;
        }
        Periode annenPeriode = (Periode) annen;
        return fom.equals(annenPeriode.fom) && tom.equals(annenPeriode.tom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fom, tom);
    }

    @Override
    public String toString() {
        return beskriv(fom) + " - " + beskriv(tom);
    }

    private String beskriv(LocalDate dato) {
        return (MIN_DATO.equals(dato) || MAX_DATO.equals(dato)) ? "ubegrenset" : dato.toString();
    }

    public boolean slutterEtter(Periode annen) {
        return this.tom.isAfter(annen.tom);
    }

    public long getVarighetDager() {
        return tom.toEpochDay() - fom.toEpochDay();
    }

}
