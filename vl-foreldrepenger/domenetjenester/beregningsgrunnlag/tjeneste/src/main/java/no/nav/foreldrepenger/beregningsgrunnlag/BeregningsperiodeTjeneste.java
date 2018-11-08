package no.nav.foreldrepenger.beregningsgrunnlag;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.BevegeligeHelligdagerUtil;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class BeregningsperiodeTjeneste {

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private int inntektRapporteringFristDag;

    BeregningsperiodeTjeneste() {
        // for CDI-proxy
    }

    @Inject
    public BeregningsperiodeTjeneste(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste, BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                     @KonfigVerdi(value = "inntekt.rapportering.frist.dato") int inntektRapporteringFristDag) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.inntektRapporteringFristDag = inntektRapporteringFristDag;
    }

    public static DatoIntervallEntitet fastsettBeregningsperiodeForATFLAndeler(LocalDate skjæringstidspunkt) {
        LocalDate fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    public boolean skalBehandlingSettesPåVent(Behandling behandling) {
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
        if (!harAktivitetStatuserSomKanSettesPåVent(beregningsgrunnlag)) {
            return false;
        }
        LocalDate beregningsperiodeTom = hentBeregningsperiodeTomForATFL(beregningsgrunnlag);
        LocalDate dagensDato = LocalDate.now(FPDateUtil.getOffset());
        LocalDate originalFrist = beregningsperiodeTom.plusDays(inntektRapporteringFristDag);
        LocalDate fristMedHelligdagerInkl = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(originalFrist);
        if (dagensDato.isAfter(fristMedHelligdagerInkl)) {
            return false;
        }
        return !harMottattInntektsmeldingEllerOppdatertInntektsinformasjonForAlleArbeidsforhold(behandling, beregningsgrunnlag);
    }

    private boolean harAktivitetStatuserSomKanSettesPåVent(Beregningsgrunnlag beregningsgrunnlag) {
        boolean harBareStatusTY = beregningsgrunnlag.getAktivitetStatuser().stream()
            .map(BeregningsgrunnlagAktivitetStatus::getAktivitetStatus)
            .allMatch(as -> as.equals(AktivitetStatus.TILSTØTENDE_YTELSE));
        return !harBareStatusTY && beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .map(BeregningsgrunnlagPrStatusOgAndel::getAktivitetStatus)
            .anyMatch(status -> status.erArbeidstaker() || status.erFrilanser());
    }

    public LocalDate utledBehandlingPåVentFrist(Behandling behandling) {
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling);
        LocalDate beregningsperiodeTom = hentBeregningsperiodeTomForATFL(beregningsgrunnlag);
        LocalDate frist = beregningsperiodeTom.plusDays(inntektRapporteringFristDag);
        return BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(frist).plusDays(1);
    }

    private boolean harMottattInntektsmeldingEllerOppdatertInntektsinformasjonForAlleArbeidsforhold(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        boolean erFrilanser = beregningsgrunnlag.getAktivitetStatuser().stream()
            .map(BeregningsgrunnlagAktivitetStatus::getAktivitetStatus).anyMatch(AktivitetStatus::erFrilanser);
        if (erFrilanser) {
            return false;
        }
        final List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldinger(behandling);
        Set<Virksomhet> arbeidsgivere = hentAlleArbeidsgivereIBeregningsgrunnlagUtenInntektsmelding(beregningsgrunnlag, inntektsmeldinger);
        if (arbeidsgivere.isEmpty()) {
            return true;
        }
        LocalDate beregningsperiodeTom = hentBeregningsperiodeTomForATFL(beregningsgrunnlag);
        final List<Inntekt> inntekter = inntektArbeidYtelseTjeneste.hentAggregat(behandling).getAktørInntektForFørStp(behandling.getAktørId())
            .map(AktørInntekt::getInntektBeregningsgrunnlag).orElse(Collections.emptyList());
        return arbeidsgivere.stream()
            .allMatch(virksomhet -> inntekter.stream()
                .filter(inntekt -> inntekt.getArbeidsgiver() != null)
                .filter(inntekt -> virksomhet.equals(inntekt.getArbeidsgiver().getVirksomhet()))
                .flatMap(inntekt -> inntekt.getInntektspost().stream())
                .anyMatch(inntektspost -> inntektspost.getFraOgMed().withDayOfMonth(1).equals(beregningsperiodeTom.withDayOfMonth(1)))
            );
    }

    private static LocalDate hentBeregningsperiodeTomForATFL(Beregningsgrunnlag beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getAktivitetStatus().erArbeidstaker() || andel.getAktivitetStatus().erFrilanser())
            .map(BeregningsgrunnlagPrStatusOgAndel::getBeregningsperiodeTom)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Beregningsperiode skal være satt for arbeidstaker- og frilansandeler"));
    }

    private static Set<Virksomhet> hentAlleArbeidsgivereIBeregningsgrunnlagUtenInntektsmelding(Beregningsgrunnlag beregningsgrunnlag, List<Inntektsmelding> inntektsmeldinger) {
        return hentAlleVirksomheterPåGrunnlaget(beregningsgrunnlag)
            .filter(virksomhet -> inntektsmeldinger
                .stream()
                .map(Inntektsmelding::getVirksomhet)
                .noneMatch(v -> v.equals(virksomhet)))
            .distinct()
            .collect(Collectors.toSet());
    }

    private static Stream<Virksomhet> hentAlleVirksomheterPåGrunnlaget(Beregningsgrunnlag beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .map(BeregningsgrunnlagPrStatusOgAndel::getBgAndelArbeidsforhold)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(BGAndelArbeidsforhold::getVirksomhet)
            .filter(Optional::isPresent)
            .map(Optional::get);
    }


}
