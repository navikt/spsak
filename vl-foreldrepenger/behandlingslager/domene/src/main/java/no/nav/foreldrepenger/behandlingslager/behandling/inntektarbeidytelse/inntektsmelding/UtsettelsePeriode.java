package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface UtsettelsePeriode {
    /**
     * Perioden som utsettes
     * @return perioden
     */
    DatoIntervallEntitet getPeriode();

    /**
     * Årsaken til utsettelsen
     * @return utsettelseårsaken
     */
    UtsettelseÅrsak getÅrsak();
}
