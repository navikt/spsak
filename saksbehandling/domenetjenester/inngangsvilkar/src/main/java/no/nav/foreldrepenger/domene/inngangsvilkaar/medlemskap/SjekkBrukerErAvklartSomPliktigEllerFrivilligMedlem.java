package no.nav.foreldrepenger.domene.inngangsvilkaar.medlemskap;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkBrukerErAvklartSomPliktigEllerFrivilligMedlem.ID)
public class SjekkBrukerErAvklartSomPliktigEllerFrivilligMedlem extends LeafSpecification<MedlemskapsvilkårGrunnlag> {

    static final String ID = "FP_VK_2.2";

    SjekkBrukerErAvklartSomPliktigEllerFrivilligMedlem() {
        super(ID);
    }


    @Override
    public Evaluation evaluate(MedlemskapsvilkårGrunnlag grunnlag) {

        if (grunnlag.isBrukerAvklartPliktigEllerFrivillig()) {
            return ja();
        }
        return nei();
    }
}
