package no.nav.foreldrepenger.regler;

import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@SuppressWarnings("rawtypes")
public class Oppfylt<T> extends LeafSpecification<T> {

    public Oppfylt() {
        super("Oppfylt");
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
