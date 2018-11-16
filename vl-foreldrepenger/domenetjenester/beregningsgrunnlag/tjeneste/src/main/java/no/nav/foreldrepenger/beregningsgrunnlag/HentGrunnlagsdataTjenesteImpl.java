package no.nav.foreldrepenger.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseAnvist;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;

@ApplicationScoped
public class HentGrunnlagsdataTjenesteImpl implements HentGrunnlagsdataTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private OpptjeningRepository opptjeningRepository;
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste;

    HentGrunnlagsdataTjenesteImpl() {
        //CDI-proxy
    }

    @Inject
    public HentGrunnlagsdataTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste,
                                         InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste, @FagsakYtelseTypeRef("FP") IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste) {
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.opptjeningRepository = repositoryProvider.getOpptjeningRepository();
        this.opptjeningInntektArbeidYtelseTjeneste = opptjeningInntektArbeidYtelseTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.iayRegisterInnhentingTjeneste = iayRegisterInnhentingTjeneste;
    }

    @Override
    public boolean vurderOmNyesteGrunnlagsdataSkalHentes(Behandling behandling) {
        Optional<Behandling> forrigeBehandlingOpt = behandling.getOriginalBehandling();
        Optional<Beregningsgrunnlag> fastsattBG = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.FASTSATT)
            .map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
        Optional<Beregningsgrunnlag> forrigeBGOpt = forrigeBehandlingOpt.flatMap(beregningsgrunnlagRepository::hentBeregningsgrunnlag);

        Optional<Beregningsgrunnlag> gjeldendeBeregningsgrunnlag = hentGjeldendeBeregningsgrunnlag(fastsattBG, forrigeBGOpt);
        if (!gjeldendeBeregningsgrunnlag.isPresent()) {
            return true;
        }
        Beregningsgrunnlag gjeldendeBG = gjeldendeBeregningsgrunnlag.get();
        Opptjening nyOpptjening = opptjeningInntektArbeidYtelseTjeneste.hentOpptjening(behandling);
        Optional<Opptjening> forrigeOpptjeningOpt = forrigeBehandlingOpt.flatMap(opptjeningRepository::finnOpptjening);

        if (erEndringIOpptjeningsAktiviteter(nyOpptjening, forrigeOpptjeningOpt)) {
            return true;
        }

        if (erEndringISkjæringstidspunktForBeregning(gjeldendeBG, forrigeBGOpt)) {
            return true;
        }
        if (erManuellRevurderingMedÅrsakEndringIOpplysningerOmInntekt(behandling)) {
            return true;
        }

        InntektArbeidYtelseGrunnlag nyIAY = inntektArbeidYtelseTjeneste.hentAggregat(behandling);
        Optional<InntektArbeidYtelseGrunnlag> forrigeIAY = forrigeBehandlingOpt.flatMap(inntektArbeidYtelseTjeneste::hentAggregatHvisEksisterer);

        if (erEndringerIOpplysningerOmYtelse(nyIAY, forrigeIAY)) {
            if (brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(behandling, gjeldendeBG, nyOpptjening)) {
                if (erEndringerIYtelseSisteTiMåneder(behandling, nyIAY, forrigeIAY, gjeldendeBG.getSkjæringstidspunkt())) {
                    return true;
                }
            } else {
                if (endringerForSisteYtelsesPeriodeFørSjæringstidspunkt(behandling, nyIAY, forrigeIAY, gjeldendeBG.getSkjæringstidspunkt())) {
                    return true;
                }
            }
        }
        if (erEndretInntektsmelding(nyIAY, forrigeIAY)) {
            InntektArbeidYtelseGrunnlag førsteVersjonIAY = inntektArbeidYtelseTjeneste.hentFørsteVersjon(behandling);
            return erEndringIInntekt(førsteVersjonIAY, nyIAY);
        }

        return false;
    }


    @Override
    public void innhentInntektsInformasjonBeregningOgSammenligning(Behandling behandling) {
        Interval opplysningsPeriode = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(behandling);
        AktørId aktørId = behandling.getAktørId();
        final InntektArbeidYtelseAggregatBuilder builder = iayRegisterInnhentingTjeneste.innhentInntekterFor(behandling, aktørId, opplysningsPeriode,
            InntektsKilde.INNTEKT_BEREGNING, InntektsKilde.INNTEKT_SAMMENLIGNING);
        inntektArbeidYtelseTjeneste.lagre(behandling, builder);
    }

    private boolean erManuellRevurderingMedÅrsakEndringIOpplysningerOmInntekt(Behandling behandling) {
        return behandling.erRevurdering() && (behandling.erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_INNTEKT)
            || behandling.erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType.RE_KLAGE_MED_END_INNTEKT));
    }

    private boolean erEndringerIOpplysningerOmYtelse(InntektArbeidYtelseGrunnlag nyIAY, Optional<InntektArbeidYtelseGrunnlag> forrigeIAYOpt) {
        return forrigeIAYOpt.filter(forrigeIAY -> inntektArbeidYtelseTjeneste.endringPåAktørYtelse(forrigeIAY, nyIAY).erEndret()).isPresent();
    }

    private boolean erEndretInntektsmelding(InntektArbeidYtelseGrunnlag nyIAY, Optional<InntektArbeidYtelseGrunnlag> forrigeIAY) {
        return forrigeIAY.filter(inntektArbeidYtelseGrunnlag -> inntektArbeidYtelseTjeneste.erEndretInntektsmelding(inntektArbeidYtelseGrunnlag, nyIAY)).isPresent();
    }

    private boolean erEndringIInntekt(InntektArbeidYtelseGrunnlag sisteGrunnlagIOrginalBehandling, InntektArbeidYtelseGrunnlag nyIAY) {
        Optional<InntektsmeldingAggregat> originaleInntektsmeldingOpt = sisteGrunnlagIOrginalBehandling.getInntektsmeldinger();
        Optional<InntektsmeldingAggregat> inntektsmeldingOpt = nyIAY.getInntektsmeldinger();
        if (!inntektsmeldingOpt.isPresent()) {
            return false;
        }
        if (!originaleInntektsmeldingOpt.isPresent()) {
            return true;
        }

        List<Inntektsmelding> inntektsmeldinger = inntektsmeldingOpt.get().getInntektsmeldinger();
        List<Inntektsmelding> originaleInntektsmeldinger = originaleInntektsmeldingOpt.get().getInntektsmeldinger();
        return matchOgSjekkOmEndringIInntekt(originaleInntektsmeldinger, inntektsmeldinger);
    }

    private boolean matchOgSjekkOmEndringIInntekt(List<Inntektsmelding> originaleInntektsmeldinger, List<Inntektsmelding> inntektsmeldinger) {
        if (originaleInntektsmeldinger.size() != inntektsmeldinger.size()) {
            return true;
        }
        if (matchSeparateInntektsmeldingMedSeparateOgSjekkEndring(originaleInntektsmeldinger, inntektsmeldinger)) {
            return true;
        }
        return matchFellesInntektsmeldingMedFellesOgSjekkEndring(originaleInntektsmeldinger, inntektsmeldinger);
    }


    private boolean matchSeparateInntektsmeldingMedSeparateOgSjekkEndring(List<Inntektsmelding> inntektsmeldinger1, List<Inntektsmelding> inntektsmeldinger2) {
        return inntektsmeldinger1.stream().filter(Inntektsmelding::gjelderForEtSpesifiktArbeidsforhold)
            .anyMatch(inntektsmeldingForArbeidsforhold -> {
                Optional<Inntektsmelding> korresponderendeIM = inntektsmeldinger2.stream()
                    .filter(im -> im.gjelderForEtSpesifiktArbeidsforhold() && im.getVirksomhet().equals(inntektsmeldingForArbeidsforhold.getVirksomhet()) &&
                        im.getArbeidsforholdRef().gjelderFor(inntektsmeldingForArbeidsforhold.getArbeidsforholdRef()))
                    .findFirst();
                if (!korresponderendeIM.isPresent()) {
                    return true;
                }
                return korresponderendeIM.get().getInntektBeløp().compareTo(inntektsmeldingForArbeidsforhold.getInntektBeløp()) != 0;
            });
    }

    private boolean matchFellesInntektsmeldingMedFellesOgSjekkEndring(List<Inntektsmelding> inntektsmeldinger1, List<Inntektsmelding> inntektsmeldinger2) {
        return inntektsmeldinger1.stream().filter(im -> !im.gjelderForEtSpesifiktArbeidsforhold())
            .anyMatch(inntektsmeldingForArbeidsforhold -> {
                Optional<Inntektsmelding> korresponderendeIM = inntektsmeldinger2.stream()
                    .filter(im -> !im.gjelderForEtSpesifiktArbeidsforhold() && im.getVirksomhet().equals(inntektsmeldingForArbeidsforhold.getVirksomhet()))
                    .findFirst();
                return korresponderendeIM.map(inntektsmelding -> inntektsmelding.getInntektBeløp().getVerdi().compareTo(inntektsmeldingForArbeidsforhold.getInntektBeløp().getVerdi()) != 0)
                    .orElse(true);
            });
    }

    private boolean endringerForSisteYtelsesPeriodeFørSjæringstidspunkt(Behandling behandling, InntektArbeidYtelseGrunnlag nyIAY, Optional<InntektArbeidYtelseGrunnlag> forrigeIAYOpt, LocalDate skjæringstidspunkt) {
        Function<InntektArbeidYtelseGrunnlag, Optional<Ytelse>> finnSisteYtelse = iay -> getAktørYtelseFørSkjæringstidspunktetStream(iay, behandling, skjæringstidspunkt)
            .max(Comparator.comparing(ay -> ay.getPeriode().getFomDato()));

        Optional<Ytelse> sisteYtelse = finnSisteYtelse.apply(nyIAY);
        Optional<Ytelse> sisteYtelseForrigeIAY = forrigeIAYOpt.flatMap(finnSisteYtelse);

        return !sisteYtelse.equals(sisteYtelseForrigeIAY);
    }

    private boolean erEndringerIYtelseSisteTiMåneder(Behandling behandling, InntektArbeidYtelseGrunnlag nyIAY, Optional<InntektArbeidYtelseGrunnlag> forrigeIAY, LocalDate skjæringstidspunkt) {
        Function<InntektArbeidYtelseGrunnlag, List<Ytelse>> siste10MånederMedYtelser = iay -> getAktørYtelseFørSkjæringstidspunktetStream(iay, behandling, skjæringstidspunkt)
            .filter(ytelse -> ytelse.getPeriode().getFomDato().isAfter(skjæringstidspunkt.minusMonths(10)))
            .sorted(Comparator.comparing(ytelse -> ytelse.getPeriode().getFomDato()))
            .collect(Collectors.toList());

        List<Ytelse> ytelser = siste10MånederMedYtelser.apply(nyIAY);
        List<Ytelse> forrigeYtelser = forrigeIAY.map(siste10MånederMedYtelser).orElse(Collections.emptyList());
        return !ytelser.equals(forrigeYtelser) || erEndringerIDagpengerSiste10Mnd(ytelser, forrigeYtelser);
    }

    private Stream<Ytelse> getAktørYtelseFørSkjæringstidspunktetStream(InntektArbeidYtelseGrunnlag iay, Behandling behandling, LocalDate skjæringstidspunkt) {
        return iay.getAktørYtelseFørStp(behandling.getAktørId())
            .map(AktørYtelse::getYtelser)
            .orElse(Collections.emptyList())
            .stream()
            .filter(ytelse -> !gjelderSammeFagsakIFPSAK(behandling, ytelse))
            .filter(ytelse -> ytelse.getPeriode().getFomDato().isBefore(skjæringstidspunkt));
    }

    private boolean gjelderSammeFagsakIFPSAK(Behandling behandling, Ytelse ytelse) {
        return Fagsystem.FPSAK.equals(ytelse.getKilde()) && Objects.equals(ytelse.getSaksnummer(), behandling.getFagsak().getSaksnummer());
    }

    private boolean erEndringerIDagpengerSiste10Mnd(List<Ytelse> ytelser, List<Ytelse> forrigeYtelser) {
        Function<List<Ytelse>, List<YtelseAnvist>> hentRelevanteMeldekort = ytelseListe -> ytelseListe.stream()
            .filter(ytelse -> ytelse.getRelatertYtelseType().equals(RelatertYtelseType.DAGPENGER))
            .flatMap(ytelse -> ytelse.getYtelseAnvist().stream())
            .sorted(Comparator.comparing(YtelseAnvist::getAnvistFOM))
            .collect(Collectors.toList());
        return !hentRelevanteMeldekort.apply(ytelser).equals(hentRelevanteMeldekort.apply(forrigeYtelser));
    }

    private boolean brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag, Opptjening opptjening) {
        boolean erMoren = RelasjonsRolleType.MORA.equals(behandling.getRelasjonsRolleType());
        if (!erMoren) {
            return false;
        }
        boolean harDagpengerStatus = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(bgp -> bgp.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .anyMatch(andel -> AktivitetStatus.DAGPENGER.equals(andel.getAktivitetStatus()));
        if (!harDagpengerStatus) {
            return false;
        }
        boolean harStatusTY = beregningsgrunnlag.getAktivitetStatuser().stream().anyMatch(status -> AktivitetStatus.TILSTØTENDE_YTELSE.equals(status.getAktivitetStatus()));
        if (harStatusTY) {
            boolean ytelseErSykepenger = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .flatMap(bgp -> bgp.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .filter(andel -> Objects.equals(andel.getAktivitetStatus(), AktivitetStatus.DAGPENGER))
                .anyMatch(andel -> Objects.equals(andel.getYtelse(), RelatertYtelseType.SYKEPENGER));
            return ytelseErSykepenger && opptjening.getOpptjeningAktivitet().stream()
                .filter(aktivitet -> !OpptjeningAktivitetType.SYKEPENGER.equals(aktivitet.getAktivitetType()))
                .anyMatch(aktivitet -> !OpptjeningAktivitetType.DAGPENGER.equals(aktivitet.getAktivitetType()));
        }
        return opptjening.getOpptjeningAktivitet().stream().anyMatch(aktivitet -> !OpptjeningAktivitetType.DAGPENGER.equals(aktivitet.getAktivitetType()));
    }

    private boolean erEndringISkjæringstidspunktForBeregning(Beregningsgrunnlag nyttBG, Optional<Beregningsgrunnlag> forrigeBGOpt) {
        return forrigeBGOpt.filter(forrigeBG -> !Objects.equals(nyttBG.getSkjæringstidspunkt(), forrigeBG.getSkjæringstidspunkt())).isPresent();
    }

    private boolean erEndringIOpptjeningsAktiviteter(Opptjening nyOpptjening, Optional<Opptjening> forrigeOpptjeningOpt) {
        return forrigeOpptjeningOpt.filter(forrigeOpptjening -> erUlikeOpptjeningsAktiviteter(nyOpptjening, forrigeOpptjening)).isPresent();
    }

    //TODO : FIX
    private boolean erUlikeOpptjeningsAktiviteter(Opptjening nyOpptjening, Opptjening forrigeOpptjening) {
        if (nyOpptjening.getOpptjeningAktivitet().size() != forrigeOpptjening.getOpptjeningAktivitet().size()) {
            return true;
        }
        List<OpptjeningAktivitet> a = new ArrayList<>(nyOpptjening.getOpptjeningAktivitet());
        List<OpptjeningAktivitet> b = new ArrayList<>(forrigeOpptjening.getOpptjeningAktivitet());
        Comparator<OpptjeningAktivitet> fomComparator = Comparator.comparing(OpptjeningAktivitet::getFom);
        a.sort(fomComparator);
        b.sort(fomComparator);
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Beregningsgrunnlag> hentGjeldendeBeregningsgrunnlag(Behandling behandling) {
        Optional<Behandling> forrigeBehandlingOpt = behandling.getOriginalBehandling();
        Optional<Beregningsgrunnlag> fastsattBG = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.FASTSATT)
            .map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
        Optional<Beregningsgrunnlag> forrigeBGOpt = forrigeBehandlingOpt.flatMap(beregningsgrunnlagRepository::hentBeregningsgrunnlag);

        return hentGjeldendeBeregningsgrunnlag(fastsattBG, forrigeBGOpt);
    }

    @Override
    public boolean brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        Optional<Opptjening> opptjening = opptjeningRepository.finnOpptjening(behandling);
        if (!beregningsgrunnlagOpt.isPresent() || !opptjening.isPresent()) {
            return false;
        }
        return brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(behandling, beregningsgrunnlagOpt.get(), opptjening.get());
    }

    private Optional<Beregningsgrunnlag> hentGjeldendeBeregningsgrunnlag(Optional<Beregningsgrunnlag> nyttBG, Optional<Beregningsgrunnlag> forrigeBGOpt) {
        if (nyttBG.filter(this::beregningsgrunnlagErGjeldende).isPresent()) {
            return nyttBG;
        }
        return forrigeBGOpt.filter(this::beregningsgrunnlagErGjeldende);
    }

    private boolean beregningsgrunnlagErGjeldende(Beregningsgrunnlag beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .anyMatch(bgp -> bgp.getRedusertPrÅr() != null);
    }
}
