package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.feil.FeilFactory;

class HarSattUtbetalingsprosentValidering implements OverstyrUttakPerioderValidering {

    private UttakResultatPerioder opprinnelig;

    HarSattUtbetalingsprosentValidering(UttakResultatPerioder opprinnelig) {
        this.opprinnelig = opprinnelig;
    }

    @Override
    public void utfør(UttakResultatPerioder nyePerioder) {
        for (UttakResultatPeriode periode : nyePerioder.getPerioder()) {
            if (manglerUtbetalingsprosent(periode) && opprinneligErManuell(periode)) {
                throw FeilFactory.create(OverstyrUttakValideringFeil.class).manglerUtbetalingsprosent(periode.getTidsperiode()).toException();
            }
        }
    }

    private boolean manglerUtbetalingsprosent(UttakResultatPeriode periode) {
        return periode.getAktiviteter().stream().anyMatch(p -> p.getUtbetalingsgrad() == null);
    }

    private boolean opprinneligErManuell(UttakResultatPeriode periode) {
        LocalDateInterval tidsperiode = periode.getTidsperiode();
        for (UttakResultatPeriode opprinneligPeriode : opprinnelig.getPerioder()) {
            if (opprinneligPeriode.getTidsperiode().overlaps(tidsperiode)) {
                return opprinneligPeriode.getResultatType().equals(PeriodeResultatType.MANUELL_BEHANDLING);
            }

        }
        return false;
    }

}
