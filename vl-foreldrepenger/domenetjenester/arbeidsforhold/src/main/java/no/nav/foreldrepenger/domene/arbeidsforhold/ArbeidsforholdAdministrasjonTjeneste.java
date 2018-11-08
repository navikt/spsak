package no.nav.foreldrepenger.domene.arbeidsforhold;

import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdWrapper;

/**
 * Håndterer administrasjon(saksbehandlers input) vedrørende arbeidsforhold.
 *
 *
 */
public interface ArbeidsforholdAdministrasjonTjeneste {

    /**
     * Oppretter en builder for å lagre ned overstyringen av arbeidsforhold
     *
     * @param behandling behandlingen
     * @return buildern
     */
    ArbeidsforholdInformasjonBuilder opprettBuilderFor(Behandling behandling);

    /**
     * Rydder opp i inntektsmeldinger som blir erstattet
     *
     * @param behandling behandlingen
     * @param builder ArbeidsforholdsOverstyringene som skal lagrers
     */
    void lagre(Behandling behandling, ArbeidsforholdInformasjonBuilder builder);

    Set<ArbeidsforholdWrapper> hentArbeidsforholdFerdigUtledet(Behandling behandling);
}
