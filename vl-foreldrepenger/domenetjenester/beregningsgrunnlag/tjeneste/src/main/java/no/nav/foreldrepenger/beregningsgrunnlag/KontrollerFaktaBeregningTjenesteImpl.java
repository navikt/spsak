package no.nav.foreldrepenger.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Refusjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

@ApplicationScoped
public class KontrollerFaktaBeregningTjenesteImpl implements KontrollerFaktaBeregningTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;
    private BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste;

    public KontrollerFaktaBeregningTjenesteImpl() {
        // For CDI
    }

    @Inject
    public KontrollerFaktaBeregningTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                                InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste,
                                                BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste) {
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.hentGrunnlagsdataTjeneste = hentGrunnlagsdataTjeneste;
        this.beregningInntektsmeldingTjeneste = beregningInntektsmeldingTjeneste;
    }


    @Override
    public Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> hentAndelerForKortvarigeArbeidsforhold(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (beregningsgrunnlagOpt.isPresent()) {
            Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagOpt.get();
            List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
            if (!beregningsgrunnlagPerioder.isEmpty()) {
                // beregningsgrunnlagPerioder er sortert, tar utgangspunkt i første
                BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = beregningsgrunnlagPerioder.get(0);
                Collection<Yrkesaktivitet> kortvarigeArbeidsforhold = hentKortvarigeYrkesaktiviteter(behandling, beregningsgrunnlag);
                return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(prStatus -> prStatus.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
                    .filter(andel -> finnKorresponderendeYrkesaktivitet(
                        kortvarigeArbeidsforhold,
                        andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver),
                        andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isPresent())
                    .collect(Collectors.toMap(Function.identity(),
                        andel -> finnKorresponderendeYrkesaktivitet(
                            kortvarigeArbeidsforhold,
                            andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver),
                            andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).get()));
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public boolean erNyIArbeidslivetMedAktivitetStatusSN(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (!beregningsgrunnlagOpt.isPresent()) {
            return false;
        }
        boolean erSN = beregningsgrunnlagOpt.get().getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .anyMatch(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende());

        return erSN
            && inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling)
            .flatMap(InntektArbeidYtelseGrunnlag::getOppgittOpptjening)
            .map(OppgittOpptjening::getEgenNæring)
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch(EgenNæring::getNyIArbeidslivet);
    }

    @Override
    public boolean vurderManuellBehandlingForEndretBeregningsgrunnlag(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (!beregningsgrunnlagOpt.isPresent()) {
            return false;
        }
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagOpt.get();
        List<BeregningsgrunnlagPeriode> gradertePerioder = finnPerioderMedGradering(beregningsgrunnlag, behandling);
        List<BeregningsgrunnlagPeriode> periodeMedRefusjonskrav = finnPerioderMedRefusjonskrav(beregningsgrunnlag, behandling);
        if (gradertePerioder.isEmpty() && periodeMedRefusjonskrav.isEmpty()) {
            return false;
        }
        return vurderManuellBehandlingForGraderingEllerEndretRefusjon(behandling, beregningsgrunnlag);
    }

    private boolean vurderManuellBehandlingForGraderingEllerEndretRefusjon(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            if (vurderManuellBehandlingForPeriode(behandling, beregningsgrunnlag.getSkjæringstidspunkt(), periode))
                return true;
        }
        return false;
    }

    private boolean vurderManuellBehandlingForPeriode(Behandling behandling, LocalDate skjæringstidspunkt, BeregningsgrunnlagPeriode periode) {
        for (BeregningsgrunnlagPrStatusOgAndel andel : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            Optional<Inntektsmelding> inntektsmeldingForAndelOpt = hentInntektsmeldingForAndel(behandling, andel);
            if (inntektsmeldingForAndelOpt.isPresent() && vurderManuellBehandlingForAndel(behandling, skjæringstidspunkt, periode, andel)) {
                return true;
            }
        }
        return false;
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndel> finnAndelIGjeldendeGrunnlag(BeregningsgrunnlagPeriode periode, Optional<Beregningsgrunnlag> gjeldendeBeregningsgrunnlag,
                                                                                    BeregningsgrunnlagPrStatusOgAndel andel) {
        if (!gjeldendeBeregningsgrunnlag.isPresent()) {
            return Optional.empty();
        }
        BeregningsgrunnlagPeriode korresponderendePeriodeIGjeldendeBG = finnPeriodeIBeregningsgrunnlag(periode, gjeldendeBeregningsgrunnlag.get());
        return korresponderendePeriodeIGjeldendeBG.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andelIGjeldendeGrunnlag -> andelIGjeldendeGrunnlag.gjelderSammeArbeidsforhold(andel))
            .findFirst();
    }

    private BeregningsgrunnlagPeriode finnPeriodeIBeregningsgrunnlag(BeregningsgrunnlagPeriode periode, Beregningsgrunnlag gjeldendeBeregningsgrunnlag) {

        if (periode.getBeregningsgrunnlagPeriodeFom().isBefore(gjeldendeBeregningsgrunnlag.getSkjæringstidspunkt())) {
            return gjeldendeBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .min(Comparator.comparing(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPeriodeFom))
                .orElseThrow(() -> new IllegalStateException("Fant ingen perioder i beregningsgrunnlag."));
        }

        return gjeldendeBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .filter(bgPeriode -> inkludererBeregningsgrunnlagPeriodeDato(bgPeriode, periode.getBeregningsgrunnlagPeriodeFom()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Finner ingen korresponderende periode i det fastsatte grunnlaget"));
    }

    @Override
    public boolean vurderManuellBehandlingForAndel(Behandling behandling, LocalDate skjæringstidspunkt, BeregningsgrunnlagPeriode periode,
                                                   BeregningsgrunnlagPrStatusOgAndel andel) {
        boolean harGraderingIBGPeriode = !hentGraderingerForAndelIPeriode(behandling, periode.getPeriode(), andel).isEmpty();
        boolean harRefusjonIPerioden = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null) != null && andel.getBgAndelArbeidsforhold().get().getRefusjonskravPrÅr().compareTo(BigDecimal.ZERO) != 0;
        if (harGraderingIBGPeriode || harRefusjonIPerioden) {
            Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling);
            Optional<Beregningsgrunnlag> gjeldendeBeregningsgrunnlagOpt = bg.getGjeldendeBeregningsgrunnlag();
            if (harGraderingIBGPeriode && !harRefusjonIPerioden) {
                return vurderManuellBehandlingForGraderingogIkkjeEndretRefusjon(behandling, skjæringstidspunkt, andel, periode, gjeldendeBeregningsgrunnlagOpt);
            } else {
                return vurderManuellBehandlingForRefusjonMedEllerUtenGradering(behandling, skjæringstidspunkt, periode, andel, gjeldendeBeregningsgrunnlagOpt);
            }
        }
        return false;
    }

    private boolean vurderManuellBehandlingForRefusjonMedEllerUtenGradering(Behandling behandling, LocalDate skjæringstidspunkt, BeregningsgrunnlagPeriode periode, BeregningsgrunnlagPrStatusOgAndel andel, Optional<Beregningsgrunnlag> gjeldendeBeregningsgrunnlagOpt) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelIGjeldendeGrunnlag = finnAndelIGjeldendeGrunnlag(periode, gjeldendeBeregningsgrunnlagOpt, andel);
        if (tilkomArbeidsforholdEtterStp(behandling, skjæringstidspunkt, andel)) {
            if (!andelIGjeldendeGrunnlag.isPresent()) {
                return true;
            }
            if (andelIGjeldendeGrunnlag.get().getBruttoPrÅr() == null) {
                return true;
            }
        } else {
            if (!andelIGjeldendeGrunnlag.isPresent()) {
                return false;
            }
            if (andelIGjeldendeGrunnlag.get().getBruttoPrÅr() == null) {
                return false;
            }
        }
        return !erGjeldendeBruttoBGForAndelStørreEnnNull(andelIGjeldendeGrunnlag.get());
    }

    @Override
    public Optional<BeregningsgrunnlagPrStatusOgAndel> hentKorresponderendeAndelIGjeldendeBeregningsgrunnlag(Behandling behandling, BeregningsgrunnlagPeriode periode, BeregningsgrunnlagPrStatusOgAndel andelINyttBG) {
        Optional<Beregningsgrunnlag> bg = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (!bg.isPresent()) {
            return Optional.empty();
        }
        Optional<Beregningsgrunnlag> gjeldendeBeregningsgrunnlagOpt = bg.get().getGjeldendeBeregningsgrunnlag();
        if (!gjeldendeBeregningsgrunnlagOpt.isPresent()) {
            return Optional.empty();
        }
        Beregningsgrunnlag gjeldendeBeregningsgrunnlag = gjeldendeBeregningsgrunnlagOpt.get();
        BeregningsgrunnlagPeriode periodeIGjeldendeBG = finnPeriodeIBeregningsgrunnlag(periode, gjeldendeBeregningsgrunnlag);

        List<BeregningsgrunnlagPrStatusOgAndel> matcherUtenomInntektskategori = periodeIGjeldendeBG.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andelIGjeldendeGrunnlag -> andelIGjeldendeGrunnlag.matchUtenInntektskategori(andelINyttBG)).collect(Collectors.toList());

        if (matcherUtenomInntektskategori.isEmpty()) {
            return Optional.empty();
        } else if (matcherUtenomInntektskategori.size() == 1) {
            return Optional.of(matcherUtenomInntektskategori.get(0));
        }

        return periodeIGjeldendeBG.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andelIGjeldendeGrunnlag -> andelIGjeldendeGrunnlag.equals(andelINyttBG))
            .findFirst();
    }

    @Override
    public Optional<BeregningsgrunnlagPrStatusOgAndel> hentKorresponderendeAndelIOriginaltBeregningsgrunnlag(Behandling behandling, BeregningsgrunnlagPeriode periode, BeregningsgrunnlagPrStatusOgAndel andelINyttBG) {
        if (behandling.erRevurdering()) {
            Behandling originalBehandling = behandling.getOriginalBehandling().orElseThrow(() -> new IllegalStateException("Revurdering uten originalbehandling"));
            Optional<Beregningsgrunnlag> beregningsgrunnlagOriginalBehandlingOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(originalBehandling);
            if (beregningsgrunnlagOriginalBehandlingOpt.isPresent()) {
                BeregningsgrunnlagPeriode periodeIOriginaltBG = finnPeriodeIBeregningsgrunnlag(periode, beregningsgrunnlagOriginalBehandlingOpt.get());
                return periodeIOriginaltBG.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(andelIGjeldendeGrunnlag -> andelIGjeldendeGrunnlag.equals(andelINyttBG))
                    .findFirst();
            }
        }
        return hentKorresponderendeAndelIGjeldendeBeregningsgrunnlag(behandling, periode, andelINyttBG);
    }

    @Override
    public List<Gradering> hentGraderingerForAndelIPeriode(Behandling behandling, ÅpenDatoIntervallEntitet periode, BeregningsgrunnlagPrStatusOgAndel andel) {
        Optional<Inntektsmelding> inntektsmeldingForAndelOpt = hentInntektsmeldingForAndel(behandling, andel);
        if (inntektsmeldingForAndelOpt.isPresent()) {
            Inntektsmelding inntektsmelding = inntektsmeldingForAndelOpt.get();
            return inntektsmelding.getGraderinger().stream().filter(gradering -> periode.overlapper(gradering.getPeriode())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Gradering> hentGraderingerForAndel(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel andel) {
        Optional<Inntektsmelding> inntektsmeldingOpt = hentInntektsmeldingForAndel(behandling, andel);
        if (inntektsmeldingOpt.isPresent()) {
            Inntektsmelding inntektsmelding = inntektsmeldingOpt.get();
            return inntektsmelding.getGraderinger();
        }
        return Collections.emptyList();
    }

    private boolean vurderManuellBehandlingForGraderingogIkkjeEndretRefusjon(Behandling behandling, LocalDate skjæringstidspunkt,
                                                                             BeregningsgrunnlagPrStatusOgAndel gradertAndel, BeregningsgrunnlagPeriode periode,
                                                                             Optional<Beregningsgrunnlag> gjeldendeBeregningsgrunnlag) {
        boolean tilkomEtterSkjæringstidspunkt = tilkomArbeidsforholdEtterStp(behandling, skjæringstidspunkt, gradertAndel);
        if (tilkomEtterSkjæringstidspunkt) {
            return true;
        }
        boolean refusjonErStørreEnn6G = erTotalRefusjonIPeriodenStørreEnn6G(periode, behandling);
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelIGjeldendeGrunnlag = finnAndelIGjeldendeGrunnlag(periode, gjeldendeBeregningsgrunnlag, gradertAndel);
        if (!andelIGjeldendeGrunnlag.isPresent()) {
            return refusjonErStørreEnn6G;
        }
        boolean erBruttoBGStørreEnnNull = erGjeldendeBruttoBGForAndelStørreEnnNull(andelIGjeldendeGrunnlag.get());
        return (!erBruttoBGStørreEnnNull && refusjonErStørreEnn6G) || erBGAndelAvkortetTilNull(andelIGjeldendeGrunnlag.get());
    }

    private boolean erTotalRefusjonIPeriodenStørreEnn6G(BeregningsgrunnlagPeriode periode, Behandling behandling) {
        return beregningInntektsmeldingTjeneste.erTotaltRefusjonskravStørreEnnSeksG(behandling, periode.getBeregningsgrunnlag(), periode.getPeriode().getFomDato());
    }

    @Override
    public boolean tilkomArbeidsforholdEtterStp(Behandling behandling, LocalDate skjæringstidspunkt, BeregningsgrunnlagPrStatusOgAndel andel) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (!inntektArbeidYtelseGrunnlagOpt.isPresent()) {
            return false;
        }
        Optional<AktørArbeid> aktørArbeidEtterOpt = inntektArbeidYtelseGrunnlagOpt.get().getAktørArbeidEtterStp(behandling.getAktørId());
        Optional<AktørArbeid> aktørArbeidFørOpt = inntektArbeidYtelseGrunnlagOpt.get().getAktørArbeidEtterStp(behandling.getAktørId());

        if (!aktørArbeidEtterOpt.isPresent()) {
            return false;
        }
        if (!aktørArbeidFørOpt.isPresent()) {
            return true;
        }
        return aktørArbeidEtterOpt.get().getYrkesaktiviteter().stream()
            .anyMatch(aktivitet -> andel.gjelderSammeArbeidsforhold(aktivitet)
                && harKunAvtalerMedStartEtterSkjæringstidspunkt(skjæringstidspunkt, aktivitet, aktørArbeidFørOpt.get().getYrkesaktiviteter()));
    }

    private boolean harKunAvtalerMedStartEtterSkjæringstidspunkt(LocalDate skjæringstidspunkt, Yrkesaktivitet aktivitet, Collection<Yrkesaktivitet> aktiviteterFørStp) {
        return aktivitet.getAktivitetsAvtaler().stream().noneMatch(avtale -> avtale.getFraOgMed().isBefore(skjæringstidspunkt)) &&
            arbeidsforholdEksisterteFørStp(aktivitet, aktiviteterFørStp);
    }

    private boolean arbeidsforholdEksisterteFørStp(Yrkesaktivitet yrkesaktivitet, Collection<Yrkesaktivitet> aktiviteterFørStp) {
        return !yrkesaktivitet.getArbeidsforholdRef().isPresent() || aktiviteterFørStp.stream()
            .anyMatch(aktivitet -> aktivitet.getArbeidsforholdRef().isPresent() && aktivitet.getArbeidsforholdRef().get().equals(yrkesaktivitet.getArbeidsforholdRef().get()));
    }

    private boolean finnesOverlapp(BeregningsgrunnlagPeriode periode, DatoIntervallEntitet intervall) {
        return periode.getPeriode().overlapper(intervall);
    }

    private boolean inkludererBeregningsgrunnlagPeriodeDato(BeregningsgrunnlagPeriode periode, LocalDate dato) {
        return !periode.getBeregningsgrunnlagPeriodeFom().isAfter(dato) && (periode.getBeregningsgrunnlagPeriodeTom() == null || !periode.getBeregningsgrunnlagPeriodeTom().isBefore(dato));
    }

    @Override
    public boolean erGjeldendeBruttoBGForAndelStørreEnnNull(BeregningsgrunnlagPrStatusOgAndel andel) {
        return andel.getBruttoPrÅr() != null && andel.getBruttoPrÅr().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean erBGAndelAvkortetTilNull(BeregningsgrunnlagPrStatusOgAndel gradertAndel) {
        return gradertAndel.getAvkortetPrÅr() != null && gradertAndel.getAvkortetPrÅr().compareTo(BigDecimal.ZERO) == 0;
    }

    private Optional<Periode> hentRefusjonsperiode(Behandling behandling, Inntektsmelding inntektsmelding) {
        boolean refusjonFraStart = !Optional.ofNullable(inntektsmelding.getRefusjonBeløpPerMnd()).orElse(Beløp.ZERO).erNullEllerNulltall();
        Optional<LocalDate> startdatoRefusjonEtterOppstart = inntektsmelding.getEndringerRefusjon().stream()
            .filter(er -> er.getRefusjonsbeløp() != null && !er.getRefusjonsbeløp().erNullEllerNulltall())
            .map(Refusjon::getFom)
            .min(Comparator.naturalOrder());
        boolean refusjonEtterhvert = startdatoRefusjonEtterOppstart.isPresent();
        if (!refusjonFraStart && !refusjonEtterhvert) {
            return Optional.empty();
        }
        LocalDate sisteDagMedRefusjon = inntektsmelding.getRefusjonOpphører();
        if (refusjonFraStart) {
            LocalDate startdatoRefusjon = beregningInntektsmeldingTjeneste.fastsettStartdatoInntektsmelding(behandling, inntektsmelding);
            return Optional.of(new Periode(startdatoRefusjon, sisteDagMedRefusjon));
        }
        LocalDate startdatoRefusjon = startdatoRefusjonEtterOppstart.get();
        return Optional.of(new Periode(startdatoRefusjon, sisteDagMedRefusjon));
    }

    @Override
    public Optional<Inntektsmelding> hentInntektsmeldingForAndel(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel andel) {
        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldinger(behandling);
        return inntektsmeldinger.stream()
            .filter(inntektsmelding ->
                andel.gjelderSammeArbeidsforhold(inntektsmelding.getVirksomhet(), inntektsmelding.getArbeidsforholdRef()))
            .findFirst();
    }

    @Override
    public Optional<Periode> hentRefusjonsPeriodeForAndel(Behandling behandling, BeregningsgrunnlagPrStatusOgAndel andel) {
        Optional<Inntektsmelding> inntektsmeldingOpt = hentInntektsmeldingForAndel(behandling, andel);
        if (inntektsmeldingOpt.isPresent()) {
            Inntektsmelding inntektsmelding = inntektsmeldingOpt.get();
            if (inntektsmelding.getRefusjonBeløpPerMnd() == null || inntektsmelding.getRefusjonOpphører() == null) {
                return Optional.empty();
            }
            return hentRefusjonsperiode(behandling, inntektsmelding);
        }
        return Optional.empty();
    }

    private List<BeregningsgrunnlagPeriode> finnPerioderMedGradering(Beregningsgrunnlag beregningsgrunnlag, Behandling behandling) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().filter(periode -> harPeriodeGradering(periode, behandling))
            .collect(Collectors.toList());
    }

    @Override
    public boolean harPeriodeGradering(BeregningsgrunnlagPeriode periode, Behandling behandling) {
        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldinger(behandling);
        List<Gradering> graderingerForBehandling = inntektsmeldinger.stream().flatMap(im -> im.getGraderinger().stream()).collect(Collectors.toList());
        return graderingerForBehandling.stream().anyMatch(gradering -> finnesOverlapp(periode, gradering.getPeriode()));
    }

    private List<BeregningsgrunnlagPeriode> finnPerioderMedRefusjonskrav(Beregningsgrunnlag beregningsgrunnlag, Behandling behandling) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().filter(periode -> harPeriodeRefusjonskrav(periode, behandling))
            .collect(Collectors.toList());
    }

    @Override
    public boolean harPeriodeRefusjonskrav(BeregningsgrunnlagPeriode periode, Behandling behandling) {
        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldinger(behandling);
        return inntektsmeldinger.stream()
            .anyMatch(im -> im.getRefusjonBeløpPerMnd() != null && !periode.getPeriode().getFomDato().isAfter(im.getRefusjonOpphører()));
    }

    @Override
    public boolean brukerHarHattLønnsendringOgManglerInntektsmelding(Behandling behandling) {
        List<Yrkesaktivitet> aktiviteterMedLønnsendringUtenIM = finnAlleAktiviteterMedLønnsendringUtenInntektsmelding(behandling);
        return !aktiviteterMedLønnsendringUtenIM.isEmpty();
    }

    @Override
    public List<Yrkesaktivitet> finnAlleAktiviteterMedLønnsendringUtenInntektsmelding(Behandling behandling) {
        ArrayList<Yrkesaktivitet> aktiviteterMedLønnsendringUtenIM = new ArrayList<>();
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (!inntektArbeidYtelseGrunnlagOpt.isPresent()) {
            return aktiviteterMedLønnsendringUtenIM;
        }
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseGrunnlagOpt.get();
        Optional<Beregningsgrunnlag> beregningsgrunnlagOptional = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        List<InntektsmeldingSomIkkeKommer> manglendeInntektsmeldinger = inntektArbeidYtelseGrunnlag.getInntektsmeldingerSomIkkeKommer();
        if (!beregningsgrunnlagOptional.isPresent() || manglendeInntektsmeldinger.isEmpty()) {
            return aktiviteterMedLønnsendringUtenIM;
        }
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagOptional.get();
        Optional<AktørArbeid> aktørArbeidFørStpOpt = inntektArbeidYtelseGrunnlagOpt.get().getAktørArbeidFørStp(behandling.getAktørId());
        List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerAndeler = alleArbeidstakerandeler(beregningsgrunnlag);

        if (!aktørArbeidFørStpOpt.isPresent() || arbeidstakerAndeler.isEmpty()) {
            return aktiviteterMedLønnsendringUtenIM;
        }
        // Alle arbeidstakerandeler har samme beregningsperiode, kan derfor ta fra den første
        LocalDate beregningsperiodeFom = arbeidstakerAndeler.get(0).getBeregningsperiodeFom();
        LocalDate beregningsperiodeTom = arbeidstakerAndeler.get(0).getBeregningsperiodeTom();
        if (beregningsperiodeFom == null || beregningsperiodeTom == null) {
            return Collections.emptyList();
        }
        AktørArbeid aktørArbeidFørStp = aktørArbeidFørStpOpt.get();
        Collection<Yrkesaktivitet> yrkesaktiviteter = aktørArbeidFørStp.getYrkesaktiviteter();
        Collection<Yrkesaktivitet> aktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringIBeregningsperioden(yrkesaktiviteter, beregningsperiodeFom, beregningsperiodeTom);
        if (aktiviteterMedLønnsendring.isEmpty()) {
            return aktiviteterMedLønnsendringUtenIM;
        }
        Collection<Yrkesaktivitet> matchendeAktiviteter = finnAktiviteterMedLønnsendringSomManglerIM(aktiviteterMedLønnsendring, manglendeInntektsmeldinger);
        aktiviteterMedLønnsendringUtenIM.addAll(matchendeAktiviteter);
        return aktiviteterMedLønnsendringUtenIM;
    }

    @Override
    public boolean skalHaBesteberegningForFødendeKvinne(Behandling behandling) {
        return hentGrunnlagsdataTjeneste.brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(behandling);
    }

    private Collection<Yrkesaktivitet> finnAktiviteterMedLønnsendringSomManglerIM(Collection<Yrkesaktivitet> aktiviteterMedLønnsendring, List<InntektsmeldingSomIkkeKommer> manglendeInntektsmeldinger) {
        return aktiviteterMedLønnsendring.stream()
            .filter(a -> a.getArbeidsgiver() != null && a.getArbeidsgiver().getIdentifikator() != null)
            .filter(a -> matchYrkesaktivitetMedInntektsmeldingSomIkkeKommer(manglendeInntektsmeldinger, a))
            .collect(Collectors.toList());
    }

    private boolean matchYrkesaktivitetMedInntektsmeldingSomIkkeKommer(List<InntektsmeldingSomIkkeKommer> manglendeInntektsmeldinger, Yrkesaktivitet yrkesaktivitet) {
        if (yrkesaktivitet.getArbeidsforholdRef().isPresent()) {
            return manglendeInntektsmeldinger.stream()
                .filter(im -> im.getArbeidsgiver().equals(yrkesaktivitet.getArbeidsgiver()))
                .map(InntektsmeldingSomIkkeKommer::getRef)
                .anyMatch(ref -> ref.gjelderForSpesifiktArbeidsforhold() && ref.gjelderFor(yrkesaktivitet.getArbeidsforholdRef().get()));
        }
        return manglendeInntektsmeldinger.stream()
            .map(InntektsmeldingSomIkkeKommer::getArbeidsgiver)
            .map(Arbeidsgiver::getIdentifikator)
            .anyMatch(id -> id.equals(yrkesaktivitet.getArbeidsgiver().getIdentifikator()));
    }

    private List<BeregningsgrunnlagPrStatusOgAndel> alleArbeidstakerandeler(Beregningsgrunnlag beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .map(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPrStatusOgAndelList).flatMap(Collection::stream)
            .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
            .collect(Collectors.toList());
    }

    private Collection<Yrkesaktivitet> finnAktiviteterMedLønnsendringIBeregningsperioden(Collection<Yrkesaktivitet> yrkesaktiviteter, LocalDate beregningsperiodeFom, LocalDate beregningsperiodeTom) {
        return yrkesaktiviteter
            .stream()
            .filter(aktivitet -> !ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER.equals(aktivitet.getArbeidType())
                && !ArbeidType.FRILANSER.equals(aktivitet.getArbeidType()))
            .filter(aktivitet ->
                harAvtalerMedLønnsendringIBeregningsgrunnlagperioden(aktivitet.getAktivitetsAvtaler(), beregningsperiodeFom, beregningsperiodeTom))
            .collect(Collectors.toList());
    }

    private boolean harAvtalerMedLønnsendringIBeregningsgrunnlagperioden(Collection<AktivitetsAvtale> aktivitetsAvtaler, LocalDate beregningsperiodeFom, LocalDate beregningsperiodeTom) {
        return !aktivitetsAvtaler
            .stream()
            .filter(aa -> aa.getSisteLønnsendringsdato() != null)
            .filter(aa -> aa.getSisteLønnsendringsdato().equals(beregningsperiodeFom) || aa.getSisteLønnsendringsdato().isAfter(beregningsperiodeFom))
            .filter(aa -> aa.getSisteLønnsendringsdato().equals(beregningsperiodeTom) || aa.getSisteLønnsendringsdato().isBefore(beregningsperiodeTom))
            .collect(Collectors.toList())
            .isEmpty();
    }

    @Override
    public boolean erLønnsendringIBeregningsperioden(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        return beregningsgrunnlagOpt.map(Beregningsgrunnlag::getBeregningsgrunnlagPerioder)
            .orElse(Collections.emptyList())
            .stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .anyMatch(andel -> andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::erLønnsendringIBeregningsperioden).orElse(null) != null && andel.getBgAndelArbeidsforhold().get().erLønnsendringIBeregningsperioden());
    }

    @Override
    public boolean brukerMedAktivitetStatusTY(Behandling behandling) {
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        return beregningsgrunnlagOpt.map(beregningsgrunnlag -> beregningsgrunnlag.getAktivitetStatuser().stream()
            .map(BeregningsgrunnlagAktivitetStatus::getAktivitetStatus)
            .anyMatch(AktivitetStatus.TILSTØTENDE_YTELSE::equals))
            .orElse(false);
    }

    private Optional<Yrkesaktivitet> finnKorresponderendeYrkesaktivitet(Collection<Yrkesaktivitet> kortvarigeArbeidsforhold, Optional<Arbeidsgiver> arbeidsgiverOpt, Optional<ArbeidsforholdRef> arbeidsforholdRefOpt) {
        if (arbeidsforholdRefOpt.isPresent()) {
            return kortvarigeArbeidsforhold.stream()
                .filter(ya -> ya.getArbeidsforholdRef().isPresent()
                    && ya.getArbeidsforholdRef().get().getReferanse().equals(arbeidsforholdRefOpt.get().getReferanse()))
                .findFirst();
        } else if (arbeidsgiverOpt.isPresent()) {
            return kortvarigeArbeidsforhold.stream()
                .filter(ya -> arbeidsgiverOpt.get().equals(ya.getArbeidsgiver()))
                .findFirst();
        } else return Optional.empty();
    }

    private Collection<Yrkesaktivitet> hentKortvarigeYrkesaktiviteter(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        Collection<Yrkesaktivitet> yrkesAktiviteter = hentYrkesaktiviteter(behandling);
        Collection<Yrkesaktivitet> yrkesAktiviteterOrdArb = yrkesAktiviteter.stream()
            .filter(ya -> ya.getArbeidType().equals(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)).collect(Collectors.toList());
        return yrkesAktiviteterOrdArb.stream()
            .filter(ya -> erKortvarigYrkesaktivitetSomAvsluttesEtterSkjæringstidspunkt(beregningsgrunnlag, ya))
            .collect(Collectors.toList());
    }

    private Collection<Yrkesaktivitet> hentYrkesaktiviteter(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (!inntektArbeidYtelseGrunnlagOpt.isPresent()) {
            return Collections.emptyList();
        }
        InntektArbeidYtelseAggregat bekreftet = inntektArbeidYtelseGrunnlagOpt.get().getOpplysningerFørSkjæringstidspunkt()
            .orElseThrow(() -> new IllegalStateException("Fant ingen bekreftet inntektsarbeidytelsegrunnlag for behandling " + behandling.getId()));
        Optional<AktørArbeid> aktørArbeidOpt = bekreftet.getAktørArbeid().stream()
            .filter(aktørArbeid -> aktørArbeid.getAktørId().equals(behandling.getAktørId())).findFirst();
        return aktørArbeidOpt.map(AktørArbeid::getYrkesaktiviteter).orElse(Collections.emptyList());
    }

    private boolean erKortvarigYrkesaktivitetSomAvsluttesEtterSkjæringstidspunkt(Beregningsgrunnlag beregningsgrunnlag, Yrkesaktivitet yrkesaktivitet) {
        Optional<AktivitetsAvtale> avtaleOpt = yrkesaktivitet.getAnsettelsesPeriode();
        return avtaleOpt
            .filter(avtale -> avtale.getFraOgMed().isBefore(beregningsgrunnlag.getSkjæringstidspunkt()))
            .filter(avtale -> !avtale.getTilOgMed().isBefore(beregningsgrunnlag.getSkjæringstidspunkt()))
            .filter(this::isDurationLessThan6Months).isPresent();
    }

    private boolean isDurationLessThan6Months(AktivitetsAvtale aa) {
        Period duration = aa.getFraOgMed().until(aa.getTilOgMed().plusDays(1));
        return duration.getYears() < 1 && duration.getMonths() < 6;
    }

    @Override
    public Map<Virksomhet, List<Inntektsmelding>> hentInntektsmeldingerForVirksomheter(Behandling behandling, Set<Virksomhet> virksomheter) {
        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldinger(behandling);
        return inntektsmeldinger.stream()
            .filter(im -> virksomheter.contains(im.getVirksomhet()))
            .collect(Collectors.groupingBy(Inntektsmelding::getVirksomhet));
    }
}
