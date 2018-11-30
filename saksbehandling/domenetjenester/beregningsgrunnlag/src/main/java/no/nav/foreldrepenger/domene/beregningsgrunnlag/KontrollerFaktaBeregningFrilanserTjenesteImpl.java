package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.Frilans;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;

@ApplicationScoped
public class KontrollerFaktaBeregningFrilanserTjenesteImpl implements KontrollerFaktaBeregningFrilanserTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    public KontrollerFaktaBeregningFrilanserTjenesteImpl() {
        // For CDI
    }

    @Inject
    public KontrollerFaktaBeregningFrilanserTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }


    @Override
    public boolean erNyoppstartetFrilanser(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (!beregningsgrunnlagOpt.isPresent()) {
            return false;
        }
        boolean erFrilanser = beregningsgrunnlagOpt.get().getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser());

        return erFrilanser
            && inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling)
            .flatMap(InntektArbeidYtelseGrunnlag::getOppgittOpptjening)
            .flatMap(OppgittOpptjening::getFrilans)
            .map(Frilans::getErNyoppstartet)
            .orElse(false);
    }

    @Override
    public boolean harOverstyrtFrilans(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (!beregningsgrunnlagOpt.isPresent()) {
            return false;
        }
        boolean erNyoppstartetFrilans = erNyoppstartetFrilanser(behandling);
        if (!erNyoppstartetFrilans) {
            return false;
        }
        return beregningsgrunnlagOpt.get().getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser() && andel.getFastsattAvSaksbehandler() != null && andel.getFastsattAvSaksbehandler()
                && andel.getBeregnetPrÅr() != null);
    }

    @Override
    public boolean erBrukerArbeidstakerOgFrilanserISammeOrganisasjon(Behandling behandling) {
        return !brukerErArbeidstakerOgFrilanserISammeOrganisasjon(behandling).isEmpty();
    }

    @Override
    public Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (beregningsgrunnlagOpt.isPresent()) {
            return arbeidsgivereSomHarFrilansforholdOgArbeidsforholdMedBruker(beregningsgrunnlagOpt.get(), behandling);
        }
        return Collections.emptySet();
    }

    private Set<Arbeidsgiver> arbeidsgivereSomHarFrilansforholdOgArbeidsforholdMedBruker(Beregningsgrunnlag beregningsgrunnlag, Behandling behandling) {

        // Sjekk om statusliste inneholder AT og FL.

        if (beregningsgrunnlag.getBeregningsgrunnlagPerioder().isEmpty() ||
            !harFrilanserOgArbeidstakerAndeler(beregningsgrunnlag)) {
            return Collections.emptySet();
        }

        // Sjekk om samme orgnr finnes både som arbeidsgiver og frilansoppdragsgiver

        final Set<Arbeidsgiver> arbeidsforholdArbeidsgivere = finnArbeidsgivere(beregningsgrunnlag);
        if (arbeidsforholdArbeidsgivere.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<Arbeidsgiver> frilansOppdragsgivere = finnFrilansOppdragsgivere(behandling);
        if (frilansOppdragsgivere.isEmpty()) {
            return Collections.emptySet();
        }
        return finnMatchendeArbeidsgiver(arbeidsforholdArbeidsgivere, frilansOppdragsgivere);
    }

    private boolean harFrilanserOgArbeidstakerAndeler(Beregningsgrunnlag beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
        .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser()) &&
            beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .anyMatch(andel -> andel.getAktivitetStatus().erArbeidstaker());
    }

    private Set<Arbeidsgiver> finnMatchendeArbeidsgiver(final Set<Arbeidsgiver> virksomheterForArbeidsforhold, final Set<Arbeidsgiver> frilansOppdragsgivere) {
        Set<Arbeidsgiver> intersection = new HashSet<>(virksomheterForArbeidsforhold);
        intersection.retainAll(frilansOppdragsgivere);
        return intersection;
    }

    private Set<Arbeidsgiver> finnFrilansOppdragsgivere(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (!inntektArbeidYtelseGrunnlagOpt.isPresent()) {
            return Collections.emptySet();
        }
        Optional<AktørArbeid> aktørArbeidOpt = inntektArbeidYtelseGrunnlagOpt.get().getAktørArbeidFørStp(behandling.getAktørId());
        return aktørArbeidOpt.map(AktørArbeid::getFrilansOppdrag).orElse(Collections.emptyList())
            .stream()
            .map(Yrkesaktivitet::getArbeidsgiver)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toSet());
    }

    private Set<Arbeidsgiver> finnArbeidsgivere(Beregningsgrunnlag beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bpsa -> AktivitetStatus.ARBEIDSTAKER.equals(bpsa.getAktivitetStatus()))
            .map(BeregningsgrunnlagPrStatusOgAndel::getBgAndelArbeidsforhold)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(BGAndelArbeidsforhold::getArbeidsgiver)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .distinct()
            .collect(Collectors.toSet());
    }
}
