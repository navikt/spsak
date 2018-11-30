package no.nav.foreldrepenger.domene.inngangsvilkaar.medlemskap;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkBrukerErAvklartMedOppholdsrett.ID)
public class SjekkBrukerErAvklartMedOppholdsrett extends LeafSpecification<MedlemskapsvilkårGrunnlag> {

    static final String ID = "FP_VK_2.12.2"; //TODO: skal det være samme ID som lovlig opphold???

    static final RuleReasonRef IKKE_OPPFYLT_BRUKER_HAR_IKKE_OPPHOLDSRETT = new RuleReasonRefImpl("1024", "Bruker har ikke oppholdsrett.");

    SjekkBrukerErAvklartMedOppholdsrett() {
        super(ID);
    }


    @Override
    public Evaluation evaluate(MedlemskapsvilkårGrunnlag grunnlag) {
        if (grunnlag.isBrukerAvklartOppholdsrett()) {
            return ja();
        }
        return nei(IKKE_OPPFYLT_BRUKER_HAR_IKKE_OPPHOLDSRETT);
    }
}
