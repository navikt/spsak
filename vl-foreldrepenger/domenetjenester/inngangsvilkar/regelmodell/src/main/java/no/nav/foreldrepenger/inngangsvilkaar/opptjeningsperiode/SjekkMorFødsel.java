package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.SoekerRolle;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkMorFødsel.ID)
public class SjekkMorFødsel extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK 21.2";
    static final String BESKRIVELSE = "Er mor søker?";

    SjekkMorFødsel() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        return regelmodell.getSøkerRolle().equals(SoekerRolle.MORA) ? ja() : nei();
    }
}
