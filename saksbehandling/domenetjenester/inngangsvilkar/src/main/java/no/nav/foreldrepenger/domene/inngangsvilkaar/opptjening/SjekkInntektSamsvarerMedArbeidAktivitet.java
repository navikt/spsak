package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening;

import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Aktivitet;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.spsak.tidsserie.LocalDateInterval;
import no.nav.spsak.tidsserie.LocalDateSegment;
import no.nav.spsak.tidsserie.LocalDateTimeline;
import no.nav.spsak.tidsserie.LocalDateTimeline.JoinStyle;
import no.nav.spsak.tidsserie.StandardCombinators;

/**
 * Regel som sjekker om det finnes registrerte inntekter for de periodene arbeid er innrapportert, for samme
 * arbeidsgiver.
 * Perioder der det ikke finnes inntekter underkjennes som aktivitet.
 */
@RuleDocumentation("FP_VK_23.1.1")
public class SjekkInntektSamsvarerMedArbeidAktivitet extends LeafSpecification<OpptjeningsvilkårMellomregning> {
    public static final String ID = SjekkInntektSamsvarerMedArbeidAktivitet.class.getSimpleName();

    private static final String ARBEID = Opptjeningsvilkår.ARBEID;

    public SjekkInntektSamsvarerMedArbeidAktivitet() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(OpptjeningsvilkårMellomregning data) {

        Map<Aktivitet, LocalDateTimeline<AktivitetStatus>> ikkeGodkjent = finnPerioderSomIkkeHarNokInntektForOpplystArbeid(data);

        LocalDate sisteAntattGodkjentDato = sisteAntattGodkjentDato(data);

        // regn utførste dato for antatt godkjent bakover
        Period periodeAntattGodkjentAksepteres = data.getGrunnlag().getPeriodeAntattGodkjentFørBehandlingstidspunkt();
        LocalDate førsteDatoForAntattGodkjent = sisteAntattGodkjentDato
            .plusMonths(1).withDayOfMonth(1) // Periode P2M blir denne måneden (enn så lenge) og forrige måned
            .minus(periodeAntattGodkjentAksepteres);

        LocalDateInterval antattGodkjentInterval = new LocalDateInterval(førsteDatoForAntattGodkjent, sisteAntattGodkjentDato);

        AntaGodkjent antaGodkjent = new AntaGodkjent(antattGodkjentInterval, ikkeGodkjent);

        LocalDateInterval opptjeningPeriode = data.getGrunnlag().getOpptjeningPeriode();

        data.setAntattGodkjentePerioder(antaGodkjent.getAntattGodkjentResultat(opptjeningPeriode));
        data.setUnderkjentePerioder(antaGodkjent.getUnderkjentResultat(opptjeningPeriode));

        Evaluation evaluation = ja();

        evaluation.setEvaluationProperty(Opptjeningsvilkår.EVAL_RESULT_UNDERKJENTE_PERIODER, data.getUnderkjentePerioder());

        return evaluation;
    }

    private static LocalDate sisteAntattGodkjentDato(OpptjeningsvilkårMellomregning data) {
        if(data.getGrunnlag().getBehandlingsTidspunkt().isAfter(data.getGrunnlag().getSisteDatoForOpptjening())) {
            return data.getGrunnlag().getBehandlingsTidspunkt();
        }
        return data.getGrunnlag().getSisteDatoForOpptjening();
    }

    private Map<Aktivitet, LocalDateTimeline<AktivitetStatus>> finnPerioderSomIkkeHarNokInntektForOpplystArbeid(OpptjeningsvilkårMellomregning data) {

        Map<Aktivitet, LocalDateTimeline<Boolean>> aktiviteter = data.getAktivitetTidslinjer(false, false);
        Map<Aktivitet, LocalDateTimeline<Long>> inntekter = data.getInntektTidslinjer();
        Opptjeningsgrunnlag grunnlag = data.getGrunnlag();
        UnderkjennPerioder underkjennPerioder = new UnderkjennPerioder(inntekter, grunnlag.getMinsteInntekt());

        aktiviteter.entrySet().stream()
            .filter(e -> ARBEID.equals(e.getKey().getAktivitetType()))
            .forEach(underkjennPerioder::underkjennPeriode);

        return underkjennPerioder.getUnderkjentePerioder();
    }

    /**
     * Underkjenn perioder som ikke matcher filter funksjon
     */
    private static class UnderkjennPerioder {
        private final Map<Aktivitet, LocalDateTimeline<AktivitetStatus>> underkjentePerioder = new HashMap<>();
        private Map<Aktivitet, LocalDateTimeline<Long>> inntekter;
        private Long minsteInntekt;

        UnderkjennPerioder(Map<Aktivitet, LocalDateTimeline<Long>> inntekter, Long minsteInntekt) {
            this.inntekter = inntekter;
            this.minsteInntekt = minsteInntekt;
        }

        Map<Aktivitet, LocalDateTimeline<AktivitetStatus>> getUnderkjentePerioder() {
            return Collections.unmodifiableMap(underkjentePerioder);
        }

