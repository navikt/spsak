package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;

interface OverstyrUttakPerioderValidering {

    void utfør(UttakResultatPerioder nyePerioder);
}
