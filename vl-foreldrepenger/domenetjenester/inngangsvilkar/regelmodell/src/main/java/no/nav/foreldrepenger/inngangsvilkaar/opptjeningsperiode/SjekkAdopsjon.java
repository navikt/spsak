package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.FagsakÅrsak;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkAdopsjon.ID)
public class SjekkAdopsjon extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK 21.10";
    static final String BESKRIVELSE = "Gjelder det adopsjon?";

    SjekkAdopsjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        return regelmodell.getFagsakÅrsak().equals(FagsakÅrsak.ADOPSJON) ? ja() : nei();
    }
}
