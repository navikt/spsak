package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBareFarMedmorHarRett.ID)
public class SjekkOmBareFarMedmorHarRett extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 36.2.2";

    public SjekkOmBareFarMedmorHarRett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return grunnlag.isFarRett() && !grunnlag.isMorRett() ? ja() : nei();
    }
}
