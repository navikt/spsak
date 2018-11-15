package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Arbeidsgiverperiode {
    /**
     * Perioden
     * @return perioden
     */
    DatoIntervallEntitet getPeriode();
}
