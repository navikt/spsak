package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkMorForSykTilÅTaVarePåBarn extends LeafSpecification<FødselsvilkårGrunnlag> {

    static final String ID = SjekkMorForSykTilÅTaVarePåBarn.class.getSimpleName();

    static final RuleReasonRefImpl MOR_IKKE_FOR_SYK_TIL_Å_TA_VARE_PÅ_BARN = new RuleReasonRefImpl("1028",
        "Mor ikke for syk til å ta vare på barn");

    SjekkMorForSykTilÅTaVarePåBarn() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FødselsvilkårGrunnlag grunnlag) {
        if (grunnlag.isErMorForSykVedFødsel()) {
            return ja();
        } else {
            return nei();
        }
    }
}
