package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.util.Collection;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;

public interface Inntekt {

    /**
     * System (+ filter) som inntektene er hentet inn fra / med
     *
     * @return {@link InntektsKilde}
     */
    InntektsKilde getInntektsKilde();

    /**
     * Utbetaler
     *
     * @return {@link Arbeidsgiver}
     */
    Arbeidsgiver getArbeidsgiver();

    /**
     * Utbetalinger utført av utbetaler
     *
     * @return liste av {@link Inntektspost}
     */
    Collection<Inntektspost> getInntektspost();

}
