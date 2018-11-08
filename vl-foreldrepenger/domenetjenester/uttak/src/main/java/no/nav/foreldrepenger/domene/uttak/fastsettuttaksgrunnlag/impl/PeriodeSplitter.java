package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.threeten.extra.Days;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;

class PeriodeSplitter {

    private PeriodeSplitter() {
        //Forhindrer instanser
    }

    static List<OppgittPeriode> splittPeriodeMotVenstre(List<OppgittPeriode> ikkeFlyttbarePerioder, OppgittPeriode periode, int delta) {
        LocalDate nyFom = periode.getFom().plusDays(delta);
        LocalDate nyTom = periode.getTom().plusDays(delta);
        List<OppgittPeriode> splittedePerioder = new ArrayList<>();
        for (int i = ikkeFlyttbarePerioder.size()-1; i >= 0; i--) {
            OppgittPeriode ikkeFlyttbarPeriode = ikkeFlyttbarePerioder.get(i);
            int antallIkkeFlyttbareDager = Days.between(ikkeFlyttbarPeriode.getFom(), ikkeFlyttbarPeriode.getTom()).getAmount()+1;
            if (nyFom.isAfter(ikkeFlyttbarPeriode.getTom())) {
                //Justert perioder er etter ikke flyttbar periode. Legg til justert periode og returner perioder.
                splittedePerioder.add(OppgittPeriodeBuilder.fraEksisterende(periode).medPeriode(nyFom, nyTom).build());
                return splittedePerioder;
            } else if (nyFom.isBefore(ikkeFlyttbarPeriode.getTom()) && nyTom.isAfter(ikkeFlyttbarPeriode.getTom())) {
                //Perioden overlapper med slutten av ikke flyttbar periode.
                splittedePerioder.add(0, OppgittPeriodeBuilder.fraEksisterende(periode).medPeriode(ikkeFlyttbarPeriode.getTom().plusDays(1), nyTom). build());
                int restDager = Days.between(nyFom, nyTom).getAmount() + 1 - (Days.between(ikkeFlyttbarPeriode.getTom().plusDays(1), nyTom).getAmount() + 1);
                nyFom = nyFom.minusDays(antallIkkeFlyttbareDager);
                nyTom = nyFom.plusDays(restDager-1L);
            } else {
                //Resterende flyttes tilsvarende lengden på den ikke flyttbare perioden.
                nyFom = nyFom.minusDays(antallIkkeFlyttbareDager);
                nyTom = nyTom.minusDays(antallIkkeFlyttbareDager);
            }
        }
        splittedePerioder.add(0, OppgittPeriodeBuilder.fraEksisterende(periode).medPeriode(nyFom, nyTom).build());
        return splittedePerioder;
    }

    static List<OppgittPeriode> splittPeriodeMotHøyre(List<OppgittPeriode> ikkeFlyttbarePerioder, OppgittPeriode periode, int delta) {
        LocalDate nyFom = periode.getFom().plusDays(delta);
        LocalDate nyTom = periode.getTom().plusDays(delta);
        List<OppgittPeriode> splittedePerioder = new ArrayList<>();
        for (OppgittPeriode ikkeFlyttbarPeriode : ikkeFlyttbarePerioder) {
            int antallIkkeFlyttbareDager = Days.between(ikkeFlyttbarPeriode.getFom(), ikkeFlyttbarPeriode.getTom()).getAmount() + 1;
            if (nyTom.isBefore(ikkeFlyttbarPeriode.getFom())) {
                //Justert perioder er før ikke flyttbar periode. Legg til justert periode og returner perioder.
                splittedePerioder.add(OppgittPeriodeBuilder.fraEksisterende(periode).medPeriode(nyFom, nyTom).build());
                return splittedePerioder;
            } else if (nyFom.isBefore(ikkeFlyttbarPeriode.getFom()) && nyTom.isAfter(ikkeFlyttbarPeriode.getFom())) {
                //Perioden overlapper med begynnelsen av ikke flyttbar periode.
                splittedePerioder.add(OppgittPeriodeBuilder.fraEksisterende(periode).medPeriode(nyFom, ikkeFlyttbarPeriode.getFom().minusDays(1)).build());
                int restDager = Days.between(nyFom, nyTom).getAmount() + 1 - (Days.between(nyFom, ikkeFlyttbarPeriode.getFom().minusDays(1)).getAmount() + 1);
                nyFom = ikkeFlyttbarPeriode.getTom().plusDays(1);
                nyTom = nyFom.plusDays(restDager - 1L);
            } else {
                //Resterende flyttes tilsvarende lengden på den ikke flyttbare perioden.
                nyFom = nyFom.plusDays(antallIkkeFlyttbareDager);
                nyTom = nyTom.plusDays(antallIkkeFlyttbareDager);
            }
        }
        splittedePerioder.add(OppgittPeriodeBuilder.fraEksisterende(periode).medPeriode(nyFom, nyTom).build());
        return splittedePerioder;
    }

}
