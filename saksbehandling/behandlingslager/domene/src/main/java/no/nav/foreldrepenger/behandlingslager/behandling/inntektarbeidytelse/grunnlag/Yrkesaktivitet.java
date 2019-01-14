package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.util.Collection;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;

public interface Yrkesaktivitet {

    /**
     * Kategorisering av aktivitet som er enten pensjonsgivende inntekt eller likestilt med pensjonsgivende inntekt
     * <p>
     * Fra aa-reg
     * <ul>
     * <li>{@value ArbeidType#ORDINÆRT_ARBEIDSFORHOLD}</li>
     * <li>{@value ArbeidType#MARITIMT_ARBEIDSFORHOLD}</li>
     * <li>{@value ArbeidType#FORENKLET_OPPGJØRSORDNING}</li>
     * </ul>
     * <p>
     * Fra inntektskomponenten
     * <ul>
     * <li>{@value ArbeidType#FRILANSER_OPPDRAGSTAKER_MED_MER}</li>
     * </ul>
     * <p>
     * De resterende kommer fra søknaden
     *
     * @return {@link ArbeidType}
     */
    ArbeidType getArbeidType();

    /**
     * Unik identifikator for arbeidsforholdet til aktøren i bedriften. Selve nøkkelen er ikke unik, men er unik for arbeidstaker hos arbeidsgiver.
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return referanse
     */
    Optional<ArbeidsforholdRef> getArbeidsforholdRef();

    /**
     * Liste over fremtidige / historiske permisjoner hos arbeidsgiver.
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return liste med permisjoner
     */
    Collection<Permisjon> getPermisjon();

    /**
     * Liste over perioder med aktivitet
     *
     * @return liste med permisjoner
     */
    Collection<AktivitetsAvtale> getAktivitetsAvtaler();

    /**
     * Arbeidsgiver
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return {@link Arbeidsgiver}
     */
    Arbeidsgiver getArbeidsgiver();

    /**
     * Navn på utenlands arbeidsgiver
     *
     * @return Navn
     */
    String getNavnArbeidsgiverUtland();

    /**
     * Gir hele ansettelsesperioden for et arbeidsforhold.
     *
     * NB! Gjelder kun arbeidsforhold.
     *
     * @return perioden
     */
    Optional<AktivitetsAvtale> getAnsettelsesPeriode();

    boolean erArbeidsforhold();

}
