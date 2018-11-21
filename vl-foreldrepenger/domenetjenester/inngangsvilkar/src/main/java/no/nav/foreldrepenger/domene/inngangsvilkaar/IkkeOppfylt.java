package no.nav.foreldrepenger.domene.inngangsvilkaar;

import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.specification.LeafSpecification;

@SuppressWarnings("rawtypes")
public class IkkeOppfylt extends LeafSpecification {

    private RuleReasonRef ruleReasonRef;

    public IkkeOppfylt(RuleReasonRef ruleReasonRef){
        super(ruleReasonRef.getReasonCode());
        this.ruleReasonRef = ruleReasonRef;
    }
    @Override
    public Evaluation evaluate(Object grunnlag) {
        return nei(ruleReasonRef);
    }

    @Override
    public String beskrivelse() {
        return ruleReasonRef.getReasonTextTemplate();
    }

}
