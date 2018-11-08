package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodePropertyType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class FastsettePeriodeUtfall extends LeafSpecification<FastsettePeriodeGrunnlag> {
    private final UtfallType utfallType;
    private final RuleReasonRef ruleReasonRef;
    private final List<BiConsumer<SingleEvaluation, FastsettePeriodeGrunnlag>> utfallSpesifiserere;

    private FastsettePeriodeUtfall(String id, UtfallType utfallType, RuleReasonRef ruleReasonRef, List<BiConsumer<SingleEvaluation, FastsettePeriodeGrunnlag>> utfallSpesifiserere) {
        super(id);
        if (utfallType == null) {
            throw new IllegalArgumentException("UtfallType kan ikke være null.");
        }
        this.utfallType = utfallType;
        this.ruleReasonRef = ruleReasonRef;
        this.utfallSpesifiserere = utfallSpesifiserere;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (!grunnlag.getAktuellPeriode().isPresent()) {
            throw new IllegalArgumentException("Grunnlag mangler aktuell periode.");
        }
        SingleEvaluation utfall = getHovedUtfall();
        spesifiserUtfall(utfall, grunnlag);
        return utfall;
    }

    private void spesifiserUtfall(SingleEvaluation utfall, FastsettePeriodeGrunnlag grunnlag) {
        if (utfallSpesifiserere.isEmpty()) {
            return;
        }
        utfall.setEvaluationProperties(new HashMap<>());
        utfallSpesifiserere.forEach(utfallSpesifiserer -> utfallSpesifiserer.accept(utfall, grunnlag));
    }

    private SingleEvaluation getHovedUtfall() {
        switch (utfallType) {
            case INNVILGET:
                return ja();
            case AVSLÅTT:
            case MANUELL_BEHANDLING:
                return nei(ruleReasonRef);
            default:
                throw new IllegalStateException("Ugyldig UtfallType: " + utfallType);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UtfallType hovedUtfall;
        private RuleReasonRef ruleReasonRef;
        private String id;
        private List<BiConsumer<SingleEvaluation, FastsettePeriodeGrunnlag>> utfallSpesifiserere = new ArrayList<>();

        public Builder ikkeOppfylt(IkkeOppfyltÅrsak årsak) {
            this.hovedUtfall = UtfallType.AVSLÅTT;
            this.ruleReasonRef = new RuleReasonRefImpl(String.valueOf(årsak.getId()), årsak.getBeskrivelse());
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.getEvaluationProperties().put(FastsettePeriodePropertyType.UTFALL, UtfallType.AVSLÅTT);
                singleEvaluation.getEvaluationProperties().put(FastsettePeriodePropertyType.AVKLARING_ÅRSAK,  årsak);
            });
            return this;
        }

        public Builder oppfylt(InnvilgetÅrsak innvilgetÅrsak) {
            this.hovedUtfall = UtfallType.INNVILGET;
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.getEvaluationProperties().put(FastsettePeriodePropertyType.UTFALL, UtfallType.INNVILGET);
                singleEvaluation.getEvaluationProperties().put(FastsettePeriodePropertyType.INNVILGET_ÅRSAK, innvilgetÅrsak);
            });
            return this;
        }


        public Builder medId(String id) {
            this.id = id;
            return this;
        }

        public Builder medKnekkpunkt(Function<FastsettePeriodeGrunnlag, LocalDate> finnKnekkpunkt) {
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> singleEvaluation.getEvaluationProperties()
                .put(FastsettePeriodePropertyType.KNEKKPUNKT, finnKnekkpunkt.apply(grunnlag)));
            return this;
        }

        public Builder medTrekkDagerFraSaldo(boolean trekkDagerFraSaldo) {
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> singleEvaluation.getEvaluationProperties()
                    .put(FastsettePeriodePropertyType.TREKK_DAGER_FRA_SALDO, trekkDagerFraSaldo));
            return this;
        }

        public Builder medAvslåttGradering(GraderingIkkeInnvilgetÅrsak graderingAvslagÅrsak) {
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.getEvaluationProperties().put(FastsettePeriodePropertyType.GRADERING, UtfallType.AVSLÅTT);
                singleEvaluation.getEvaluationProperties().put(FastsettePeriodePropertyType.GRADERING_IKKE_OPPFYLT_ÅRSAK, graderingAvslagÅrsak);
            });
            return this;
        }

        public Builder manuellBehandling(IkkeOppfyltÅrsak ikkeOppfyltÅrsak, Manuellbehandlingårsak manuellbehandlingårsak) {
            this.hovedUtfall = UtfallType.MANUELL_BEHANDLING;
            if (ikkeOppfyltÅrsak!=null) {
                this.ruleReasonRef = new RuleReasonRefImpl(String.valueOf(ikkeOppfyltÅrsak.getId()), ikkeOppfyltÅrsak.getBeskrivelse());
            }
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> {
                singleEvaluation.getEvaluationProperties().put(FastsettePeriodePropertyType.UTFALL, UtfallType.MANUELL_BEHANDLING);
                singleEvaluation.getEvaluationProperties().put(FastsettePeriodePropertyType.MANUELL_BEHANDLING_ÅRSAK, manuellbehandlingårsak);
                singleEvaluation.getEvaluationProperties().put(FastsettePeriodePropertyType.AVKLARING_ÅRSAK, ikkeOppfyltÅrsak);
            });
            return this;
        }

        public FastsettePeriodeUtfall create() {
            return new FastsettePeriodeUtfall(id, hovedUtfall, ruleReasonRef, utfallSpesifiserere);
        }

        public Builder utbetal(boolean utbetal) {
            this.utfallSpesifiserere.add((singleEvaluation, grunnlag) -> singleEvaluation.getEvaluationProperties()
                    .put(FastsettePeriodePropertyType.UTBETAL, utbetal));
            return this;
        }
    }
}
