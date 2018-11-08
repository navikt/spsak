package no.nav.foreldrepenger.inngangsvilkaar.adopsjon;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.AdopsjonsvilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkMannAdoptererAlene.ID)
class SjekkMannAdoptererAlene extends LeafSpecification<AdopsjonsvilkårGrunnlag> {

    static final String ID = "FP_VK_4.2";

    static final RuleReasonRefImpl IKKE_OPPFYLT_MANN_ADOPTERER_IKKE_ALENE = new RuleReasonRefImpl("1006", "Mann adopterer ikke alene.");

    SjekkMannAdoptererAlene() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(AdopsjonsvilkårGrunnlag grunnlag) {
        if (grunnlag.isMannAdoptererAlene()) {
            return ja();
        }
        return nei(IKKE_OPPFYLT_MANN_ADOPTERER_IKKE_ALENE);
    }
}
