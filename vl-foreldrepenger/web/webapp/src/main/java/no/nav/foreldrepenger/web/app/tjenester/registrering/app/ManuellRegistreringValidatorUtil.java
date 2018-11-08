package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.FREMTIDIG_DATO;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.OVERLAPPENDE_PERIODER;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.PAAKREVD_FELT;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.STARTDATO_FØR_SLUTTDATO;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.TIDLIGERE_DATO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

import no.nav.vedtak.util.FPDateUtil;

public class ManuellRegistreringValidatorUtil {

    private ManuellRegistreringValidatorUtil() {
        // skal ikke lages instans
    }

    static boolean perioderOverlapper(Periode p1, Periode p2) {
        if (p2.getStart() == null || p2.getSlutt() == null || p1.getStart() == null || p1.getSlutt() == null ) {
            return false;
        }
        p2.begynnerFør(p1);
        boolean p1BegynnerFørst = p1.begynnerFør(p2);
        Periode begynnerFørst = p1BegynnerFørst ? p1 : p2;
        Periode begynnerSist = p1BegynnerFørst ? p2 : p1;
        return begynnerFørst.getSlutt().isAfter(begynnerSist.getStart());
    }

    static List<String> overlappendePerioder(List<Periode> perioder) {
        List<String> feil = new ArrayList<>();
        for (int i = 0; i < perioder.size(); i++) {
            Periode periode = perioder.get(i);

            for (int y = i + 1; y < perioder.size(); y++) {
                if (perioderOverlapper(periode, perioder.get(y))) {
                    feil.add(OVERLAPPENDE_PERIODER);
                }
            }
        }
        return feil;
    }

    static List<String> startdatoFørSluttdato(List<Periode> perioder) {
        return perioder.stream().filter(p -> !p.startFørSlutt()).map(p -> STARTDATO_FØR_SLUTTDATO).collect(Collectors.toList());
    }

    static List<String> datoIkkeNull(List<Periode> perioder) {
        List<String> feil = new ArrayList<>();
        for (Periode periode: perioder) {
            if (periode.getStart() == null || periode.getSlutt() == null) {
                feil.add(PAAKREVD_FELT);
            }
        }
        return feil;
    }

    static List<String> startdatoFørDagensDato(List<Periode> perioder) {
        LocalDate now = LocalDate.now(FPDateUtil.getOffset());
        return perioder.stream().filter(p -> p.start.isBefore(now)).map(p -> FREMTIDIG_DATO).collect(Collectors.toList());
    }

    static List<String> periodeFørDagensDato(List<Periode> perioder) {
        return perioder.stream().filter(p -> !p.erFørDagensDato()).map(p -> TIDLIGERE_DATO).collect(Collectors.toList());
    }

    static class Periode {
        private LocalDate start;
        private LocalDate slutt;

        Periode(LocalDate start, LocalDate slutt) {
            this.start = start;
            this.slutt = slutt;
        }

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getSlutt() {
            return slutt;
        }

        boolean begynnerFør(Periode otherPeriode) {
            return start.isBefore(otherPeriode.start);
        }

        boolean startFørSlutt(){
            if (slutt == null || start == null) {
                return true;
            }
            return start.isBefore(slutt) || start.isEqual(slutt);
        }

        boolean erFørDagensDato() {
            LocalDate now = LocalDate.now(FPDateUtil.getOffset());
            return !(start.isAfter(now) || start.isEqual(now) || slutt.isAfter(now));
        }
    }
}
