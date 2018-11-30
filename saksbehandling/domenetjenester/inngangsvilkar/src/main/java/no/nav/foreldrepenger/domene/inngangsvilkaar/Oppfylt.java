package no.nav.foreldrepenger.domene.inngangsvilkaar;

import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@SuppressWarnings("rawtypes")
public class Oppfylt extends LeafSpecification {

    public Oppfylt(){
        super("Oppfylt"); //TODO: skal det være noe annet her?
    }
    @Override
    public Evaluation evaluate(Object grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Oppfylt";
    }
}
