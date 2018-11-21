package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette;

import java.math.BigDecimal;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkBeregningsgrunnlagStørreEnn6G.ID)
class SjekkBeregningsgrunnlagStørreEnn6G extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.4";
    static final String BESKRIVELSE = "Er beregningsgrunnlag større enn 6G";

    SjekkBeregningsgrunnlagStørreEnn6G() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        BigDecimal bruttoInklBortfaltNaturalytelsePrÅr = grunnlag.getBruttoPrÅrInkludertNaturalytelser();
        BigDecimal seksG = grunnlag.getRedusertGrunnbeløp().multiply(BigDecimal.valueOf(6));
        SingleEvaluation resultat = bruttoInklBortfaltNaturalytelsePrÅr.compareTo(seksG) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("bruttoPrÅr", bruttoInklBortfaltNaturalytelsePrÅr);
        resultat.setEvaluationProperty("redusertGrunnbeløp", grunnlag.getRedusertGrunnbeløp());
        resultat.setEvaluationProperty("seksG", seksG);
        return resultat;
    }
}
