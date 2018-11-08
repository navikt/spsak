package no.nav.foreldrepenger.inngangsvilkaar.opptjening;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Aktivitet;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjentTidslinje;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.doc.RuleOutcomeDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

/**
 * Sjekk om bruker har tilstrekkelig opptjening inklusiv antatt godkjente perioder for arbeidsforhold uten innrapportert
 * inntekt ennå.
 * <p>
 * (antar at dersom bruker ikke trenger antatt godkjente perioder er det sjekket av tidligere regel)
 */
@RuleDocumentation(value = "FP_VK_23.2.2", outcomes = {
        @RuleOutcomeDocumentation(code = SjekkTilstrekkeligOpptjeningInklAntatt.LEGG_PÅ_VENT_ID, result = Resultat.IKKE_VURDERT, description = "Ikke tilstrekkelig opptjening, men kan oppnås hvis inntekt kommer for siste måneder senere."),
        @RuleOutcomeDocumentation(code = SjekkTilstrekkeligOpptjeningInklAntatt.IKKE_TILSTREKKELIG_OPPTJENING_ID, result = Resultat.NEI, description = "Ikke tilstrekkelig opptjening")
})
public class SjekkTilstrekkeligOpptjeningInklAntatt extends LeafSpecification<OpptjeningsvilkårMellomregning> {

    public static final String ID = SjekkTilstrekkeligOpptjeningInklAntatt.class.getSimpleName();

    static final String IKKE_TILSTREKKELIG_OPPTJENING_ID = "1035";
    public static final RuleReasonRefImpl IKKE_TILSTREKKELIG_OPPTJENING = new RuleReasonRefImpl(IKKE_TILSTREKKELIG_OPPTJENING_ID,
        "Ikke tilstrekkelig opptjening. Har opptjening: {0}");

    static final String LEGG_PÅ_VENT_ID = "7006";
    static final RuleReasonRefImpl LEGG_PÅ_VENT = new RuleReasonRefImpl(LEGG_PÅ_VENT_ID,
        "Ikke p.t. tilstrekkelig opptjening, men kan oppnå hvis det sjekkes senere. Har opptjening: {0}, frist for innsending: {1}");

    public SjekkTilstrekkeligOpptjeningInklAntatt() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(OpptjeningsvilkårMellomregning data) {
        Period antattTotalOpptjening = data.getAntattTotalOpptjening().getOpptjentPeriode();
        Period bekreftetOpptjeningPeriode = data.getBekreftetOpptjening().getOpptjentPeriode();

        if (data.sjekkErInnenforMinstePeriodeGodkjent(bekreftetOpptjeningPeriode)) {
            // quick return, skal være håndtert av tidligere regel.
            data.setTotalOpptjening(data.getBekreftetOpptjening());
            return ja();
        }

        if (data.sjekkErInnenforMinstePeriodeGodkjent(antattTotalOpptjening)) {
            // sjekk at bekreftet periode er minst 4 måneder
            if (data.sjekkErInnenforMinsteGodkjentePeriodeForVent(bekreftetOpptjeningPeriode)) {
                if (manglerInntektForSisteMånederIOpptjeningsperiodenINoenArbeidsforhold(data)) {
                    LocalDate fristForOpptjeningsopplysninger = beregnFristForOpptjeningsopplysninger(data);
                    data.setOpptjeningOpplysningerFrist(fristForOpptjeningsopplysninger);

                    Evaluation evaluation = kanIkkeVurdere(LEGG_PÅ_VENT, bekreftetOpptjeningPeriode, fristForOpptjeningsopplysninger);
                    loggAntattOpptjeningPeriode(data, evaluation);
                    return evaluation;
                }
            }
        }

        data.setTotalOpptjening(data.getBekreftetOpptjening());

        Evaluation evaluation = nei(IKKE_TILSTREKKELIG_OPPTJENING, bekreftetOpptjeningPeriode);
        loggAntattOpptjeningPeriode(data, evaluation);
        return evaluation;
    }

    private LocalDate beregnFristForOpptjeningsopplysninger(OpptjeningsvilkårMellomregning data) {
        LocalDate skjæringstidspunkt = data.getGrunnlag().getSisteDatoForOpptjening();

        // first er 5 i måned etter skjæringstidspunktet
        LocalDate frist = skjæringstidspunkt.plusMonths(1).withDayOfMonth(5);
        return frist;
    }

    private boolean manglerInntektForSisteMånederIOpptjeningsperiodenINoenArbeidsforhold(OpptjeningsvilkårMellomregning data) {
        LocalDate sisteDatoIOpptjening = data.getGrunnlag().getSisteDatoForOpptjening();

        Map<Aktivitet, LocalDateTimeline<Boolean>> åpneArbeidsforhold;
        åpneArbeidsforhold = data.getAktivitetTidslinjer(true, true)
            .entrySet().stream()
            .filter(e -> e.getValue().getMaxLocalDate().isAfter(sisteDatoIOpptjening))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        LocalDate startSiste2måneder = sisteDatoIOpptjening.minusMonths(1).withDayOfMonth(1);
        LocalDateInterval sisteParMåneder = new LocalDateInterval(startSiste2måneder, sisteDatoIOpptjening);

        LocalDateTimeline<Boolean> manglendeOpptjeningPerioder = new LocalDateTimeline<Boolean>(sisteParMåneder, Boolean.TRUE)
            .disjoint(data.getBekreftetOpptjening().getTidslinje(), StandardCombinators::leftOnly);

        Map<Aktivitet, LocalDateTimeline<Boolean>> mulighetForInntekt = new LinkedHashMap<>();
        data.getInntektTidslinjer().entrySet()
            .stream().filter(e -> åpneArbeidsforhold.containsKey(e.getKey()))
            .forEach(e -> {
                LocalDateTimeline<Long> inntektSisteTid = e.getValue().intersection(sisteParMåneder);
                LocalDateTimeline<Boolean> muligInntektRapportering = manglendeOpptjeningPerioder.disjoint(inntektSisteTid, StandardCombinators::leftOnly);
                if (!muligInntektRapportering.isEmpty()) {
                    mulighetForInntekt.put(e.getKey(), muligInntektRapportering);
                }
            });

            boolean manglerInntektRapportertSisteParMåneder = !mulighetForInntekt.isEmpty();
            return manglerInntektRapportertSisteParMåneder;
    }

    private void loggAntattOpptjeningPeriode(OpptjeningsvilkårMellomregning data, Evaluation ev) {
        OpptjentTidslinje antattTotalOpptjening = data.getAntattTotalOpptjening();
        ev.setEvaluationProperty(Opptjeningsvilkår.EVAL_RESULT_ANTATT_AKTIVITET_TIDSLINJE, antattTotalOpptjening.getTidslinje());
        ev.setEvaluationProperty(Opptjeningsvilkår.EVAL_RESULT_ANTATT_GODKJENT, antattTotalOpptjening.getOpptjentPeriode());
        if (data.getOpptjeningOpplysningerFrist() != null) {
            ev.setEvaluationProperty(Opptjeningsvilkår.EVAL_RESULT_FRIST_FOR_OPPTJENING_OPPLYSNINGER, data.getOpptjeningOpplysningerFrist());
        }
    }

}
