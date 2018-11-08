package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmPeriodeErForeldrepengerFørFødsel.ID)
public class SjekkOmPeriodeErForeldrepengerFørFødsel extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 27";

    public SjekkOmPeriodeErForeldrepengerFørFødsel() {
        super(ID);
    }


    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        if (grunnlag.getStønadskontotype() == Stønadskontotype.FORELDREPENGER_FØR_FØDSEL) {
            return ja();
        }
        return nei();
    }
}
