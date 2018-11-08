package no.nav.foreldrepenger.beregning.regelmodell;

import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Beregnet extends LeafSpecification<BeregningsresultatRegelmodellMellomregning> {

    public Beregnet(){
        super("Beregnet");
    }

    @Override
    public Evaluation evaluate(BeregningsresultatRegelmodellMellomregning grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Beregnet";
    }
}
