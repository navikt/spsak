package no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger;

import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class BeregnetFeriepenger extends LeafSpecification<BeregningsresultatFeriepengerRegelModell> {

    public BeregnetFeriepenger(){
        super("Beregnet");
    }

    @Override
    public Evaluation evaluate(BeregningsresultatFeriepengerRegelModell grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Beregnet";
    }
}
