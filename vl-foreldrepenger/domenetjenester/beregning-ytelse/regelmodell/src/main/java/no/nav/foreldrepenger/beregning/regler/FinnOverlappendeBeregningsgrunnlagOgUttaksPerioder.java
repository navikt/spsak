package no.nav.foreldrepenger.beregning.regler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatAndel;
import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatPeriode;
import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatRegelmodell;
import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatRegelmodellMellomregning;
import no.nav.foreldrepenger.beregning.regelmodell.UttakAktivitet;
import no.nav.foreldrepenger.beregning.regelmodell.UttakResultat;
import no.nav.foreldrepenger.beregning.regelmodell.UttakResultatPeriode;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.vedtak.util.Tuple;

class FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder extends LeafSpecification<BeregningsresultatRegelmodellMellomregning> {
    public static final String ID = "FP_BR 20_1";
    public static final String BESKRIVELSE = "FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder";

    private static final String BRUKER_ANDEL = ".brukerAndel";
    private static final String ARBEIDSGIVERS_ANDEL = ".arbeidsgiverAndel";
    private static final String DAGSATS_BRUKER = ".dagsatsBruker";
    private static final String DAGSATS_ARBEIDSGIVER = ".dagsatsArbeidsgiver";
    private static final String ORGNR = ".orgnr";

    FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsresultatRegelmodellMellomregning mellomregning) {
        //Regelsporing
        Map<String, Object> resultater = new LinkedHashMap<>();

        BeregningsresultatRegelmodell regelmodell = mellomregning.getInput();
        Beregningsgrunnlag grunnlag = regelmodell.getBeregningsgrunnlag();
        UttakResultat uttakResultat = regelmodell.getUttakResultat();

        List<BeregningsresultatPeriode> periodeListe = mapPerioder(grunnlag, uttakResultat, resultater);
        periodeListe.forEach(p -> mellomregning.getOutput().addBeregningsresultatPeriode(p));
        return beregnet(resultater);
    }

    private List<BeregningsresultatPeriode> mapPerioder(Beregningsgrunnlag grunnlag, UttakResultat uttakResultat, Map<String, Object> resultater) {
        LocalDateTimeline<BeregningsgrunnlagPeriode> grunnlagTimeline = mapGrunnlagTimeline(grunnlag);
        LocalDateTimeline<UttakResultatPeriode> uttakTimeline = uttakResultat.getUttakPeriodeTimeline();
        LocalDateTimeline<BeregningsresultatPeriode> resultatTimeline = intersectTimelines(grunnlagTimeline, uttakTimeline, resultater)
            .compress();
        return resultatTimeline.toSegments().stream().map(LocalDateSegment::getValue).collect(Collectors.toList());
    }

    private LocalDateTimeline<BeregningsresultatPeriode> intersectTimelines(LocalDateTimeline<BeregningsgrunnlagPeriode> grunnlagTimeline, LocalDateTimeline<UttakResultatPeriode> uttakTimeline, Map<String, Object> resultater) {
        final int[] i = {0}; //Periode-teller til regelsporing
        return grunnlagTimeline.intersection(uttakTimeline, (dateInterval, grunnlagSegment, uttakSegment) ->
        {
            BeregningsresultatPeriode resultatPeriode = BeregningsresultatPeriode.builder()
                .medPeriode(dateInterval).build();

            //Regelsporing
            String periodeNavn = "BeregningsresultatPeriode[" + i[0] + "]";
            resultater.put(periodeNavn + ".fom", dateInterval.getFomDato());
            resultater.put(periodeNavn + ".tom", dateInterval.getTomDato());

            BeregningsgrunnlagPeriode grunnlag = grunnlagSegment.getValue();
            UttakResultatPeriode uttakResultatPeriode = uttakSegment.getValue();

            grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).forEach(gbps -> {
                // for hver arbeidstaker andel: map fra grunnlag til 1-2 resultatAndel
                List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdList = gbps.getArbeidsforhold();
                arbeidsforholdList.forEach(a -> opprettBeregningsresultatAndelerATFL(a, resultatPeriode, resultater, periodeNavn, uttakResultatPeriode));
            });
            grunnlag.getBeregningsgrunnlagPrStatus().stream()
                .filter(bgps -> !AktivitetStatus.ATFL.equals(bgps.getAktivitetStatus()))
                .forEach(bgps -> opprettBeregningsresultatAndelerGenerell(bgps, resultatPeriode, resultater, periodeNavn, uttakResultatPeriode));

            i[0]++;
            return new LocalDateSegment<>(dateInterval, resultatPeriode);
        });
    }

    private void opprettBeregningsresultatAndelerGenerell(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus, BeregningsresultatPeriode resultatPeriode,
                                                          Map<String, Object> resultater, String periodeNavn, UttakResultatPeriode uttakResultatPeriode) {

        UttakAktivitet uttakAktivitet = matchUttakAktivitetMedBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatus, uttakResultatPeriode.getUttakAktiviteter());

        //Gradering
        Tuple<Long, Long> dagsatser = kalkulerDagsatserForGradering(beregningsgrunnlagPrStatus.getRedusertBrukersAndelPrÅr(), BigDecimal.ZERO,
            uttakAktivitet, resultater, periodeNavn);
        Long dagsatsBruker = dagsatser.getElement1();

        resultatPeriode.addBeregningsresultatAndel(
            BeregningsresultatAndel.builder()
                .medBrukerErMottaker(true)
                .medDagsats(dagsatsBruker)
                .medDagsatsFraBg(årsbeløpTilDagsats(beregningsgrunnlagPrStatus.getRedusertBrukersAndelPrÅr()))
                .medAktivitetStatus(beregningsgrunnlagPrStatus.getAktivitetStatus())
                .medInntektskategori(beregningsgrunnlagPrStatus.getInntektskategori())
                .medUtbetalingssgrad(uttakAktivitet.getUtbetalingsgrad())
                .medStillingsprosent(uttakAktivitet.getStillingsgrad())
                .build(resultatPeriode));

        // Regelsporing
        String beskrivelse = periodeNavn + BRUKER_ANDEL + "['" + beregningsgrunnlagPrStatus.getAktivitetStatus().name() + "']" + DAGSATS_BRUKER;
        resultater.put(beskrivelse, dagsatsBruker);

    }

    private UttakAktivitet matchUttakAktivitetMedBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus, List<UttakAktivitet> uttakAktiviteter) {
        if (uttakAktiviteter.size() == 1) {
            return uttakAktiviteter.get(0);
        }
        return uttakAktiviteter.stream()
            .filter(aktivitet -> aktivitet.getAktivitetStatus().equals(beregningsgrunnlagPrStatus.getAktivitetStatus())
                || (aktivitet.getAktivitetStatus().equals(AktivitetStatus.UDEFINERT) && !beregningsgrunnlagPrStatus.getAktivitetStatus().erGraderbar()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Klarer ikke å matche beregningsgrunnlagPrStatus til uttakAktivitet"));
    }

    private void opprettBeregningsresultatAndelerATFL(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsresultatPeriode resultatPeriode,
                                                      Map<String, Object> resultater, String periodeNavn, UttakResultatPeriode uttakResultatPeriode) {

        UttakAktivitet uttakAktivitet = matchUttakAktivitetMedArbeidsforhold(uttakResultatPeriode.getUttakAktiviteter(), arbeidsforhold);
        String orgnr = arbeidsforhold.getArbeidsforhold().getOrgnr();

        //Gradering
        Tuple<Long, Long> dagsatser = kalkulerDagsatserForGradering(arbeidsforhold.getRedusertBrukersAndelPrÅr(), arbeidsforhold.getRedusertRefusjonPrÅr(),
            uttakAktivitet, resultater, periodeNavn);

        Long dagsatsBruker = dagsatser.getElement1();
        Long dagsatsArbeidsgiver = dagsatser.getElement2();

        resultatPeriode.addBeregningsresultatAndel(
            BeregningsresultatAndel.builder()
                .medArbeidsforhold(arbeidsforhold.getArbeidsforhold())
                .medBrukerErMottaker(true)
                .medStillingsprosent(uttakAktivitet.getStillingsgrad())
                .medUtbetalingssgrad(uttakAktivitet.getUtbetalingsgrad())
                .medDagsats(dagsatsBruker)
                .medDagsatsFraBg(arbeidsforhold.getDagsatsBruker())
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medInntektskategori(arbeidsforhold.getInntektskategori())
                .build(resultatPeriode));

        // Regelsporing
        resultater.put(periodeNavn + BRUKER_ANDEL + "['" + orgnr + "']" + ORGNR, orgnr);
        resultater.put(periodeNavn + BRUKER_ANDEL + "['" + orgnr + "']" + DAGSATS_BRUKER, dagsatsBruker);
        resultater.put(periodeNavn + BRUKER_ANDEL + "['" + orgnr + "']" + ".dagsatsFraBeregningsgrunnlagBruker", arbeidsforhold.getDagsatsBruker());

        if (arbeidsforhold.getDagsatsArbeidsgiver() != null && arbeidsforhold.getDagsatsArbeidsgiver() > 0) {
            resultatPeriode.addBeregningsresultatAndel(
                BeregningsresultatAndel.builder()
                    .medArbeidsforhold(arbeidsforhold.getArbeidsforhold())
                    .medBrukerErMottaker(false)
                    .medStillingsprosent(uttakAktivitet.getStillingsgrad())
                    .medUtbetalingssgrad(uttakAktivitet.getUtbetalingsgrad())
                    .medDagsats(dagsatsArbeidsgiver)
                    .medDagsatsFraBg(arbeidsforhold.getDagsatsArbeidsgiver())
                    .medInntektskategori(arbeidsforhold.getInntektskategori())
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .build(resultatPeriode));

            //Regelsporing
            resultater.put(periodeNavn + ARBEIDSGIVERS_ANDEL + "['" + orgnr + "']" + ORGNR, orgnr);
            resultater.put(periodeNavn + ARBEIDSGIVERS_ANDEL + "['" + orgnr + "']" + DAGSATS_ARBEIDSGIVER, dagsatsArbeidsgiver);
            resultater.put(periodeNavn + ARBEIDSGIVERS_ANDEL + "['" + orgnr + "']" + ".dagsatsFraBeregningsgrunnlagArbeidsgiver", arbeidsforhold.getDagsatsArbeidsgiver());
        }
    }

    private UttakAktivitet matchUttakAktivitetMedArbeidsforhold(List<UttakAktivitet> uttakAktiviteter, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        if (uttakAktiviteter.size() == 1) {
            return uttakAktiviteter.get(0);
        }

        return uttakAktiviteter.stream()
            .filter(uttakAktivitet -> Objects.equals(uttakAktivitet.getArbeidsforhold(), arbeidsforhold.getArbeidsforhold()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Kunne ikke matche uttakaktivitet til beregningsgrunnlagAndel"));
    }

    /*
    Returnerer en Tuple (Long dagsatsBruker, Long dagsatsArbeidsgiver) med dagsatser gradert for bruker og arbeidsgiver
     */
    private static Tuple<Long, Long> kalkulerDagsatserForGradering(BigDecimal redusertBrukersAndelPrÅr, BigDecimal redusertRefusjonPrÅr,
                                                                   UttakAktivitet uttakAktivitet, Map<String, Object> resultater, String periodenavn) {
        if (uttakAktivitet.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) == 0) {
            resultater.put(periodenavn + ".utbetalingsgrad", uttakAktivitet.getUtbetalingsgrad());
            return new Tuple<>(0L, 0L);
        }

        BigDecimal utbetalingsgrad = uttakAktivitet.getUtbetalingsgrad().scaleByPowerOfTen(-2);
        BigDecimal andelArbeidsgiver = Optional.ofNullable(redusertRefusjonPrÅr).orElse(BigDecimal.ZERO);
        BigDecimal andelBruker = Optional.ofNullable(redusertBrukersAndelPrÅr).orElse(BigDecimal.ZERO);
        BigDecimal redusertAndelArb = andelArbeidsgiver.multiply(utbetalingsgrad);
        BigDecimal redusertAndelBruker = andelBruker.multiply(utbetalingsgrad);
        BigDecimal permisjonsProsent = finnPermisjonsprosent(uttakAktivitet, utbetalingsgrad);
        BigDecimal maksimalRefusjon = BigDecimal.ZERO.max(andelArbeidsgiver.multiply(permisjonsProsent));
        BigDecimal utbetalingBruker = redusertAndelArb.subtract(maksimalRefusjon).add(redusertAndelBruker);

        long dagsatsArbeidsgiver = årsbeløpTilDagsats(maksimalRefusjon);
        long dagsatsBruker = årsbeløpTilDagsats(utbetalingBruker);

        // Regelsporing
        resultater.put(periodenavn + ".utbetalingsgrad", uttakAktivitet.getUtbetalingsgrad());
        resultater.put(periodenavn + ".stillingsprosent", uttakAktivitet.getStillingsgrad());

        return new Tuple<>(dagsatsBruker, dagsatsArbeidsgiver);
    }

    private static BigDecimal finnPermisjonsprosent(UttakAktivitet uttakAktivitet, BigDecimal utbetalingsgrad) {
        if (!uttakAktivitet.isErGradering()) {
            return utbetalingsgrad;
        }
        BigDecimal stillingsprosent = uttakAktivitet.getStillingsgrad().scaleByPowerOfTen(-2);
        if (stillingsprosent.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal nyStillingsProsent = BigDecimal.ONE.subtract(utbetalingsgrad);
        return BigDecimal.ONE.subtract(nyStillingsProsent.divide(stillingsprosent, 10, RoundingMode.HALF_UP));
    }

    private LocalDateTimeline<BeregningsgrunnlagPeriode> mapGrunnlagTimeline(Beregningsgrunnlag grunnlag) {
        List<LocalDateSegment<BeregningsgrunnlagPeriode>> grunnlagPerioder = grunnlag.getBeregningsgrunnlagPerioder().stream()
            .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriode().getFom(), p.getBeregningsgrunnlagPeriode().getTom(), p))
            .collect(Collectors.toList());
        return new LocalDateTimeline<>(grunnlagPerioder);
    }

    private static long årsbeløpTilDagsats(BigDecimal årsbeløp) {
        final BigDecimal toHundreSeksti = BigDecimal.valueOf(260);
        return årsbeløp.divide(toHundreSeksti, 0, RoundingMode.HALF_UP).longValue();
    }
}
