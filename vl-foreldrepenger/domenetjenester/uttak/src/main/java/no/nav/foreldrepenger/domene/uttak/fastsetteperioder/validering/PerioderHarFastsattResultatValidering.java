package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.vedtak.feil.FeilFactory;

class PerioderHarFastsattResultatValidering implements OverstyrUttakPerioderValidering {

    @Override
    public void utfør(UttakResultatPerioder nyePerioder) {
        for (UttakResultatPeriode periode : nyePerioder.getPerioder()) {
            if (periode.getResultatType() == null || periode.getResultatType() == PeriodeResultatType.IKKE_FASTSATT) {
                throw FeilFactory.create(OverstyrUttakValideringFeil.class).periodeManglerResultat().toException();
            }
        }
    }
}
