package no.nav.foreldrepenger.beregningsgrunnlag.fastsette;

import java.math.BigDecimal;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkBeregningsgrunnlagMindreEnn.ID)
class SjekkBeregningsgrunnlagMindreEnn extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_VK_32.1";
    static final String BESKRIVELSE = "Er beregningsgrunnlag mindre enn en 0,5G?";
    private BigDecimal antallGrunnbeløp;

    SjekkBeregningsgrunnlagMindreEnn(BigDecimal antallGrunnbeløp) {
        super(ID, BESKRIVELSE);
        this.antallGrunnbeløp = antallGrunnbeløp;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal beløp = grunnlag.getGrunnbeløp().multiply(antallGrunnbeløp);

        SingleEvaluation resultat = (grunnlag.getBruttoPrÅrInkludertNaturalytelser().compareTo(beløp) < 0) ? ja() : nei();
        resultat.setEvaluationProperty("grunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("gittBeløp", beløp);
        resultat.setEvaluationProperty("bruttoPrÅr", grunnlag.getBruttoPrÅr());
        return resultat;
    }
}
