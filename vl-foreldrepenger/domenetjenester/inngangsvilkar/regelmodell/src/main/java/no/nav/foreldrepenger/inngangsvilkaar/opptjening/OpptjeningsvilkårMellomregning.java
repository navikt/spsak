package no.nav.foreldrepenger.inngangsvilkaar.opptjening;

import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Aktivitet;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.AktivitetPeriode;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.InntektPeriode;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsvilkårResultat;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjentTidslinje;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

/**
 * Data underlag inkludert mellomregning og mellomresultater brukt i vilkårsvurderingen.
 */
class OpptjeningsvilkårMellomregning {

    private final Map<Aktivitet, AktivitetMellomregning> mellomregning = new HashMap<>();

    /** Beregnet total opptjening (inklusiv bekreftet og antatt) */
    private OpptjentTidslinje antattTotalOpptjening;

    /** Beregnet total opptjening (kun bekreftet). */
    private OpptjentTidslinje bekreftetTotalOpptjening;

    /** Beregnet total opptjening. */
    private OpptjentTidslinje totalOpptjening;

    /**
     * Opprinnelig grunnlag.
     */
    private Opptjeningsgrunnlag grunnlag;

    /** Frist for å motta opptjening opplysninger (henger sammen med Aksjonspunkt 7006 "Venter på Opptjeningsopplysninger"). */
    private LocalDate opptjeningOpplysningerFrist;

