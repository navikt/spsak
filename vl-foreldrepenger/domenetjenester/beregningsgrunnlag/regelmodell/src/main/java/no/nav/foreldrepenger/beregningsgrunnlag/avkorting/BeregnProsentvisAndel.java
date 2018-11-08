package no.nav.foreldrepenger.beregningsgrunnlag.avkorting;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnProsentvisAndel.ID)
class BeregnProsentvisAndel extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.7";
    static final String BESKRIVELSE = "For hver beregningsgrunnlagsandel som kommer fra arbeidsforhold, som enda ikke er fordelt, må den prosentvise andelen beregnes";

    BeregnProsentvisAndel() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return ja();
    }



}
