package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;

public interface InntektsmeldingAggregat {

    /**
     * Alle gjeldende inntektsmeldinger i behandlingen.
     * @return Liste med {@link Inntektsmelding}
     */
    List<Inntektsmelding> getInntektsmeldinger();

    /**
     * Alle gjeldende inntektsmeldinger for en virksomhet i behandlingen.
     * @return Liste med {@link Inntektsmelding}
     */
    List<Inntektsmelding> getInntektsmeldingerFor(Virksomhet virksomhet);

}
