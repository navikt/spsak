package no.nav.foreldrepenger.domene.inngangsvilkaar.medlemskap;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkBrukerErAvklartSomIkkeMedlem.ID)
public class SjekkBrukerErAvklartSomIkkeMedlem extends LeafSpecification<MedlemskapsvilkårGrunnlag> {

    static final String ID = "FP_VK_2.13";

    static final RuleReasonRef IKKE_OPPFYLT_BRUKER_ER_OPPFØRT_SOM_IKKE_MEDLEM = new RuleReasonRefImpl("1020", "Bruker er registrert som ikke medlem.");

    SjekkBrukerErAvklartSomIkkeMedlem() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(MedlemskapsvilkårGrunnlag grunnlag) {
        if (!grunnlag.isBrukerErMedlem()) {
            return ja(IKKE_OPPFYLT_BRUKER_ER_OPPFØRT_SOM_IKKE_MEDLEM);
        }
        return nei();
    }
}
