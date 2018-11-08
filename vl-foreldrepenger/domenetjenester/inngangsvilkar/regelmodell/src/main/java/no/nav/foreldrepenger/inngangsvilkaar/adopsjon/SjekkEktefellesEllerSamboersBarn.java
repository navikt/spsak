package no.nav.foreldrepenger.inngangsvilkaar.adopsjon;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.AdopsjonsvilkårGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkEktefellesEllerSamboersBarn extends LeafSpecification<AdopsjonsvilkårGrunnlag> {

    static final String ID_ES = "FP_VK_4.3";
    static final String ID_FP = "FP_VK_16.2";

    static final RuleReasonRef IKKE_OPPFYLT_ADOPSJON_AV_EKTEFELLE_ELLER_SAMBOERS_BARN = new RuleReasonRefImpl("1005", "Adopsjon av ektefelles eller samboers barn.");

    SjekkEktefellesEllerSamboersBarn(String id) {
        super(id);
    }

    @Override
    public Evaluation evaluate(AdopsjonsvilkårGrunnlag grunnlag) {
        if (grunnlag.isEktefellesBarn()) {
            return ja();
        }
        return nei();
    }
}
