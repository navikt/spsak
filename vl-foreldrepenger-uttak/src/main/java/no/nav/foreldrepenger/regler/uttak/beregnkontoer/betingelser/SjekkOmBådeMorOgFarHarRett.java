package no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBådeMorOgFarHarRett.ID)
public class SjekkOmBådeMorOgFarHarRett extends LeafSpecification<BeregnKontoerGrunnlag> {
    public static final String ID = "FP_VK 17.1.3";

    public SjekkOmBådeMorOgFarHarRett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag grunnlag) {
        if (grunnlag.isMorRett() && grunnlag.isFarRett()) {
            return ja();
        }
        return nei();
    }
}
