package no.nav.foreldrepenger.inngangsvilkaar.medlemskap;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmSaksbehandlerHarSattOpphørPåGrunnAvEndringerITps.ID)
public class SjekkOmSaksbehandlerHarSattOpphørPåGrunnAvEndringerITps extends LeafSpecification<MedlemskapsvilkårGrunnlag> {

    static final String ID = "FP_VK_2.14";

    static final RuleReasonRef IKKE_OPPFYLT_SAKSBEHANDLER_SETTER_OPPHØR_PGA_ENDRINGER_I_TPS = new RuleReasonRefImpl("1020", "Opphør av medlemskap pga endringer i tps.");

    SjekkOmSaksbehandlerHarSattOpphørPåGrunnAvEndringerITps() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(MedlemskapsvilkårGrunnlag grunnlag) {
        if (grunnlag.isMedlemskapOpphørtPågrunnAvEndredePersonopplysningerITPS()) {
            return ja(IKKE_OPPFYLT_SAKSBEHANDLER_SETTER_OPPHØR_PGA_ENDRINGER_I_TPS);
        }
        return nei();
    }
}