        void underkjennPeriode(Entry<Aktivitet, LocalDateTimeline<Boolean>> e) {
            Aktivitet key = e.getKey();
            LocalDateTimeline<Boolean> arbeid = e.getValue();
            LocalDateTimeline<AktivitetStatus> periode;
            if (!inntekter.containsKey(key)) {
                periode = arbeid.mapValue(a -> AktivitetStatus.IKKE_GODKJENT);
            } else {

                LocalDateTimeline<Long> inntekt = inntekter.get(key);

                LocalDateTimeline<Long> okInntekt = inntekt
                    .filterValue(this::filtrerInntektFunksjon);

                LocalDateTimeline<Boolean> okArbeid = okInntekt.intersection(arbeid,
                    StandardCombinators::rightOnly);

                LocalDateTimeline<Boolean> underkjentArbeid = arbeid.disjoint(okArbeid,
                    StandardCombinators::leftOnly);

                periode = underkjentArbeid.mapValue(a -> AktivitetStatus.IKKE_GODKJENT);
            }

            underkjentePerioder.put(key, periode);

        }

        private boolean filtrerInntektFunksjon(Long inntekt) {
            /* må ha minst inntekt */
            return inntekt >= minsteInntekt;
        }

    }

    /**
     * FYll antatt godkjente intervaller for arbeid som har blitt underkjent inngen angitt interval.
     */
    private static class AntaGodkjent {
        /**
         * Mulig intervall med antatt godkjent.
         */
        private final LocalDateTimeline<AktivitetStatus> antattGodkjent;
        private final Map<Aktivitet, LocalDateTimeline<AktivitetStatus>> medAntattGodkjentFramforIkkeGodkjent = new LinkedHashMap<>();

        /**
         * @param antattGodkjentInterval - interval der arbeid skal antas godkjent selv om det er underkjent av tidligere regler ang. krav
         *            til inntekt.
         * @param aktiviteter
         */
        AntaGodkjent(final LocalDateInterval antattGodkjentInterval, final Map<Aktivitet, LocalDateTimeline<AktivitetStatus>> aktiviteter) {
            antattGodkjent = new LocalDateTimeline<>(antattGodkjentInterval, AktivitetStatus.ANTATT_GODKJENT);

            aktiviteter.entrySet().stream()
                .filter(e -> ARBEID.equals(e.getKey().getAktivitetType()))
                .forEach(this::fyllAntattGodkjent);

        }

        private void fyllAntattGodkjent(Entry<Aktivitet, LocalDateTimeline<AktivitetStatus>> e) {
            Aktivitet key = e.getKey();
            LocalDateTimeline<AktivitetStatus> arbeid = e.getValue();

            // la antatt godkjent overstyre ikke godkjent.
            LocalDateTimeline<AktivitetStatus> medAntattGodkjent = arbeid.combine(antattGodkjent,
                this::antaGodkjentFramforIkkeGodkjent, JoinStyle.LEFT_JOIN);

            if (!medAntattGodkjent.isEmpty()) {
                medAntattGodkjentFramforIkkeGodkjent.put(key, medAntattGodkjent);
            }
        }

        private LocalDateSegment<AktivitetStatus> antaGodkjentFramforIkkeGodkjent(LocalDateInterval di,
                                                                                  LocalDateSegment<AktivitetStatus> lhs,
                                                                                  LocalDateSegment<AktivitetStatus> rhs) {
            AktivitetStatus nyStatus = lhs == null || Objects.equals(lhs.getValue(), AktivitetStatus.IKKE_GODKJENT)
                ? (rhs == null ? (lhs == null ? null : lhs.getValue()) : rhs.getValue())
                : lhs.getValue();
            return new LocalDateSegment<>(di, nyStatus);
        }

        Map<Aktivitet, LocalDateTimeline<Boolean>> getAntattGodkjentResultat(LocalDateInterval interval) {
            Map<Aktivitet, LocalDateTimeline<Boolean>> resultat = medAntattGodkjentFramforIkkeGodkjent.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> filtrertForStatus(e.getValue(), AktivitetStatus.ANTATT_GODKJENT)));

            return avgrensTilPeriode(resultat, interval);
        }

        Map<Aktivitet, LocalDateTimeline<Boolean>> getUnderkjentResultat(LocalDateInterval interval) {
            Map<Aktivitet, LocalDateTimeline<Boolean>> resultat = medAntattGodkjentFramforIkkeGodkjent.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> filtrertForStatus(e.getValue(), AktivitetStatus.IKKE_GODKJENT)));
            return avgrensTilPeriode(resultat, interval);
        }

        /** avgrens til angitt interval og fjern tomme tidslinjer */
        private static Map<Aktivitet, LocalDateTimeline<Boolean>> avgrensTilPeriode(Map<Aktivitet, LocalDateTimeline<Boolean>> tidslinjer,
                                                                                    LocalDateInterval interval) {

            return tidslinjer
                .entrySet()
                .stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().intersection(interval)))
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }

        private static LocalDateTimeline<Boolean> filtrertForStatus(LocalDateTimeline<AktivitetStatus> tidslinje, AktivitetStatus aktivitetStatus) {
            return tidslinje.filterValue(a -> Objects.equals(a, aktivitetStatus)).mapValue(a -> Boolean.TRUE);
        }

    }

}
