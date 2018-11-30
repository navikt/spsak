package no.nav.foreldrepenger.domene.inngangsvilkaar.medlemskap;

import no.nav.foreldrepenger.domene.inngangsvilkaar.PersonStatusType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkBrukerErAvklartSomBosattEllerDød.ID)
public class SjekkBrukerErAvklartSomBosattEllerDød extends LeafSpecification<MedlemskapsvilkårGrunnlag> {

    static final String ID = "FP_VK_2.1";

    static final RuleReasonRef IKKE_OPPFYLT_BRUKER_ER_UTVANDRET = new RuleReasonRefImpl("1021", "Bruker er utvandret.");

    SjekkBrukerErAvklartSomBosattEllerDød() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(MedlemskapsvilkårGrunnlag grunnlag) {
        if (PersonStatusType.UTVA.equals(grunnlag.getPersonStatusType())) {
            return nei(IKKE_OPPFYLT_BRUKER_ER_UTVANDRET);
        }
        return ja();
    }

}
