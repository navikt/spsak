package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjeningsperiode;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class IkkeGyldigUtgang extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK 21";
    static final String BESKRIVELSE = "Ikke gyldig utgang";

    IkkeGyldigUtgang() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        return nei();
    }
}

