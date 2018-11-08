package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;

public interface ArbeidsgiverHistorikkinnslagTjeneste {

    /**
     * Metode som lager string som representerer arbeidsgiver i historikkinnslagene
     * hvis arbeidsforholdref ikke er tilgjengelig.
     * @param arbeidsgiver Arbeidsgiveren det skal lages historikkinnslag om
     * @param arbeidsforholdRef Arbeidsforholdreferansen til det aktuelle arbeidsforholdet
     * @return Returnerer en string på formatet: Statoil (938471284) ...ef2k for virksomheter, Ole Jensen (999888777666) ...9fj4 for privatpersoner
     */
    String lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef);


    /**
     * Metode som lager string som representerer arbeidsgiver i historikkinnslagene
     * hvis arbeidsforholdref er tilgjengelig. Viser kun de siste 4 tegn i arbeidsforholdreferansen.
     * @param arbeidsgiver Arbeidsgiveren det skal lages historikkinnslag om
     * @return Returnerer en string på formatet: Statoil (938471284) for virksomheter, Ole Jensen (999888777666) for privatpersoner
     */

    String lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver arbeidsgiver);

    /**
     * Metode som tar inn en beregningsgrunnlagandel og deretter kaller en passende metode for å lage en
     * tekstlig representasjon av arbeidsgiver og arbeidsforholdet.
     * @param bgAndel Beregningsgrunnlagsandelen det skal lages historikkinnslag for
     * @return Returnerer en string på formatet: Statoil (938471284) for virksomheter, Ole Jensen (999888777666) for privatpersoner
     */

    String lagHistorikkinnslagTekstForBeregningsgrunnlag(BeregningsgrunnlagPrStatusOgAndel bgAndel);


}
