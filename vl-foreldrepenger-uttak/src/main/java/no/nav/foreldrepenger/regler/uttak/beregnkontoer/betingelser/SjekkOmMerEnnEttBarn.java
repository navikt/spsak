package no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMerEnnEttBarn.ID)
public class SjekkOmMerEnnEttBarn extends LeafSpecification<BeregnKontoerGrunnlag> {

    public static final String ID = "FP_VK 17.1.2";

    public SjekkOmMerEnnEttBarn() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag grunnlag) {
        if (grunnlag.getAntallBarn() > 1) {
            return ja();
        }
        return nei();
    }
}
