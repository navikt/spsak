package no.nav.foreldrepenger.inngangsvilkaar.medlemskap;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkBrukerErAvklartMedLovligOpphold.ID)
public class SjekkBrukerErAvklartMedLovligOpphold extends LeafSpecification<MedlemskapsvilkårGrunnlag> {

    static final String ID = "FP_VK_2.12.1";

    static final RuleReasonRef IKKE_OPPFYLT_BRUKER_HAR_IKKE_LOVLIG_OPPHOLD = new RuleReasonRefImpl("1023", "Bruker har ikke lovlig opphold.");

    SjekkBrukerErAvklartMedLovligOpphold() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(MedlemskapsvilkårGrunnlag grunnlag) {
        if (grunnlag.isBrukerAvklartLovligOppholdINorge()) {
            return ja();
        }
        return nei(IKKE_OPPFYLT_BRUKER_HAR_IKKE_LOVLIG_OPPHOLD);
    }
}
