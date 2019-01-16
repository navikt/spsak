package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.domene.typer.AktørId;

@ApplicationScoped
public class HentGrunnlagsdataTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste;

    HentGrunnlagsdataTjeneste() {
        //CDI-proxy
    }

    @Inject
    public HentGrunnlagsdataTjeneste(ResultatRepositoryProvider repositoryProvider,
                                     OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste,
                                     InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                     @FagsakYtelseTypeRef("FP") IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste) {
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.opptjeningInntektArbeidYtelseTjeneste = opptjeningInntektArbeidYtelseTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.iayRegisterInnhentingTjeneste = iayRegisterInnhentingTjeneste;
    }

    public boolean vurderOmNyesteGrunnlagsdataSkalHentes(Behandling behandling) {
        Optional<Behandling> forrigeBehandlingOpt = behandling.getOriginalBehandling();
        Optional<Beregningsgrunnlag> fastsattBG = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.FASTSATT)
            .map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
        Optional<Beregningsgrunnlag> forrigeBGOpt = forrigeBehandlingOpt.flatMap(beregningsgrunnlagRepository::hentBeregningsgrunnlag);

        Optional<Beregningsgrunnlag> gjeldendeBeregningsgrunnlag = hentGjeldendeBeregningsgrunnlag(fastsattBG, forrigeBGOpt);
        if (gjeldendeBeregningsgrunnlag.isEmpty()) {
            return true;
        }
        Beregningsgrunnlag gjeldendeBG = gjeldendeBeregningsgrunnlag.get();
        Opptjening nyOpptjening = opptjeningInntektArbeidYtelseTjeneste.hentOpptjening(behandling);
        Optional<Opptjening> forrigeOpptjeningOpt = forrigeBehandlingOpt.flatMap(opptjeningInntektArbeidYtelseTjeneste::hentOpptjeningHvisEksisterer);

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
            if (endringerForSisteYtelsesPeriodeFørSjæringstidspunkt(behandling, nyIAY, forrigeIAY, gjeldendeBG.getSkjæringstidspunkt())) {
                return true;
            }
        }
        if (erEndretInntektsmelding(nyIAY, forrigeIAY)) {
            InntektArbeidYtelseGrunnlag førsteVersjonIAY = inntektArbeidYtelseTjeneste.hentFørsteVersjon(behandling);
            return erEndringIInntekt(førsteVersjonIAY, nyIAY);
        }

        return false;
    }


    public void innhentInntektsInformasjonBeregningOgSammenligning(Behandling behandling) {
        Interval opplysningsPeriode = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(behandling);
        AktørId aktørId = behandling.getAktørId();
        final InntektArbeidYtelseAggregatBuilder builder = iayRegisterInnhentingTjeneste.innhentInntekterFor(behandling, aktørId, opplysningsPeriode,
            InntektsKilde.INNTEKT_BEREGNING, InntektsKilde.INNTEKT_SAMMENLIGNING);
        inntektArbeidYtelseTjeneste.lagre(behandling, builder);
    }

    private boolean erManuellRevurderingMedÅrsakEndringIOpplysningerOmInntekt(Behandling behandling) {
        return behandling.erRevurdering() && (behandling.erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_INNTEKT));
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
        if (inntektsmeldingOpt.isEmpty()) {
            return false;
        }
        if (originaleInntektsmeldingOpt.isEmpty()) {
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

    /**
         * Henter ut siste gjeldende beregningsgrunnlag for gitt behandling eller forrige behandling (f. eks om det er
         * en revurdering). Et gjeldene beregningsgrunnlag er ferdig beregnet og skal da være avkortet og redusert.
         * @return Optional med gjeldene BG. Tom optional om ikke eksisterer.
         */
    public Optional<Beregningsgrunnlag> hentGjeldendeBeregningsgrunnlag(Behandling behandling) {
        Optional<Behandling> forrigeBehandlingOpt = behandling.getOriginalBehandling();
        Optional<Beregningsgrunnlag> fastsattBG = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.FASTSATT)
            .map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
        Optional<Beregningsgrunnlag> forrigeBGOpt = forrigeBehandlingOpt.flatMap(beregningsgrunnlagRepository::hentBeregningsgrunnlag);

        return hentGjeldendeBeregningsgrunnlag(fastsattBG, forrigeBGOpt);
    }

    private Optional<Beregningsgrunnlag> hentGjeldendeBeregningsgrunnlag(Optional<Beregningsgrunnlag> nyttBG, Optional<Beregningsgrunnlag> forrigeBGOpt) {
        if (nyttBG.map(this::beregningsgrunnlagErGjeldende).orElse(false)) {
            return nyttBG;
        }
        return forrigeBGOpt.filter(this::beregningsgrunnlagErGjeldende);
    }

    private boolean beregningsgrunnlagErGjeldende(Beregningsgrunnlag beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .anyMatch(bgp -> bgp.getRedusertPrÅr() != null);
    }
}
