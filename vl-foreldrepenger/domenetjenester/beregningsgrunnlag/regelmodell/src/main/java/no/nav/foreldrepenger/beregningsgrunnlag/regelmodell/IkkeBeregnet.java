package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class IkkeBeregnet extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private RuleReasonRef ruleReasonRef;

    public IkkeBeregnet(RuleReasonRef ruleReasonRef){
        super(ruleReasonRef.getReasonCode());
        this.ruleReasonRef = ruleReasonRef;
    }
    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return nei(ruleReasonRef);
    }

    @Override
    public String beskrivelse() {
        return ruleReasonRef.getReasonTextTemplate();
    }
}
