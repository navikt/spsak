package no.nav.foreldrepenger.inngangsvilkaar.adopsjon;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.AdopsjonsvilkårGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkOmStønadsperiodeForAnnenForelderErBruktOpp extends LeafSpecification<AdopsjonsvilkårGrunnlag> {

    static final String ID = "FP_VK_16";

    static final RuleReasonRef STEBARNSADOPSJON_IKKE_FLERE_DAGER_IGJEN = new RuleReasonRefImpl("1051", "Stebarnsadopsjon ikke flere dager igjen");


    SjekkOmStønadsperiodeForAnnenForelderErBruktOpp() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(AdopsjonsvilkårGrunnlag grunnlag) {
        if (grunnlag.getErStønadsperiodeBruktOpp()) {
            return ja();
        }
        // hvis stønadsperioden ikke er brukt opp = det er flere dager igjen
        return nei();
    }
}
