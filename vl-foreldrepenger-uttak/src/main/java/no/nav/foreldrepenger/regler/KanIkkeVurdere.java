package no.nav.foreldrepenger.regler;

import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class KanIkkeVurdere<T> extends LeafSpecification<T> {

    private RuleReasonRef ruleReasonRef;

    public KanIkkeVurdere(RuleReasonRef ruleReasonRef) {
        super(ruleReasonRef.getReasonCode());
        this.ruleReasonRef = ruleReasonRef;
    }

    @Override
    public Evaluation evaluate(Object grunnlag) {
        return kanIkkeVurdere(ruleReasonRef);
    }

    @Override
    public String beskrivelse() {
        return ruleReasonRef.getReasonTextTemplate();
    }

}
