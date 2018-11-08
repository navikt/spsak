package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import java.time.LocalDate;

import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.vedtak.feil.FeilFactory;

class BareSplittetPerioderValidering implements OverstyrUttakPerioderValidering {

    private UttakResultatPerioder opprinnelig;

    BareSplittetPerioderValidering(UttakResultatPerioder opprinnelig) {
        this.opprinnelig = opprinnelig;
    }

    @Override
    public void utfør(UttakResultatPerioder nyePerioder) {
        for (UttakResultatPeriode nyPeriode : nyePerioder.getPerioder()) {
            validerAtAlleDagerINyErIOpprinnelig(nyPeriode);
        }

        for (UttakResultatPeriode opprinneligPeriode : opprinnelig.getPerioder()) {
            validerAtAlleDagerIOpprinneligErINy(opprinneligPeriode, nyePerioder);
        }
    }

    private void validerAtAlleDagerIOpprinneligErINy(UttakResultatPeriode opprinnelig, UttakResultatPerioder nyePerioder) {
        validerAtAlleDagerIPeriodeFinnesIPerioder(opprinnelig, nyePerioder);
    }

    private void validerAtAlleDagerINyErIOpprinnelig(UttakResultatPeriode nyPeriode) {
        validerAtAlleDagerIPeriodeFinnesIPerioder(nyPeriode, opprinnelig);
    }

    private void validerAtAlleDagerIPeriodeFinnesIPerioder(UttakResultatPeriode periode, UttakResultatPerioder perioder) {
        LocalDate dato = periode.getTidsperiode().getFomDato();
        while (dato.isBefore(periode.getTidsperiode().getTomDato()) || dato.isEqual(periode.getTidsperiode().getTomDato())) {
            if (!datoFinnesBareEnGangIPerioder(dato, perioder)) {
                throwException();
            }
            dato = dato.plusDays(1);
        }
    }

    private boolean datoFinnesBareEnGangIPerioder(LocalDate dato, UttakResultatPerioder perioder) {
        int antallFunnet = 0;
        for (UttakResultatPeriode periode : perioder.getPerioder()) {
            if (periode.harDatoIPerioden(dato)) {
                antallFunnet++;
            }
        }
        return antallFunnet == 1;
    }

    private void throwException() {
        throw FeilFactory.create(OverstyrUttakValideringFeil.class).ugyldigSplittingAvPeriode().toException();
    }
}
