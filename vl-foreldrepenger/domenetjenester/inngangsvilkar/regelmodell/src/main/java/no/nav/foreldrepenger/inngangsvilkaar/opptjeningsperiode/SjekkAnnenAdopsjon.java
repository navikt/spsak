package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.SoekerRolle;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkAnnenAdopsjon.ID)
public class SjekkAnnenAdopsjon extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK 21.4";
    static final String BESKRIVELSE = "Det er far som søker og han skal starte uttaket av foreldrepenger?";

    SjekkAnnenAdopsjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        return regelmodell.getSøkerRolle().equals(SoekerRolle.FARA) ? ja() : nei();
    }
}
