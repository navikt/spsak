package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Beregnet extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public Beregnet(){
        super("Beregnet");
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Beregnet";
    }
}
