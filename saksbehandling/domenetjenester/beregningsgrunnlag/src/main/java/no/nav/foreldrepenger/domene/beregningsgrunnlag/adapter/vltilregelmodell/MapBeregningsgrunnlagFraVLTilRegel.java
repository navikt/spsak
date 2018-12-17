package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningsgrunnlagUtil.ATFL_SN_STATUSER;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningsgrunnlagUtil.ATFL_STATUSER;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ReferanseType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseAnvist;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.AktivPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.AktivitetStatusModell;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.Grunnbeløp;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.NaturalYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Periodeinntekt;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class MapBeregningsgrunnlagFraVLTilRegel {

    private static final Map<OpptjeningAktivitetType, Aktivitet> MAP_AKTIVITET;
    private static final Map<no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori, Inntektskategori> MAP_INNTEKTSKATEGORI;

    static {
        Map<OpptjeningAktivitetType, Aktivitet> map = new LinkedHashMap<>();
        map.put(OpptjeningAktivitetType.ARBEIDSAVKLARING, Aktivitet.AAP_MOTTAKER);
        map.put(OpptjeningAktivitetType.ARBEID, Aktivitet.ARBEIDSTAKERINNTEKT);
        map.put(OpptjeningAktivitetType.DAGPENGER, Aktivitet.DAGPENGEMOTTAKER);
        map.put(OpptjeningAktivitetType.ETTERLØNN_ARBEIDSGIVER, Aktivitet.ETTERLØNN);
        map.put(OpptjeningAktivitetType.FORELDREPENGER, Aktivitet.FORELDREPENGER_MOTTAKER);
        map.put(OpptjeningAktivitetType.FRILANS, Aktivitet.FRILANSINNTEKT);
        map.put(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, Aktivitet.MILITÆR_ELLER_SIVILTJENESTE);
        map.put(OpptjeningAktivitetType.NÆRING, Aktivitet.NÆRINGSINNTEKT);
        map.put(OpptjeningAktivitetType.OMSORGSPENGER, Aktivitet.OMSORGSPENGER);
        map.put(OpptjeningAktivitetType.OPPLÆRINGSPENGER, Aktivitet.OPPLÆRINGSPENGER);
        map.put(OpptjeningAktivitetType.UTDANNINGSPERMISJON, Aktivitet.UTDANNINGSPERMISJON);
        map.put(OpptjeningAktivitetType.PLEIEPENGER, Aktivitet.PLEIEPENGER_MOTTAKER);
        map.put(OpptjeningAktivitetType.SLUTTPAKKE, Aktivitet.SLUTTPAKKE);
        map.put(OpptjeningAktivitetType.SVANGERSKAPSPENGER, Aktivitet.SVANGERSKAPSPENGER_MOTTAKER);
        map.put(OpptjeningAktivitetType.SYKEPENGER, Aktivitet.SYKEPENGER_MOTTAKER);
        map.put(OpptjeningAktivitetType.VARTPENGER, Aktivitet.VARTPENGER);
        map.put(OpptjeningAktivitetType.VENTELØNN, Aktivitet.VENTELØNN);
        map.put(OpptjeningAktivitetType.VIDERE_ETTERUTDANNING, Aktivitet.VIDERE_ETTERUTDANNING);

        MAP_AKTIVITET = Collections.unmodifiableMap(map);

        Map<no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori, Inntektskategori> mapInntektskategori = new LinkedHashMap<>();
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.ARBEIDSAVKLARINGSPENGER, Inntektskategori.ARBEIDSAVKLARINGSPENGER);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.DAGMAMMA, Inntektskategori.DAGMAMMA);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.DAGPENGER, Inntektskategori.DAGPENGER);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER, Inntektskategori.FISKER);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FRILANSER, Inntektskategori.FRILANSER);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.JORDBRUKER, Inntektskategori.JORDBRUKER);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.SJØMANN, Inntektskategori.SJØMANN);
        mapInntektskategori.put(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.UDEFINERT, Inntektskategori.UDEFINERT);

        MAP_INNTEKTSKATEGORI = Collections.unmodifiableMap(mapInntektskategori);

    }

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    private BeregningRepository beregningRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private int inntektRapporteringFristDag;

    MapBeregningsgrunnlagFraVLTilRegel() {
        // CDI
    }

    @Inject
    public MapBeregningsgrunnlagFraVLTilRegel(BehandlingRepositoryProvider repositoryProvider,
                                              OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste,
                                              SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                              HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste,
                                              @KonfigVerdi(value = "inntekt.rapportering.frist.dato") int inntektRapporteringFristDag) {
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.opptjeningInntektArbeidYtelseTjeneste = opptjeningInntektArbeidYtelseTjeneste;
        this.beregningRepository = repositoryProvider.getBeregningRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.hentGrunnlagsdataTjeneste = hentGrunnlagsdataTjeneste;
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.inntektRapporteringFristDag = inntektRapporteringFristDag;
    }

    private static AktivitetStatus mapVLAktivitetStatus(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus vlBGAktivitetStatus) {
        try {
            return AktivitetStatus.valueOf(vlBGAktivitetStatus.getKode());
        } catch (IllegalArgumentException e) {
            if (ATFL_STATUSER.contains(vlBGAktivitetStatus)) {
                return AktivitetStatus.ATFL;
            } else if (ATFL_SN_STATUSER.contains(vlBGAktivitetStatus)) {
                return AktivitetStatus.ATFL_SN;
            }
            throw new IllegalArgumentException("Ukjent AktivitetStatus " + vlBGAktivitetStatus.getKode(), e);
        }
    }

    private static AktivitetStatusMedHjemmel mapVLAktivitetStatusMedHjemmel(final BeregningsgrunnlagAktivitetStatus vlBGAktivitetStatus) {
        BeregningsgrunnlagHjemmel hjemmel = null;
        if (!Hjemmel.UDEFINERT.equals(vlBGAktivitetStatus.getHjemmel())) {
            hjemmel = BeregningsgrunnlagHjemmel.valueOf(vlBGAktivitetStatus.getHjemmel().getKode());
        }
        AktivitetStatus as = mapVLAktivitetStatus(vlBGAktivitetStatus.getAktivitetStatus());
        return new AktivitetStatusMedHjemmel(as, hjemmel);
    }

    static List<Grunnbeløp> mapGrunnbeløpSatser(BeregningRepository beregningRepository) {
        List<Grunnbeløp> grunnbeløpListe = new ArrayList<>();
        int iår = LocalDate.now().getYear();
        for (int år = 2000; år <= iår; år++) {
            grunnbeløpListe.add(grunnbeløpOgSnittFor(beregningRepository, LocalDate.now().withYear(år)));
        }
        return grunnbeløpListe;
    }

    private static Grunnbeløp grunnbeløpOgSnittFor(BeregningRepository beregningRepository, LocalDate dato) {
        Sats g = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, dato);
        Sats gSnitt = beregningRepository.finnEksaktSats(SatsType.GSNITT, g.getPeriode().getFomDato());
        return new Grunnbeløp(g.getPeriode().getFomDato(), g.getPeriode().getTomDato(), g.getVerdi(), gSnitt.getVerdi());
    }

    private static void lagInntektBeregning(Inntektsgrunnlag inntektsgrunnlag, AktørInntekt aktørInntekt, Collection<Yrkesaktivitet> yrkesaktiviteter) {
        aktørInntekt.getInntektBeregningsgrunnlag().stream().filter(inntekt -> inntekt.getArbeidsgiver() != null)
            .forEach(inntekt -> mapInntekt(inntektsgrunnlag, inntekt, yrkesaktiviteter));
    }

    private static void mapInntekt(Inntektsgrunnlag inntektsgrunnlag, Inntekt inntekt, Collection<Yrkesaktivitet> yrkesaktiviteter) {
        inntekt.getInntektspost().forEach(inntektspost -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
            .medArbeidsgiver(mapYrkesaktivitet(inntekt.getArbeidsgiver(), yrkesaktiviteter))
            .medMåned(inntektspost.getFraOgMed())
            .medInntekt(inntektspost.getBeløp().getVerdi())
            .build()));
    }

    private static Arbeidsforhold mapYrkesaktivitet(Arbeidsgiver arbeidsgiver, Collection<Yrkesaktivitet> yrkesaktiviteter) {
        final List<ArbeidType> arbeidType = yrkesaktiviteter
            .stream()
            //TODO(OJR) hva gjør vi med yrkesaktiviter som ikke har arbeidsgiver? Sånns som militær-tjeneste??
            .filter(it -> it.getArbeidsgiver() != null)
            .filter(it -> it.getArbeidsgiver().getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
            .map(Yrkesaktivitet::getArbeidType)
            .distinct()
            .collect(Collectors.toList());
        boolean erFrilanser = yrkesaktiviteter.stream()
            .map(Yrkesaktivitet::getArbeidType)
            .anyMatch(ArbeidType.FRILANSER::equals);
        return (arbeidType.isEmpty() && erFrilanser) || arbeidType.contains(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER)
            ? Arbeidsforhold.frilansArbeidsforhold()
            : lagNyttArbeidsforholdHosArbeidsgiver(arbeidsgiver);
    }

    private static Arbeidsforhold lagNyttArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getIdentifikator());
        } else if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getIdentifikator());
        }
        throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet");
    }

    static Aktivitet mapTilRegelmodell(OpptjeningAktivitetType aktivitetType) {
        if (MAP_AKTIVITET.containsKey(aktivitetType)) {
            return MAP_AKTIVITET.get(aktivitetType);
        }
        return Aktivitet.UDEFINERT;//TODO: Kast feil?
    }

    private static void mapOppgittOpptjening(Inntektsgrunnlag inntektsgrunnlag, OppgittOpptjening oppgittOpptjening) {
        oppgittOpptjening.getEgenNæring().stream()
            .filter(en -> en.getNyoppstartet() || en.getVarigEndring())
            .filter(en -> en.getBruttoInntekt() != null)
            .forEach(en -> {
                    BigDecimal månedsinntekt = en.getBruttoInntekt().divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
                    inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                        .medInntektskilde(Inntektskilde.SØKNAD)
                        .medMåned(en.getEndringDato())
                        .medInntekt(månedsinntekt)
                        .build());
                }
            );
    }

    private static void mapInntektsmelding(Inntektsgrunnlag inntektsgrunnlag, List<Inntektsmelding> inntektsmeldinger) {
        inntektsmeldinger.forEach(im -> {
            Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(im.getVirksomhet().getOrgnr(), im.getArbeidsforholdRef() == null ? null : im.getArbeidsforholdRef().getReferanse());
            LocalDate måned = im.getStartDatoPermisjon().minusMonths(1).withDayOfMonth(1);
            BigDecimal inntekt = im.getInntektBeløp().getVerdi();
            List<NaturalYtelse> naturalytelser = im.getNaturalYtelser().stream()
                .map(ny -> new NaturalYtelse(ny.getBeloepPerMnd().getVerdi(), ny.getPeriode().getFomDato(), ny.getPeriode().getTomDato()))
                .collect(Collectors.toList());
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskilde(Inntektskilde.INNTEKTSMELDING)
                .medArbeidsgiver(arbeidsforhold)
                .medInntekt(inntekt)
                .medMåned(måned)
                .medNaturalYtelser(naturalytelser)
                .build());
        });
    }

    private static void mapTilstøtendeYtelserDagpengerOgAAP(Inntektsgrunnlag inntektsgrunnlag, AktørYtelse aktørYtelse, LocalDate skjæringstidspunkt) {
        Optional<Ytelse> nyesteVedtak = aktørYtelse.getYtelser().stream()
            .filter(ytelse -> RelatertYtelseType.DAGPENGER.equals(ytelse.getRelatertYtelseType())
                || RelatertYtelseType.ARBEIDSAVKLARINGSPENGER.equals(ytelse.getRelatertYtelseType()))
            .filter(ytelse -> ytelse.getPeriode() != null)
            .filter(ytelse -> !skjæringstidspunkt.isBefore(ytelse.getPeriode().getFomDato()))
            .filter(ytelse -> ytelse.getYtelseAnvist().stream().anyMatch(ya -> skjæringstidspunkt.isAfter(ya.getAnvistTOM())))
            .max(Comparator.comparing(ytelse -> ytelse.getPeriode().getFomDato()));

        if (!nyesteVedtak.isPresent()) {
            return;
        }
        Optional<YtelseAnvist> ytelseAnvistOpt = nyesteVedtak.get().getYtelseAnvist().stream()
            .filter(ya -> skjæringstidspunkt.isAfter(ya.getAnvistTOM()))
            .max(Comparator.comparing(YtelseAnvist::getAnvistFOM));

        ytelseAnvistOpt.ifPresent(ytelseAnvist -> {
            BigDecimal dagsats = ytelseAnvist.getDagsats().orElse(BigDecimal.ZERO);
            final BigDecimal toHundreSeksti = new BigDecimal("260");
            final BigDecimal tolv = new BigDecimal("12");
            BigDecimal månedsinntekt = dagsats.multiply(toHundreSeksti).divide(tolv, 10, RoundingMode.HALF_UP);
            BigDecimal utbetalingsgradProsent = ytelseAnvist.getUtbetalingsgradProsent()
                .orElseThrow(() -> new IllegalStateException("Utbetalingsgrad for ytelseanvist mangler"));

            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskilde(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
                .medInntekt(månedsinntekt)
                .medMåned(skjæringstidspunkt)
                .medUtbetalingsgrad(utbetalingsgradProsent)
                .build());
        });
    }

    private static void lagInntektSammenligning(Inntektsgrunnlag inntektsgrunnlag, AktørInntekt aktørInntekt) {
        Map<LocalDate, BigDecimal> månedsinntekter = aktørInntekt.getInntektSammenligningsgrunnlag().stream()
            .filter(inntekt -> inntekt.getArbeidsgiver() != null)
            .flatMap(i -> i.getInntektspost().stream())
            .collect(Collectors.groupingBy(Inntektspost::getFraOgMed, Collectors.reducing(BigDecimal.ZERO,
                ip -> ip.getBeløp().getVerdi(), BigDecimal::add)));

        månedsinntekter.forEach((måned, inntekt) -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
            .medMåned(måned)
            .medInntekt(inntekt)
            .build()));
    }

    private static void lagInntekterSN(Inntektsgrunnlag inntektsgrunnlag, AktørInntekt aktørInntekt) {
        List<Inntekt> liste = aktørInntekt.getBeregnetSkatt();
        liste.stream().flatMap(inntekt -> inntekt.getInntektspost().stream())
            .forEach(inntektspost -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskilde(Inntektskilde.SIGRUN)
                .medInntekt(inntektspost.getBeløp().getVerdi())
                .medPeriode(Periode.of(inntektspost.getFraOgMed(), inntektspost.getTilOgMed()))
                .build()
            ));
    }

    private static List<no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak> mapPeriodeÅrsak(List<BeregningsgrunnlagPeriodeÅrsak> beregningsgrunnlagPeriodeÅrsaker) {
        if (beregningsgrunnlagPeriodeÅrsaker.isEmpty()) {
            return Collections.emptyList();
        }
        List<no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak> periodeÅrsakerMapped = new ArrayList<>();
        beregningsgrunnlagPeriodeÅrsaker.forEach(bgPeriodeÅrsak -> {
            if (!PeriodeÅrsak.UDEFINERT.equals(bgPeriodeÅrsak.getPeriodeÅrsak())) {
                periodeÅrsakerMapped.add(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak.valueOf(bgPeriodeÅrsak.getPeriodeÅrsak().getKode()));
            }
        });
        return periodeÅrsakerMapped;
    }

    static boolean harYrkesaktiviteterFørSkjæringstidspunktet(AktørArbeid aa, LocalDate skjæringstidspunkt, ArbeidsforholdRef arbeidsforholdRef) {
        return aa.getYrkesaktiviteter().stream().anyMatch(ya -> ya.getArbeidsforholdRef().filter(ref -> ref.gjelderFor(arbeidsforholdRef)).isPresent() &&
            harAktivitetsavtaleAktivFørSkjæringstidspunktet(ya, skjæringstidspunkt));
    }

    private static boolean harAktivitetsavtaleAktivFørSkjæringstidspunktet(Yrkesaktivitet ya, LocalDate skjæringstidspunkt) {
        return ya.getAktivitetsAvtaler().stream().anyMatch(aa -> aa.getFraOgMed().isBefore(skjæringstidspunkt));
    }

    public no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag map(Behandling behandling,
                                                                                                       Beregningsgrunnlag vlBeregningsgrunnlag) {
        List<AktivitetStatusMedHjemmel> aktivitetStatuser = vlBeregningsgrunnlag.getAktivitetStatuser().stream()
            .map(MapBeregningsgrunnlagFraVLTilRegel::mapVLAktivitetStatusMedHjemmel)
            .sorted()
            .collect(Collectors.toList());


        Inntektsgrunnlag inntektsgrunnlag = mapInntektsgrunnlag(behandling, vlBeregningsgrunnlag);
        List<BeregningsgrunnlagPeriode> perioder = mapBeregningsgrunnlagPerioder(vlBeregningsgrunnlag);
        SammenligningsGrunnlag sammenligningsgrunnlag = mapEllerHentSammenligningsgrunnlag(behandling, vlBeregningsgrunnlag);
        Dekningsgrad dekningsgrad = mapDekningsgrad(behandling);

        return no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(inntektsgrunnlag)
            .medSkjæringstidspunkt(vlBeregningsgrunnlag.getSkjæringstidspunkt())
            .medAktivitetStatuser(aktivitetStatuser)
            .medBeregningsgrunnlagPerioder(perioder)
            .medSammenligningsgrunnlag(sammenligningsgrunnlag)
            .medDekningsgrad(dekningsgrad)
            .medGrunnbeløp(vlBeregningsgrunnlag.getGrunnbeløp().getVerdi())
            .medRedusertGrunnbeløp(vlBeregningsgrunnlag.getRedusertGrunnbeløp().getVerdi())
            .medGrunnbeløpSatser(mapGrunnbeløpSatser(beregningRepository))
            .medArbeidskategoriInaktiv(erArbeidskategoriInaktiv(behandling))
            .medSykepengerPåSkjæringstidspunkt(harSykepengerPåSkjæringstidpunkt(behandling, aktivitetStatuser))
            .build();
    }

    public Dekningsgrad mapDekningsgrad(Behandling behandling) {
        return Dekningsgrad.DEKNINGSGRAD_100;
    }

    private Inntektsgrunnlag mapInntektsgrunnlag(Behandling behandling, Beregningsgrunnlag vlBeregningsgrunnlag) {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(inntektRapporteringFristDag);
        hentInntektArbeidYtelse(behandling, inntektsgrunnlag, vlBeregningsgrunnlag);

        return inntektsgrunnlag;
    }

    private boolean harSykepengerPåSkjæringstidpunkt(Behandling behandling, List<AktivitetStatusMedHjemmel> aktivitetStatuser) {
        if (aktivitetStatuser.stream().noneMatch(as -> AktivitetStatus.TY.equals(as.getAktivitetStatus()))) {
            return false;
        }
        Optional<Ytelse> sisteYtelseFørSkjæringstidspunktOpt = opptjeningInntektArbeidYtelseTjeneste.hentSisteInfotrygdYtelseFørSkjæringstidspunktForOpptjening(behandling);
        return sisteYtelseFørSkjæringstidspunktOpt.filter(ytelse -> RelatertYtelseType.SYKEPENGER.equals(ytelse.getRelatertYtelseType())).isPresent();
    }

    private boolean erArbeidskategoriInaktiv(Behandling behandling) {
        Optional<Ytelse> ytelseOpt = opptjeningInntektArbeidYtelseTjeneste.hentSisteInfotrygdYtelseFørSkjæringstidspunktForOpptjening(behandling);
        if (!ytelseOpt.isPresent()) {
            return false;
        }
        Ytelse ytelse = ytelseOpt.get();
        Optional<YtelseGrunnlag> ytelsegrunnlagOpt = ytelse.getYtelseGrunnlag();
        if (!ytelsegrunnlagOpt.isPresent()) {
            return false;
        }
        YtelseGrunnlag ytelseGrunnlag = ytelsegrunnlagOpt.get();
        return ytelseGrunnlag.getArbeidskategori().filter(arbeidskategori -> arbeidskategori.equals(Arbeidskategori.INAKTIV)).isPresent();
    }

    private void hentInntektArbeidYtelse(Behandling behandling, Inntektsgrunnlag inntektsgrunnlag, Beregningsgrunnlag vlBeregningsgrunnlag) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseOpt = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling,
            skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        if (!inntektArbeidYtelseOpt.isPresent()) {
            return;
        }
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseOpt.get();
        Optional<AktørInntekt> aktørInntektOpt = inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp(behandling.getAktørId());
        Optional<AktørArbeid> aktørArbeidOpt = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp(behandling.getAktørId());
        Optional<AktørArbeid> bekreftetAnnenOpptjening = inntektArbeidYtelseGrunnlag.getBekreftetAnnenOpptjening();
        Optional<AktørYtelse> aktørYtelseOpt = inntektArbeidYtelseGrunnlag.getAktørYtelseFørStp(behandling.getAktørId());

        aktørInntektOpt.ifPresent(aktørInntekt -> {
            List<Yrkesaktivitet> yrkesaktiviteter = new ArrayList<>();
            yrkesaktiviteter.addAll(aktørArbeidOpt.map(AktørArbeid::getYrkesaktiviteter).orElse(Collections.emptyList()));
            yrkesaktiviteter.addAll(aktørArbeidOpt.map(AktørArbeid::getFrilansOppdrag).orElse(Collections.emptyList()));
            yrkesaktiviteter.addAll(bekreftetAnnenOpptjening.map(AktørArbeid::getYrkesaktiviteter).orElse(Collections.emptyList()));
            lagInntektBeregning(inntektsgrunnlag, aktørInntekt, yrkesaktiviteter);
            lagInntektSammenligning(inntektsgrunnlag, aktørInntekt);
            lagInntekterSN(inntektsgrunnlag, aktørInntekt);
        });

        Optional<InntektsmeldingAggregat> inntektsmeldingerOpt = inntektArbeidYtelseGrunnlag.getInntektsmeldinger();
        if (inntektsmeldingerOpt.isPresent() && !inntektsmeldingerOpt.get().getInntektsmeldinger().isEmpty()) {
            List<Inntektsmelding> inntektsmeldinger = inntektsmeldingerOpt.get().getInntektsmeldinger();
            mapInntektsmelding(inntektsgrunnlag, inntektsmeldinger);
        }

        if (aktørYtelseOpt.isPresent() && !aktørYtelseOpt.get().getYtelser().isEmpty()) {
            mapTilstøtendeYtelserDagpengerOgAAP(inntektsgrunnlag, aktørYtelseOpt.get(), vlBeregningsgrunnlag.getSkjæringstidspunkt());
        }
        Optional<OppgittOpptjening> oppgittOpptjeningOpt = inntektArbeidYtelseGrunnlag.getOppgittOpptjening();
        oppgittOpptjeningOpt.ifPresent(oppgittOpptjening -> mapOppgittOpptjening(inntektsgrunnlag, oppgittOpptjening));
    }

    private SammenligningsGrunnlag mapEllerHentSammenligningsgrunnlag(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        if (beregningsgrunnlag.getSammenligningsgrunnlag() != null) {
            return mapSammenligningsGrunnlag(beregningsgrunnlag);
        }
        if (brukTidligereSammenligningsgrunnlag(behandling, beregningsgrunnlag)) {
            if (behandling.erRevurdering()) {
                Behandling orginalbehandling = behandling.getOriginalBehandling()
                    .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal alltid ha orginalbehandling ved revurdering"));
                return hentTidligereSammenligningsgrunnlag(orginalbehandling);
            }
            return hentTidligereSammenligningsgrunnlag(behandling);
        }
        return null;
    }

    private SammenligningsGrunnlag hentTidligereSammenligningsgrunnlag(Behandling behandling) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> foreslåttBG = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.FORESLÅTT);
        return foreslåttBG.map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag).map(this::mapSammenligningsGrunnlag).orElse(null);
    }

    private boolean brukTidligereSammenligningsgrunnlag(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        boolean erATFLogIkkeSN = beregningsgrunnlag.getAktivitetStatuser().stream()
            .map(BeregningsgrunnlagAktivitetStatus::getAktivitetStatus)
            .filter(aktivitetStatus -> !aktivitetStatus.erSelvstendigNæringsdrivende())
            .anyMatch(aktivitetStatus -> aktivitetStatus.erArbeidstaker() || aktivitetStatus.erFrilanser());
        return erATFLogIkkeSN && !hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);
    }

    private SammenligningsGrunnlag mapSammenligningsGrunnlag(Beregningsgrunnlag beregningsgrunnlag) {
        return SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(new Periode(
                beregningsgrunnlag.getSammenligningsgrunnlag().getSammenligningsperiodeFom(),
                beregningsgrunnlag.getSammenligningsgrunnlag().getSammenligningsperiodeTom()))
            .medRapportertPrÅr(beregningsgrunnlag.getSammenligningsgrunnlag().getRapportertPrÅr())
            .medAvvikProsentFraPromille(beregningsgrunnlag.getSammenligningsgrunnlag().getAvvikPromille())
            .build();
    }

    private List<BeregningsgrunnlagPeriode> mapBeregningsgrunnlagPerioder(Beregningsgrunnlag vlBeregningsgrunnlag) {
        List<BeregningsgrunnlagPeriode> perioder = new ArrayList<>();
        vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(vlBGPeriode -> {
            final BeregningsgrunnlagPeriode.Builder regelBGPeriode = BeregningsgrunnlagPeriode.builder()
                .medPeriode(Periode.of(vlBGPeriode.getBeregningsgrunnlagPeriodeFom(), vlBGPeriode.getBeregningsgrunnlagPeriodeTom()))
                .leggTilPeriodeÅrsaker(mapPeriodeÅrsak(vlBGPeriode.getBeregningsgrunnlagPeriodeÅrsaker()));

            List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = mapVLBGPrStatus(vlBGPeriode);
            beregningsgrunnlagPrStatus.forEach(regelBGPeriode::medBeregningsgrunnlagPrStatus);
            perioder.add(regelBGPeriode.build());
        });

        return perioder;
    }

    private List<BeregningsgrunnlagPrStatus> mapVLBGPrStatus(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGPeriode) {
        List<BeregningsgrunnlagPrStatus> liste = new ArrayList<>();
        BeregningsgrunnlagPrStatus bgpsATFL = null;

        for (BeregningsgrunnlagPrStatusOgAndel vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
            if (AktivitetStatus.ATFL.equals(regelAktivitetStatus)) {
                if (bgpsATFL == null) {  // Alle ATFL håndteres samtidig her
                    bgpsATFL = mapVLBGPStatusForATFL(vlBGPeriode);
                    liste.add(bgpsATFL);
                }
            } else {
                BeregningsgrunnlagPrStatus bgps = mapVLBGPStatusForAlleAktivietetStatuser(vlBGPStatus);
                liste.add(bgps);
            }
        }
        return liste;
    }

    // Ikke ATFL og TY, de har separat mapping
    private BeregningsgrunnlagPrStatus mapVLBGPStatusForAlleAktivietetStatuser(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
        List<BigDecimal> pgi = (vlBGPStatus.getPgiSnitt() == null ? new ArrayList<>() :
            Arrays.asList(vlBGPStatus.getPgi1(), vlBGPStatus.getPgi2(), vlBGPStatus.getPgi3()));
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(regelAktivitetStatus)
            .medBeregningsperiode(beregningsperiodeFor(vlBGPStatus))
            .medBeregnetPrÅr(vlBGPStatus.getBeregnetPrÅr())
            .medOverstyrtPrÅr(vlBGPStatus.getOverstyrtPrÅr())
            .medGjennomsnittligPGI(vlBGPStatus.getPgiSnitt())
            .medPGI(pgi)
            .medÅrsbeløpFraTilstøtendeYtelse(vlBGPStatus.getÅrsbeløpFraTilstøtendeYtelseVerdi())
            .medErNyIArbeidslivet(vlBGPStatus.getNyIArbeidslivet())
            .medAndelNr(vlBGPStatus.getAndelsnr())
            .medInntektskategori(MAP_INNTEKTSKATEGORI.get(vlBGPStatus.getInntektskategori()))
            .medFastsattAvSaksbehandler(vlBGPStatus.getFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(vlBGPStatus.getLagtTilAvSaksbehandler())
            .medBesteberegningPrÅr(vlBGPStatus.getBesteberegningPrÅr())
            .build();
    }

    private Periode beregningsperiodeFor(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        if (vlBGPStatus.getBeregningsperiodeFom() == null && vlBGPStatus.getBeregningsperiodeTom() == null) {
            return null;
        }
        return Periode.of(vlBGPStatus.getBeregningsperiodeFom(), vlBGPStatus.getBeregningsperiodeTom());
    }

    private boolean erFrilanser(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus aktivitetStatus) {
        return no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.FRILANSER.equals(aktivitetStatus);
    }

    // Felles mapping av alle statuser som mapper til ATFL
    private BeregningsgrunnlagPrStatus mapVLBGPStatusForATFL(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGPeriode) {

        BeregningsgrunnlagPrStatus.Builder regelBGPStatusATFL = BeregningsgrunnlagPrStatus.builder().medAktivitetStatus(AktivitetStatus.ATFL);

        for (BeregningsgrunnlagPrStatusOgAndel vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            if (AktivitetStatus.ATFL.equals(mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus()))) {
                BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold = byggAndel(vlBGPStatus);
                regelBGPStatusATFL.medArbeidsforhold(regelArbeidsforhold);
            }
        }
        return regelBGPStatusATFL.build();
    }

    private BeregningsgrunnlagPrArbeidsforhold byggAndel(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        BeregningsgrunnlagPrArbeidsforhold.Builder builder = BeregningsgrunnlagPrArbeidsforhold.builder();
        builder
            .medInntektskategori(MAP_INNTEKTSKATEGORI.get(vlBGPStatus.getInntektskategori()))
            .medBeregnetPrÅr(vlBGPStatus.getBeregnetPrÅr())
            .medBeregningsperiode(beregningsperiodeFor(vlBGPStatus))
            .medFastsattAvSaksbehandler(vlBGPStatus.getFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(vlBGPStatus.getLagtTilAvSaksbehandler())
            .medAndelNr(vlBGPStatus.getAndelsnr())
            .medOverstyrtPrÅr(vlBGPStatus.getOverstyrtPrÅr())
            .medArbeidsforhold(arbeidsforholdFor(vlBGPStatus));

        vlBGPStatus.getBgAndelArbeidsforhold().ifPresent(bga ->
            builder
                .medNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().orElse(null))
                .medNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().orElse(null))
                .medErTidsbegrensetArbeidsforhold(bga.getErTidsbegrensetArbeidsforhold())
                .medRefusjonskravPrÅr(bga.getRefusjonskravPrÅr()));

        return builder.build();
    }

    private Arbeidsforhold arbeidsforholdFor(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        Optional<Arbeidsgiver> arbeidsgiver = vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver);
        if (arbeidsgiver.isPresent()) {
            return erFrilanser(vlBGPStatus.getAktivitetStatus()) ? Arbeidsforhold.frilansArbeidsforhold()
                : lagArbeidsforholdHosArbeidsgiver(arbeidsgiver.get(), vlBGPStatus);
        } else {
            return erFrilanser(vlBGPStatus.getAktivitetStatus()) ? Arbeidsforhold.frilansArbeidsforhold()
                : Arbeidsforhold.anonymtArbeidsforhold(MAP_AKTIVITET.get(vlBGPStatus.getArbeidsforholdType()));
        }
    }

    private Arbeidsforhold lagArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver, BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        String arbRef = arbeidsforholdRefFor(vlBGPStatus);
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getVirksomhet().getOrgnr(), arbRef);
        }
        if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getAktørId().getId(), arbRef);
        }
        throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet");
    }

    private String arbeidsforholdRefFor(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        return vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).map(ArbeidsforholdRef::getReferanse).orElse(null);
    }

    // TODO TOPAS Skriv tester for denne!
    public AktivitetStatusModell mapForSkjæringstidspunktOgStatuser(Behandling behandling) {
        AktivitetStatusModell modell = new AktivitetStatusModell();

        Opptjening opptjening = opptjeningInntektArbeidYtelseTjeneste.hentOpptjening(behandling);
        modell.setSkjæringstidspunktForOpptjening(opptjening.getTom().plusDays(1));

        List<OpptjeningAktivitet> aktivitet = opptjeningInntektArbeidYtelseTjeneste.hentGodkjentAktivitetTyper(behandling, modell.getSkjæringstidspunktForOpptjening());

        if (aktivitet.isEmpty()) {  // For enklere feilsøking når det mangler aktiviteter
            Aktivitet aktivitetType = mapTilRegelmodell(OpptjeningAktivitetType.UDEFINERT);
            AktivPeriode aktivPeriode = AktivPeriode.forAndre(aktivitetType, Periode.of(LocalDate.now().minusYears(1), LocalDate.now()));
            modell.leggTilAktivPeriode(aktivPeriode);
        } else {
            aktivitet.forEach(a -> lagAktivePerioder(behandling, a).forEach(modell::leggTilAktivPeriode));
        }
        return modell;
    }

    private List<AktivPeriode> lagAktivePerioder(Behandling behandling, OpptjeningAktivitet a) {
        Aktivitet aktivitetType = mapTilRegelmodell(a.getAktivitetType());
        Periode gjeldendePeriode = Periode.of(a.getFom(), a.getTom());
        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return Collections.singletonList(AktivPeriode.forFrilanser(gjeldendePeriode));
        }
        if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            return lagAktivPeriodeForArbeidstaker(behandling, a, aktivitetType, gjeldendePeriode);
        }
        return Collections.singletonList(AktivPeriode.forAndre(aktivitetType, gjeldendePeriode));
    }

    private List<AktivPeriode> lagAktivPeriodeForArbeidstaker(Behandling behandling,
                                                              OpptjeningAktivitet opptjeningAktivitet,
                                                              Aktivitet aktivitetType,
                                                              Periode gjeldendePeriode) {

        if (ReferanseType.AKTØR_ID.equals(opptjeningAktivitet.getAktivitetReferanseType())) {
            return lagAktivePerioderForArbeidstakerHosPrivatperson(opptjeningAktivitet, gjeldendePeriode);
        } else if (ReferanseType.ORG_NR.equals(opptjeningAktivitet.getAktivitetReferanseType())) {
            return lagAktivePerioderForArbeidstakerHosVirksomhet(behandling, opptjeningAktivitet, aktivitetType, gjeldendePeriode);
        }
        throw new IllegalStateException("Arbedisgiver må være enten aktør eller virksomhet");
    }

    private List<AktivPeriode> lagAktivePerioderForArbeidstakerHosVirksomhet(Behandling behandling, OpptjeningAktivitet opptjeningAktivitet, Aktivitet aktivitetType, Periode gjeldendePeriode) {
        String orgnr = mapTilRegelmodellForOrgnr(aktivitetType, opptjeningAktivitet);
        List<String> alleArbeidsforhold = finnArbeidsforholdHosVirksomhet(orgnr, behandling);
        if (alleArbeidsforhold.isEmpty()) {
            return Collections.singletonList(AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, orgnr, null));
        } else {
            return alleArbeidsforhold
                .stream()
                .map(a -> AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, orgnr, a))
                .collect(Collectors.toList());
        }
    }

    private List<AktivPeriode> lagAktivePerioderForArbeidstakerHosPrivatperson(OpptjeningAktivitet opptjeningAktivitet, Periode gjeldendePeriode) {
        // Da vi ikke kan motta inntektsmeldinger ønsker vi ikke å sette arbeidsforholdId på arbeidsforholdet
        return Collections.singletonList(AktivPeriode.forArbeidstakerHosPrivatperson(gjeldendePeriode, opptjeningAktivitet.getAktivitetReferanse(), null));
    }

    private List<String> finnArbeidsforholdHosVirksomhet(String orgnr, Behandling behandling) {
        List<String> arbeidsforhold = new ArrayList<>();
        if (orgnr == null) {
            return arbeidsforhold;
        }
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelse = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling,
            skjæringstidspunkt);
        if (inntektArbeidYtelse.isPresent()) {
            Optional<InntektsmeldingAggregat> inntektsmeldingAggregat = inntektArbeidYtelse.get().getInntektsmeldinger();
            Optional<AktørArbeid> aktørArbeidFørStp = inntektArbeidYtelse.get().getAktørArbeidFørStp(behandling.getAktørId());
            if (inntektsmeldingAggregat.isPresent() && aktørArbeidFørStp.isPresent()) {
                arbeidsforhold = inntektsmeldingAggregat.get().getInntektsmeldinger().stream()
                    .filter(im -> orgnr.equals(im.getVirksomhet().getOrgnr()))
                    .filter(im -> im.getArbeidsforholdRef() != null)
                    .filter(im -> harYrkesaktiviteterFørSkjæringstidspunktet(aktørArbeidFørStp.get(), skjæringstidspunkt, im.getArbeidsforholdRef()))
                    .map(Inntektsmelding::getArbeidsforholdRef)
                    .filter(ArbeidsforholdRef::gjelderForSpesifiktArbeidsforhold)
                    .map(ArbeidsforholdRef::getReferanse)
                    .collect(Collectors.toList());
            }
        }
        return arbeidsforhold;
    }

    private String mapTilRegelmodellForOrgnr(Aktivitet aktivitetType, OpptjeningAktivitet opptjeningAktivitet) {
        return (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType) ? opptjeningAktivitet.getAktivitetReferanse() : null);
    }
}
