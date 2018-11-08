package no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.Dekningsgrad;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDekningsgradEr100.ID)
public class SjekkOmDekningsgradEr100 extends LeafSpecification<BeregnKontoerGrunnlag> {

    public static final String ID = "FP_VK 17.1.9";

    public SjekkOmDekningsgradEr100() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag beregnKontoerGrunnlag) {
        if (beregnKontoerGrunnlag.getDekningsgrad() == Dekningsgrad.DEKNINGSGRAD_100) {
            return ja();
        }
        return nei();
    }
}
