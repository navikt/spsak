package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkSøktOmTermin extends LeafSpecification<FødselsvilkårGrunnlag> {

    private static final String ID = SjekkSøktOmTermin.class.getSimpleName();

    static final RuleReasonRefImpl IKKE_OPPFYLT_BARN_DOKUMENTERT_PÅ_FAR_MEDMOR = new RuleReasonRefImpl("1027",
        "Søker er ikke dokumentert som barnets far/medmor");

    SjekkSøktOmTermin() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FødselsvilkårGrunnlag t) {
        if (t.isErSøktOmTermin()) {
            return ja();
        } else {
            return nei();
        }
    }
}
