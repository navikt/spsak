package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.fpsak.tidsserie.LocalDateInterval;

@ApplicationScoped
public class BeregningsgrunnlagTestUtil {

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;
    private BeregningRepository beregningRepository;
    private BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil;

    public BeregningsgrunnlagTestUtil() {
        // Inject
    }

    @Inject
    public BeregningsgrunnlagTestUtil(BehandlingRepositoryProvider repositoryProvider,
                                      InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                      HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste,
                                      BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil) {
        Objects.requireNonNull(repositoryProvider);
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.hentGrunnlagsdataTjeneste = hentGrunnlagsdataTjeneste;
        this.beregningArbeidsgiverTestUtil = beregningArbeidsgiverTestUtil;
        this.beregningRepository = repositoryProvider.getBeregningRepository();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    }

    public void leggTilFLTilknyttetOrganisasjon(Behandling behandling, String orgNr, String arbId) {
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        bg.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
                .builder()
                .medArbforholdRef(arbId)
                .medArbeidsgiver(beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgNr));
            BeregningsgrunnlagPrStatusOgAndel.builder()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .build(periode);
        });
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
    }

    public Beregningsgrunnlag lagGjeldendeBeregningsgrunnlag(Behandling behandling, LocalDate skjæringstidspunktOpptjening, AktivitetStatus... statuser) {
        HashMap<String, Integer> avkortet = new HashMap<>();
        HashMap<String, Integer> bruttoPrÅr = new HashMap<>();
        List<LocalDateInterval> perioder = Collections.singletonList(new LocalDateInterval(skjæringstidspunktOpptjening, null));
        return lagGjeldendeBeregningsgrunnlag(behandling, skjæringstidspunktOpptjening, avkortet,
            bruttoPrÅr, Collections.emptyMap(), perioder, Collections.singletonList(Collections.emptyList()), Collections.emptyMap(), statuser);
    }

    public Beregningsgrunnlag lagGjeldendeBeregningsgrunnlag(Behandling behandling, LocalDate skjæringstidspunktOpptjening, List<LocalDateInterval> berPerioder, AktivitetStatus... statuser) {
        return lagGjeldendeBeregningsgrunnlag(behandling, skjæringstidspunktOpptjening, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), berPerioder, Collections.emptyList(), Collections.emptyMap(), statuser);
    }

    public Beregningsgrunnlag lagGjeldendeBeregningsgrunnlag(Behandling behandling, LocalDate skjæringstidspunktOpptjening, List<LocalDateInterval> berPerioder, List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker, AktivitetStatus... statuser) {
        return lagGjeldendeBeregningsgrunnlag(behandling, skjæringstidspunktOpptjening, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), berPerioder, opprinneligePeriodeÅrsaker, Collections.emptyMap(), statuser);
    }

    public Beregningsgrunnlag lagGjeldendeBeregningsgrunnlag(Behandling behandling,
                                                             LocalDate skjæringstidspunktOpptjening,
                                                             Map<String, Integer> andelAvkortet,
                                                             Map<String, Integer> bruttoPrÅr,
                                                             List<LocalDateInterval> berPerioder, List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker, AktivitetStatus... statuser) {
        return lagGjeldendeBeregningsgrunnlag(behandling, skjæringstidspunktOpptjening, andelAvkortet, bruttoPrÅr, Collections.emptyMap(), berPerioder, opprinneligePeriodeÅrsaker, Collections.emptyMap(), statuser);
    }

    public Beregningsgrunnlag lagGjeldendeBeregningsgrunnlag(Behandling behandling, // NOSONAR - brukes bare til test
                                                             LocalDate skjæringstidspunktOpptjening,
                                                             Map<String, Integer> andelAvkortet,
                                                             Map<String, Integer> bruttoPrÅr,
                                                             Map<String, List<Boolean>> lagtTilAvSaksbehandler,
                                                             List<LocalDateInterval> perioder,
                                                             List<List<PeriodeÅrsak>> periodePeriodeÅrsaker,
                                                             Map<String, List<Inntektskategori>> inntektskategoriPrAndelIArbeidsforhold,
                                                             AktivitetStatus... statuser) {
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .medGrunnbeløp(getGrunnbeløp(skjæringstidspunktOpptjening))
            .medRedusertGrunnbeløp(getGrunnbeløp(skjæringstidspunktOpptjening))
            .build();

        if (statuser.length > 0) {
            byggBGForSpesifikkeAktivitetstatuser(behandling, skjæringstidspunktOpptjening, beregningsgrunnlag, statuser);
        } else {
            lagPerioder(behandling, skjæringstidspunktOpptjening, andelAvkortet, bruttoPrÅr, lagtTilAvSaksbehandler, Collections.nCopies(perioder.size(), 1000),
                Collections.nCopies(perioder.size(), 1000), perioder, beregningsgrunnlag, periodePeriodeÅrsaker, inntektskategoriPrAndelIArbeidsforhold, Collections.emptyMap());
        }
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.FASTSATT);
        return beregningsgrunnlag;
    }

    private void byggBGForSpesifikkeAktivitetstatuser(Behandling behandling, LocalDate skjæringstidspunktOpptjening, Beregningsgrunnlag beregningsgrunnlag, AktivitetStatus[] statuser) {
        BeregningsgrunnlagAktivitetStatus.Builder bgAktivitetStatusbuilder = BeregningsgrunnlagAktivitetStatus.builder();
        for (int i = 1; i < statuser.length; i++) {
            bgAktivitetStatusbuilder.medAktivitetStatus(statuser[i]);
        }
        bgAktivitetStatusbuilder.medAktivitetStatus(statuser[0]).build(beregningsgrunnlag);
        List<AktivitetStatus> enkeltstatuser = oversettTilEnkeltstatuser(statuser);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(skjæringstidspunktOpptjening, null)
            .build(beregningsgrunnlag);
        for (AktivitetStatus status : enkeltstatuser) {
            if (status.equals(AktivitetStatus.ARBEIDSTAKER) || status.equals(AktivitetStatus.TILSTØTENDE_YTELSE)) {
                continue;
            }
            BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(status);
            if (status.equals(AktivitetStatus.FRILANSER)) {
                andelBuilder.medBeregningsperiode(skjæringstidspunktOpptjening.minusMonths(3).withDayOfMonth(1), skjæringstidspunktOpptjening.withDayOfMonth(1).minusDays(1));
            }
            andelBuilder.build(periode);
        }
        InntektArbeidYtelseGrunnlag agg = inntektArbeidYtelseTjeneste.hentAggregat(behandling);
        Optional<AktørArbeid> aktørArbeidOpt = agg.getAktørArbeidFørStp(behandling.getAktørId());
        Collection<Yrkesaktivitet> aktiviteterOpt = aktørArbeidOpt.map(AktørArbeid::getYrkesaktiviteter).orElse(Collections.emptyList());
        List<Yrkesaktivitet> aktiviteter = aktiviteterOpt.stream().filter(a -> a.getArbeidsforholdRef().isPresent()).collect(Collectors.toList());
        List<String> arbeidsforholdIder = aktiviteter.stream()
            .map(Yrkesaktivitet::getArbeidsforholdRef)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ArbeidsforholdRef::getReferanse)
            .collect(Collectors.toList());
        List<String> orgNummere = aktiviteter.stream().map(a -> a.getArbeidsgiver().getVirksomhet().getOrgnr()).collect(Collectors.toList());

        for (int i = 0; i < arbeidsforholdIder.size(); i++) {
            String arbId = arbeidsforholdIder.get(i);
            String orgNr = orgNummere.get(i);

            BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold.builder()
                .medArbeidsgiver(beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgNr))
                .medArbforholdRef(arbId)
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2));

            BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(skjæringstidspunktOpptjening.minusMonths(3).withDayOfMonth(1), skjæringstidspunktOpptjening.withDayOfMonth(1).minusDays(1))
                .medBGAndelArbeidsforhold(bga)
                .build(periode);
        }
    }

    private List<AktivitetStatus> oversettTilEnkeltstatuser(AktivitetStatus... statuser) {
        List<AktivitetStatus> enkeltstatuser = new ArrayList<>();
        if (statuser.length == 0) {
            enkeltstatuser.add(AktivitetStatus.ARBEIDSTAKER);
        } else {
            Map<AktivitetStatus, List<AktivitetStatus>> kombinasjonsstatuser = new HashMap<>();
            kombinasjonsstatuser.put(AktivitetStatus.KOMBINERT_AT_FL, Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER));
            kombinasjonsstatuser.put(AktivitetStatus.KOMBINERT_AT_FL_SN, Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
            kombinasjonsstatuser.put(AktivitetStatus.KOMBINERT_AT_SN, Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
            kombinasjonsstatuser.put(AktivitetStatus.KOMBINERT_FL_SN, Arrays.asList(AktivitetStatus.FRILANSER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
            for (AktivitetStatus status : statuser) {
                if (kombinasjonsstatuser.containsKey(status)) {
                    enkeltstatuser.addAll(kombinasjonsstatuser.get(status));
                } else {
                    enkeltstatuser.add(status);
                }
            }
        }
        return enkeltstatuser;
    }


    public Beregningsgrunnlag lagForrigeBeregningsgrunnlag(Behandling behandling, // NOSONAR - brukes bare til test
                                                           LocalDate skjæringstidspunktOpptjening,
                                                           Map<String, Integer> andelAvkortet,
                                                           Map<String, Integer> bruttoPrÅr,
                                                           Map<String, List<Boolean>> lagtTilAvSaksbehandler,
                                                           List<LocalDateInterval> perioder,
                                                           List<List<PeriodeÅrsak>> periodePeriodeÅrsaker,
                                                           Map<String, List<Inntektskategori>> inntektskategoriPrAndelIArbeidsforhold) {
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .medGrunnbeløp(getGrunnbeløp(skjæringstidspunktOpptjening))
            .medRedusertGrunnbeløp(getGrunnbeløp(skjæringstidspunktOpptjening))
            .build();
        lagPerioder(behandling, skjæringstidspunktOpptjening, andelAvkortet, bruttoPrÅr, lagtTilAvSaksbehandler, Collections.nCopies(perioder.size(), 1000),
            Collections.nCopies(perioder.size(), 1000), perioder, beregningsgrunnlag, periodePeriodeÅrsaker, inntektskategoriPrAndelIArbeidsforhold, Collections.emptyMap());
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        return beregningsgrunnlag;
    }

    public Beregningsgrunnlag lagBeregningsgrunnlagForEndring(Behandling behandling, LocalDate skjæringstidspunktOpptjening) {
        return lagBeregningsgrunnlagForEndring(behandling, skjæringstidspunktOpptjening, Collections.emptyList(), Collections.singletonList(new LocalDateInterval(skjæringstidspunktOpptjening, null)));
    }

    public Beregningsgrunnlag lagBeregningsgrunnlagForEndring(Behandling behandling, LocalDate skjæringstidspunktOpptjening, List<List<PeriodeÅrsak>> periodePeriodeÅrsaker, List<LocalDateInterval> perioder) {
        HashMap<String, Integer> avkortet = new HashMap<>();
        HashMap<String, Integer> bruttoPrÅr = new HashMap<>();
        return lagBeregningsgrunnlagForEndring(behandling, skjæringstidspunktOpptjening,
            avkortet, bruttoPrÅr, periodePeriodeÅrsaker, perioder, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    public Beregningsgrunnlag lagBeregningsgrunnlagForEndring(Behandling behandling, LocalDate skjæringstidspunktOpptjening, List<List<PeriodeÅrsak>> periodePeriodeÅrsaker, List<LocalDateInterval> perioder, Map<String, Integer> refusjonPrÅr) {
        HashMap<String, Integer> avkortet = new HashMap<>();
        HashMap<String, Integer> bruttoPrÅr = new HashMap<>();
        return lagBeregningsgrunnlagForEndring(behandling, skjæringstidspunktOpptjening,
            avkortet, bruttoPrÅr, periodePeriodeÅrsaker, perioder, Collections.emptyMap(), Collections.emptyMap(), refusjonPrÅr);
    }


    public Beregningsgrunnlag lagBeregningsgrunnlagForEndring(Behandling behandling, LocalDate skjæringstidspunktOpptjening, Map<String, Integer> andelAvkortet, // NOSONAR - brukes bare til test
                                                              Map<String, Integer> bruttoPrÅr,
                                                              List<List<PeriodeÅrsak>> periodePeriodeÅrsaker,
                                                              List<LocalDateInterval> perioder,
                                                              Map<String, List<Boolean>> lagtTilAvSaksbehandlerPrAndelIArbeidsforhold,
                                                              Map<String, List<Inntektskategori>> inntektskategoriPrAndelIArbeidsforhold, Map<String, Integer> refusjonPrÅr) {
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .medGrunnbeløp(getGrunnbeløp(skjæringstidspunktOpptjening))
            .medRedusertGrunnbeløp(getGrunnbeløp(skjæringstidspunktOpptjening))
            .build();
        lagPerioder(behandling, skjæringstidspunktOpptjening, andelAvkortet, bruttoPrÅr, lagtTilAvSaksbehandlerPrAndelIArbeidsforhold, Collections.nCopies(perioder.size(), null), Collections.nCopies(perioder.size(), null),
            perioder, beregningsgrunnlag, periodePeriodeÅrsaker, inntektskategoriPrAndelIArbeidsforhold, refusjonPrÅr);
        Optional<Beregningsgrunnlag> gjeldendeBg = hentGrunnlagsdataTjeneste.hentGjeldendeBeregningsgrunnlag(behandling);
        gjeldendeBg.ifPresent(bg -> Beregningsgrunnlag.builder(beregningsgrunnlag).medGjeldendeBeregningsgrunnlag(bg).build());
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        return beregningsgrunnlag;
    }

    public void leggTilAndelLagtTilAvSaksbehandler(Behandling behandling, Beregningsgrunnlag bg, int index, String orgnr, Long andelsnrNyAndel, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Beregningsgrunnlag nyttBg = Kopimaskin.deepCopy(bg);
        BeregningsgrunnlagPeriode periode = nyttBg.getBeregningsgrunnlagPerioder().get(index);
        BeregningsgrunnlagPrStatusOgAndel matchetAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> Objects.equals(orgnr, andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null))).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Fant ikke andel i periode for oppgitt orgnr"));
        BeregningsgrunnlagPrStatusOgAndel nyAndel = Kopimaskin.deepCopy(matchetAndel);
        BeregningsgrunnlagPrStatusOgAndel.builder(nyAndel)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
            .medAndelsnr(andelsnrNyAndel).build(periode);
        beregningsgrunnlagRepository.lagre(behandling, nyttBg, beregningsgrunnlagTilstand);
    }

    private void lagPerioder(Behandling behandling, LocalDate skjæringstidspunkt, Map<String, Integer> avkortetAndel, // NOSONAR - brukes bare til test, men denne bør reskrives // TODO (TOPAS)
                             Map<String, Integer> bruttoPrÅr,
                             Map<String, List<Boolean>> lagtTilAvSaksbehandlerPrAndelIArbeidsforhold,
                             List<Integer> redusert,
                             List<Integer> avkortet,
                             List<LocalDateInterval> perioder,
                             Beregningsgrunnlag beregningsgrunnlag,
                             List<List<PeriodeÅrsak>> periodePeriodeÅrsaker,
                             Map<String, List<Inntektskategori>> inntektskategoriPrAndelIArbeidsforhold, Map<String, Integer> refusjonPrÅr) {
        BeregningsgrunnlagAktivitetStatus.Builder bgAktivitetStatusbuilder = BeregningsgrunnlagAktivitetStatus.builder();
        bgAktivitetStatusbuilder.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).build(beregningsgrunnlag);
        for (int j = 0; j < perioder.size(); j++) {
            InntektArbeidYtelseGrunnlag agg = inntektArbeidYtelseTjeneste.hentAggregat(behandling);
            BeregningsgrunnlagPeriode.Builder periodeBuilder = BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(perioder.get(j).getFomDato(), perioder.get(j).getTomDato())
                .medAvkortetPrÅr(avkortet.get(j) != null ? BigDecimal.valueOf(avkortet.get(j)) : null)
                .medRedusertPrÅr(redusert.get(j) != null ? BigDecimal.valueOf(redusert.get(j)) : null);
            if (!periodePeriodeÅrsaker.isEmpty()) {
                periodeBuilder.leggTilPeriodeÅrsaker(periodePeriodeÅrsaker.get(j));
            }
            BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = periodeBuilder.build(beregningsgrunnlag);
            Optional<AktørArbeid> aktørArbeidFørStpOpt = agg.getAktørArbeidFørStp(behandling.getAktørId());
            Collection<Yrkesaktivitet> aktiviteterFørStpOpt = aktørArbeidFørStpOpt.map(AktørArbeid::getYrkesaktiviteter).orElse(Collections.emptyList());
            List<Yrkesaktivitet> aktiviteterFørStp = aktiviteterFørStpOpt.stream().filter(a -> a.getArbeidsforholdRef().isPresent()).collect(Collectors.toList());
            Optional<AktørArbeid> aktørArbeidEtterStpOpt = agg.getAktørArbeidEtterStp(behandling.getAktørId());
            Collection<Yrkesaktivitet> aktiviteterEtterStpOpt = aktørArbeidEtterStpOpt.map(AktørArbeid::getYrkesaktiviteter).orElse(Collections.emptyList());
            List<Yrkesaktivitet> aktiviteter = aktiviteterEtterStpOpt.stream().filter(a -> a.getArbeidsforholdRef().isPresent()).collect(Collectors.toList());
            aktiviteter.addAll(aktiviteterFørStp);
            List<String> arbeidsforholdIder = aktiviteter.stream().map(a -> a.getArbeidsforholdRef().get().getReferanse()).distinct().collect(Collectors.toList());
            List<String> orgNummere = aktiviteter.stream().map(a -> a.getArbeidsgiver().getVirksomhet().getOrgnr()).distinct().collect(Collectors.toList());
            for (int i = 0; i < arbeidsforholdIder.size(); i++) {
                String arbId = arbeidsforholdIder.get(i);
                String orgNr = orgNummere.get(i);

                if (lagtTilAvSaksbehandlerPrAndelIArbeidsforhold.get(orgNr) != null && !lagtTilAvSaksbehandlerPrAndelIArbeidsforhold.get(orgNr).isEmpty()) {
                    for (int k = 0; k < lagtTilAvSaksbehandlerPrAndelIArbeidsforhold.get(orgNr).size(); k++) {
                        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold.builder()
                            .medArbeidsgiver(beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgNr))
                            .medArbforholdRef(arbId)
                            .medRefusjonskravPrÅr(refusjonPrÅr.get(orgNr) != null ? BigDecimal.valueOf(refusjonPrÅr.get(orgNr)) : null)
                            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
                        BeregningsgrunnlagPrStatusOgAndel.builder()
                            .medBGAndelArbeidsforhold(bga)
                            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                            .medAvkortetPrÅr(avkortetAndel.get(orgNr) != null ? BigDecimal.valueOf(avkortetAndel.get(orgNr)) : null)
                            .medBeregnetPrÅr(bruttoPrÅr.get(orgNr) != null ? BigDecimal.valueOf(bruttoPrÅr.get(orgNr)) : null)
                            .medLagtTilAvSaksbehandler(lagtTilAvSaksbehandlerPrAndelIArbeidsforhold.get(orgNr).get(k) != null ? lagtTilAvSaksbehandlerPrAndelIArbeidsforhold.get(orgNr).get(k) : false)
                            .medInntektskategori(inntektskategoriPrAndelIArbeidsforhold.get(orgNr) != null && k < inntektskategoriPrAndelIArbeidsforhold.get(orgNr).size() ?
                                inntektskategoriPrAndelIArbeidsforhold.get(orgNr).get(k) : null)
                            .build(beregningsgrunnlagPeriode);
                    }
                } else {
                    BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold.builder()
                        .medArbeidsgiver(beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgNr))
                        .medArbforholdRef(arbId)
                        .medRefusjonskravPrÅr(refusjonPrÅr.get(orgNr) != null ? BigDecimal.valueOf(refusjonPrÅr.get(orgNr)) : null)
                        .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                        .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
                    BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medBGAndelArbeidsforhold(bga)
                        .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                        .medAvkortetPrÅr(avkortetAndel.get(orgNr) != null ? BigDecimal.valueOf(avkortetAndel.get(orgNr)) : null)
                        .medBeregnetPrÅr(bruttoPrÅr.get(orgNr) != null ? BigDecimal.valueOf(bruttoPrÅr.get(orgNr)) : null)
                        .medBeregningsperiode(skjæringstidspunkt.minusMonths(3).withDayOfMonth(1), skjæringstidspunkt.withDayOfMonth(1).minusDays(1))
                        .build(beregningsgrunnlagPeriode);
                }
            }
        }
    }

    public BigDecimal getGrunnbeløp(LocalDate skjæringstidspunktOpptjening) {
        Sats sats = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, skjæringstidspunktOpptjening);
        return BigDecimal.valueOf(sats.getVerdi());
    }
}
