package no.nav.foreldrepenger.domene.arbeidsforhold;

import java.util.Map;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;

public interface VurderArbeidsforholdTjeneste {
    /**
     * Vurderer alle arbeidsforhold innhentet i en behandling.
     * <p>
     * Gjør vurderinger for å se om saksbehandler må ta stilling til enkelte av disse og returener sett med hvilke
     * saksbehandler må ta stilling til.
     * <p>
     *
     * @param behandling behandlingen
     * @return Arbeidsforholdene det må tas stilling til
     */
    Map<Arbeidsgiver, Set<ArbeidsforholdRef>> vurder(Behandling behandling);

    /**
     * Gir forskjellen i inntektsmeldinger mellom to versjoner av inntektsmeldinger.
     * Benyttes for å markere arbeidsforhold det må tas stilling til å hva saksbehandler skal gjøre.
     *
     * @param behandling behandlingen
     * @return Endringene i inntektsmeldinger
     */
    Map<Arbeidsgiver, Set<ArbeidsforholdRef>> endringerIInntektsmelding(Behandling behandling);
}
