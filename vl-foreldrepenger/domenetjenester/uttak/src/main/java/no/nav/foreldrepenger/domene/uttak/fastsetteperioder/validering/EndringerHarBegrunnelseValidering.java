package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import java.util.Objects;

import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.vedtak.feil.FeilFactory;

class EndringerHarBegrunnelseValidering implements OverstyrUttakPerioderValidering {

    private UttakResultatPerioder opprinnelig;

    EndringerHarBegrunnelseValidering(UttakResultatPerioder opprinnelig) {
        this.opprinnelig = opprinnelig;
    }

    @Override
    public void utfør(UttakResultatPerioder nyePerioder) {
        for (UttakResultatPeriode periode : nyePerioder.getPerioder()) {
            if (nullOrEmpty(periode.getBegrunnelse()) && harEndring(periode)) {
                throw FeilFactory.create(OverstyrUttakValideringFeil.class).periodeManglerBegrunnelse().toException();
            }
        }
    }

    private boolean nullOrEmpty(String begrunnelse) {
        return Objects.isNull(begrunnelse) || Objects.equals(begrunnelse, "");
    }

    private boolean harEndring(UttakResultatPeriode periode) {
        for (UttakResultatPeriode opprinneligPeriode : opprinnelig.getPerioder()) {
            if (periode.erLik(opprinneligPeriode)) {
                return false;
            }
        }
        return true;
    }
}
