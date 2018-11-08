package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class BeregnetTY extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    public BeregnetTY(){
        super("Beregnet");
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Beregnet";
    }
}
