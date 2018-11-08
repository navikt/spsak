package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBareMorHarRett.ID)
public class SjekkOmBareMorHarRett extends LeafSpecification<FastsettePeriodeGrunnlag> {
    public static final String ID = "FP_VK 36.2.3";

    public SjekkOmBareMorHarRett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.isMorRett() && !grunnlag.isFarRett()) {
            return ja();
        }
        return nei();
    }
}
