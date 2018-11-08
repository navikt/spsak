package no.nav.foreldrepenger.inngangsvilkaar.adopsjon;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.AdopsjonsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.Kjoenn;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkKvinneAdopterer.ID)
class SjekkKvinneAdopterer extends LeafSpecification<AdopsjonsvilkårGrunnlag> {

    static final String ID = "FP_VK_4.1";

    SjekkKvinneAdopterer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(AdopsjonsvilkårGrunnlag grunnlag) {
        if (grunnlag.getSoekersKjonn().equals(Kjoenn.KVINNE)) {
            return ja();
        }
        return nei();
    }

}
