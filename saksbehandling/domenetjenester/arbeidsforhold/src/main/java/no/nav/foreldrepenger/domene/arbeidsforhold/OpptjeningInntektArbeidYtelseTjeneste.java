package no.nav.foreldrepenger.domene.arbeidsforhold;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningAktivitetPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningInntektPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;

/** Henter inntekter, arbeid, og ytelser relevant for opptjening. */
public interface OpptjeningInntektArbeidYtelseTjeneste {

    Opptjening hentOpptjening(Behandling behandling);

    Optional<Opptjening> hentOpptjeningHvisEksisterer(Behandling behandling);

    /** Hent alle opptjeningaktiviteter som er bekreftet godkjent eller anntatt godkjent på angitt dato. */
    List<OpptjeningAktivitet> hentGodkjentAktivitetTyper(Behandling behandling, LocalDate dato);

    List<OpptjeningAktivitetPeriode> hentRelevanteOpptjeningAktiveterForVilkårVurdering(Behandling behandling);

    /** Hent alle inntekter for søker der det finnes arbeidsgiver*/
    List<OpptjeningInntektPeriode> hentRelevanteOpptjeningInntekterForVilkårVurdering(Behandling behandling);

    /**
     * Hent siste ytelse etter kapittel 8, 9 og 14 før skjæringstidspunkt for opptjening
     * @param behandling en Behandling
     * @return Ytelse hvis den finnes, ellers Optional.empty()
     */
    Optional<Ytelse> hentSisteInfotrygdYtelseFørSkjæringstidspunktForOpptjening(Behandling behandling);

    /**
     * Hent siste ytelse etter kapittel 8, 9 og 14 før skjæringstidspunkt for opptjening
     * @param behandling en Behandling
     * @return liste med sammenhengende ytelser som gjelder før skjæringstidspunkt for opptjening
     */
    List<Ytelse> hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(Behandling behandling);
}
