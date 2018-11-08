package no.nav.foreldrepenger.feriedager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class BevegeligeHelligdagerUtil {

    private BevegeligeHelligdagerUtil() {
        //Privat constructor for å hindre instanser.
    }

    public static List<LocalDate> finnBevegeligeHelligdagerUtenHelg(LukketPeriode uttaksperiode) {
        List<LocalDate> bevegeligeHelligdager = new ArrayList<>();

        for (Integer år : utledÅreneDetSkalFinnesHelligdagerFor(uttaksperiode)) {
            bevegeligeHelligdager.addAll(finnBevegeligeHelligdagerUtenHelgPerÅr(år));
        }
        return bevegeligeHelligdager;
    }

    public static List<LocalDate> finnBevegeligeHelligdagerUtenHelgPerÅr(int år) {
        List<LocalDate> bevegeligeHelligdager = new ArrayList<>();

        // legger til de satte helligdagene
        bevegeligeHelligdager.add(LocalDate.of(år, 1, 1));
        bevegeligeHelligdager.add(LocalDate.of(år, 5, 1));
        bevegeligeHelligdager.add(LocalDate.of(år, 5, 17));
        bevegeligeHelligdager.add(LocalDate.of(år, 12, 25));
        bevegeligeHelligdager.add(LocalDate.of(år, 12, 26));

        // regner ut påskedag
        LocalDate påskedag = utledPåskedag(år);

        // søndag før påske; Palmesøndag
        bevegeligeHelligdager.add(påskedag.minusDays(7));

        // torsdag før påske; Skjærtorsdag
        bevegeligeHelligdager.add(påskedag.minusDays(3));

        // fredag før påske; Langfredag
        bevegeligeHelligdager.add(påskedag.minusDays(2));

        // 1.påskedag
        bevegeligeHelligdager.add(påskedag);

        // 2.påskedag
        bevegeligeHelligdager.add(påskedag.plusDays(1));

        // Kristi Himmelfartsdag
        bevegeligeHelligdager.add(påskedag.plusDays(39));

        // 1.pinsedag
        bevegeligeHelligdager.add(påskedag.plusDays(49));

        // 2.pinsedag
        bevegeligeHelligdager.add(påskedag.plusDays(50));

        return fjernHelg(bevegeligeHelligdager);
    }


    public static List<LocalDate> fjernHelg(List<LocalDate> bevegeligeHelligdager) {
        List<LocalDate> utenHelg = new ArrayList<>();
        for (LocalDate helligdag : bevegeligeHelligdager) {
            if (!DayOfWeek.SATURDAY.equals(helligdag.getDayOfWeek()) && !DayOfWeek.SUNDAY.equals(helligdag.getDayOfWeek())) {
                utenHelg.add(helligdag);
            }
        }
        Collections.sort(utenHelg);
        return utenHelg;
    }

    private static LocalDate utledPåskedag(int år) {
        int a = år % 19;
        int b = år / 100;
        int c = år % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = ((19 * a) + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + (2 * e) + (2 * i) - h - k) % 7;
        int m = (a + (11 * h) + (22 * l)) / 451;
        int n = (h + l - (7 * m) + 114) / 31; // Tallet på måneden
        int p = (h + l - (7 * m) + 114) % 31; // Tallet på dagen

        LocalDate dato = LocalDate.of(år, n, p + 1);

        return dato;
    }

    private static List<Integer> utledÅreneDetSkalFinnesHelligdagerFor(LukketPeriode uttaksperiode) {
        List<Integer> årene = new ArrayList<>();
        int antall = uttaksperiode.getTom().getYear() - uttaksperiode.getFom().getYear();
        for (int i = 0; i <= antall; i++) {
            årene.add(uttaksperiode.getFom().getYear() + i);
        }
        return årene;
    }
}