    OpptjeningsvilkårMellomregning(Opptjeningsgrunnlag grunnlag) {
        this.grunnlag = grunnlag;
        LocalDateInterval maxIntervall = grunnlag.getOpptjeningPeriode();

        // grupper aktivitet perioder etter aktivitet og avkort i forhold til angitt startDato/skjæringstidspunkt
        splitAktiviter(
            a -> a.getVurderingsStatus() == null)
                .forEach(e -> mellomregning.computeIfAbsent(e.getKey(),
                    a -> new AktivitetMellomregning(a, e.getValue())));

        splitAktiviter(
            a -> Objects.equals(AktivitetPeriode.VurderingsStatus.TIL_VURDERING, a.getVurderingsStatus()))
                .forEach(e -> mellomregning.computeIfAbsent(e.getKey(),
                    a -> new AktivitetMellomregning(a, e.getValue())));

        splitAktiviter(
            a -> Objects.equals(AktivitetPeriode.VurderingsStatus.VURDERT_GODKJENT, a.getVurderingsStatus()))
                .forEach(e -> mellomregning.computeIfAbsent(e.getKey(),
                    AktivitetMellomregning::new).setAktivitetManueltGodkjent(e.getValue()));

        splitAktiviter(
            a -> Objects.equals(AktivitetPeriode.VurderingsStatus.VURDERT_UNDERKJENT, a.getVurderingsStatus()))
                .forEach(
                    e -> mellomregning.computeIfAbsent(e.getKey(),
                        AktivitetMellomregning::new).setAktivitetManueltUnderkjent(e.getValue()));

        // grupper inntektperioder etter aktivitet og avkort i forhold til angitt startDato/skjæringstidspunkt
        Map<Aktivitet, Set<LocalDateSegment<Long>>> grupperInntekterEtterAktiitet = grunnlag.getInntektPerioder().stream().collect(
            Collectors.groupingBy(InntektPeriode::getAktivitet,
                Collectors.mapping(a1 -> new LocalDateSegment<>(a1.getDatoInterval(), a1.getInntektBeløp()), Collectors.toSet())));

        LocalDateSegmentCombinator<Long, Long, Long> inntektOverlapDuplikatCombinator = StandardCombinators::sum;

        grupperInntekterEtterAktiitet
            .entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(),
                new LocalDateTimeline<>(e.getValue(), inntektOverlapDuplikatCombinator).intersection(maxIntervall)))
            .filter(e -> !e.getValue().isEmpty())
            .forEach(
                e -> mellomregning.computeIfAbsent(e.getKey(),
                    AktivitetMellomregning::new).setInntektTidslinjer(e.getValue()));

    }

    private Stream<Map.Entry<Aktivitet, LocalDateTimeline<Boolean>>> splitAktiviter(Predicate<AktivitetPeriode> filter) {
        Map<Aktivitet, Set<LocalDateSegment<Boolean>>> aktiviteter = grunnlag.getAktivitetPerioder().stream()
            .filter(filter)
            .collect(
                Collectors.groupingBy(AktivitetPeriode::getOpptjeningAktivitet,
                    Collectors.mapping(a -> new LocalDateSegment<>(a.getDatoInterval(), Boolean.TRUE), Collectors.toSet())));

        LocalDateSegmentCombinator<Boolean, Boolean, Boolean> aktivitetOverlappDuplikatCombinator = StandardCombinators::alwaysTrueForMatch;

        return aktiviteter
            .entrySet().stream()
            .map(e -> {
                return (Map.Entry<Aktivitet, LocalDateTimeline<Boolean>>) new AbstractMap.SimpleEntry<>(e.getKey(),
                    new LocalDateTimeline<>(e.getValue().stream().sorted(Comparator.comparing(LocalDateSegment::getLocalDateInterval)).collect(Collectors.toList()), aktivitetOverlappDuplikatCombinator));
            })
            .filter(e -> !e.getValue().isEmpty());
    }

    Map<Aktivitet, LocalDateTimeline<Boolean>> getAkseptertMellomliggendePerioder() {
        return getMellomregningTidslinje(AktivitetMellomregning::getAkseptertMellomliggendePerioder);
    }

    private <V> Map<Aktivitet, LocalDateTimeline<V>> getMellomregningTidslinje(Function<AktivitetMellomregning, LocalDateTimeline<V>> fieldGetter) {
        return mellomregning.entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), fieldGetter.apply(e.getValue())))
            .filter(e -> !e.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    void setAkseptertMellomliggendePerioder(Map<Aktivitet, LocalDateTimeline<Boolean>> perioder) {
        perioder.forEach((key, value) -> mellomregning.get(key).setAkseptertMellomliggendePerioder(value));
    }

    /**
     * Returnerer aktivitet tidslinjer, uten underkjente perioder (hvis satt), og med valgfritt med/uten antatt
     * godkjente perioder.
     */
    Map<Aktivitet, LocalDateTimeline<Boolean>> getAktivitetTidslinjer(boolean medAntattGodkjentePerioder, boolean medIkkebekreftedeGodkjentePerioder) {

        Map<Aktivitet, LocalDateTimeline<Boolean>> resultat = mellomregning
            .entrySet().stream()
            .map(
                e -> new AbstractMap.SimpleEntry<>(e.getKey(),
                    e.getValue().getAktivitetTidslinje(medAntattGodkjentePerioder, medIkkebekreftedeGodkjentePerioder)))
            .filter(e -> !e.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return resultat;

    }

    boolean splitOgUnderkjennSegmenterEtterDatoForAktivitet(Aktivitet aktivitet, LocalDate splitDato) {

        if (splitDato.equals(grunnlag.getSisteDatoForOpptjening()) || mellomregning.get(aktivitet) == null) {
            return false;
        }
        LocalDateInterval underkjennIntervall = new LocalDateInterval(splitDato.plusDays(1), grunnlag.getSisteDatoForOpptjening());
        LocalDateTimeline<Boolean> underkjennTimeline = new LocalDateTimeline<>(splitDato.plusDays(1), grunnlag.getSisteDatoForOpptjening(), Boolean.TRUE);
        AktivitetMellomregning aktivitetMellomregning = mellomregning.get(aktivitet);

        aktivitetMellomregning.setAktivitetUnderkjent(underkjennTimeline);
        if (!AktivitetMellomregning.EMPTY.equals(aktivitetMellomregning.getAktivitetManueltGodkjent())) {
            // Må overskrive manuell godkjenning da annen aktivitet gjerne er vurdert i aksjonspunkt i steg 82
            aktivitetMellomregning.setAktivitetManueltGodkjent(aktivitetMellomregning.getAktivitetManueltGodkjent().disjoint(underkjennIntervall));
        }
        return true;
    }

    private Map<Aktivitet, LocalDateTimeline<Boolean>> getAntattGodkjentPerioder() {
        return getMellomregningTidslinje(AktivitetMellomregning::getAktivitetAntattGodkjent);
    }

    OpptjentTidslinje getAntattTotalOpptjening() {
        return antattTotalOpptjening;
    }

    OpptjentTidslinje getBekreftetOpptjening() {
        return bekreftetTotalOpptjening;
    }

    Opptjeningsgrunnlag getGrunnlag() {
        return grunnlag;
    }

    Map<Aktivitet, LocalDateTimeline<Long>> getInntektTidslinjer() {
        return getMellomregningTidslinje(AktivitetMellomregning::getInntektTidslinjer);
    }

    OpptjentTidslinje getTotalOpptjening() {
        return totalOpptjening;
    }

    Map<Aktivitet, LocalDateTimeline<Boolean>> getUnderkjentePerioder() {
        return getMellomregningTidslinje(AktivitetMellomregning::getAktivitetUnderkjent);
    }

    void oppdaterOutputResultat(OpptjeningsvilkårResultat outputResultat) {
        /*
         * tar ikke med antatt godkjent, mellomliggende akseptert eller underkjent i aktivitet returnert her. De angis
         * separat under.
         */
        LocalDateInterval opptjeningPeriode = getGrunnlag().getOpptjeningPeriode();
        outputResultat.setBekreftetGodkjentAktivitet(trimTidslinje(this.getAktivitetTidslinjer(false, false), opptjeningPeriode));

        outputResultat.setUnderkjentePerioder(trimTidslinje(this.getUnderkjentePerioder(), opptjeningPeriode));
        outputResultat.setAntattGodkjentePerioder(trimTidslinje(this.getAntattGodkjentPerioder(), opptjeningPeriode));
        outputResultat.setAkseptertMellomliggendePerioder(trimTidslinje(this.getAkseptertMellomliggendePerioder(), opptjeningPeriode));

        /* hvis Oppfylt/Ikke Oppfylt (men ikke "Ikke Vurdert"), så angis total opptjening som er kalkulert. */
        outputResultat.setTotalOpptjening(this.getTotalOpptjening());
        outputResultat.setFrist(this.getOpptjeningOpplysningerFrist());
    }

    LocalDate getOpptjeningOpplysningerFrist() {
        return opptjeningOpplysningerFrist;
    }

    void setOpptjeningOpplysningerFrist(LocalDate opptjeningOpplysningerFrist) {
        this.opptjeningOpplysningerFrist = opptjeningOpplysningerFrist;
    }

    void setAntattOpptjening(OpptjentTidslinje antattOpptjening) {
        this.antattTotalOpptjening = antattOpptjening;
    }

    void setBekreftetTotalOpptjening(OpptjentTidslinje opptjening) {
        this.bekreftetTotalOpptjening = opptjening;
    }

    /**
     * Endelig valt opptjeningperiode.
     */
    void setTotalOpptjening(OpptjentTidslinje totalOpptjening) {
        this.totalOpptjening = totalOpptjening;
    }

    void setAntattGodkjentePerioder(Map<Aktivitet, LocalDateTimeline<Boolean>> perioder) {
        perioder.forEach((key, value) -> mellomregning.get(key).setAktivitetAntattGodkjent(value));
    }

    void setUnderkjentePerioder(Map<Aktivitet, LocalDateTimeline<Boolean>> perioder) {
        perioder.forEach((key, value) -> mellomregning.get(key).setAktivitetUnderkjent(value));
    }

    /**
     * Sjekker om opptjening er nok til å legge på vent ifht. konfigurert minste periode for vent.
     */
    boolean sjekkErInnenforMinsteGodkjentePeriodeForVent(Period opptjeningPeriode) {
        int minsteAntallMåneder = grunnlag.getMinsteAntallMånederForVent();
        int minsteAntallDager = grunnlag.getMinsteAntallDagerForVent();
        return sjekkErErOverAntallPåkrevd(opptjeningPeriode, minsteAntallMåneder, minsteAntallDager);
    }

    /**
     * Sjekker om opptjening er nok ifht. konfigurert minste periode.
     */
    boolean sjekkErInnenforMinstePeriodeGodkjent(Period opptjeningPeriode) {
        int minsteAntallMåneder = grunnlag.getMinsteAntallMånederGodkjent();
        int minsteAntallDager = grunnlag.getMinsteAntallDagerGodkjent();
        return sjekkErErOverAntallPåkrevd(opptjeningPeriode, minsteAntallMåneder, minsteAntallDager);
    }

    private static boolean sjekkErErOverAntallPåkrevd(Period opptjentPeriode, int minsteAntallMåneder,
                                                      int minsteAntallDager) {
        return opptjentPeriode.getMonths() > minsteAntallMåneder
            || (opptjentPeriode.getMonths() == minsteAntallMåneder && opptjentPeriode.getDays() >= minsteAntallDager);
    }

    private static Map<Aktivitet, LocalDateTimeline<Boolean>> trimTidslinje(Map<Aktivitet, LocalDateTimeline<Boolean>> tidslinjer,
                                                                            LocalDateInterval maxInterval) {
        return tidslinjer.entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().intersection(maxInterval)))
            .filter(e -> !e.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
