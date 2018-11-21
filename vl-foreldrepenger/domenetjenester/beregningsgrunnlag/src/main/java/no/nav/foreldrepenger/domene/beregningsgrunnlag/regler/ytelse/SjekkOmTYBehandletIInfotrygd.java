package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTYBehandletIInfotrygd.ID)
class SjekkOmTYBehandletIInfotrygd extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    static final String ID = "FP_BR 25.5";
    static final String BESKRIVELSE = "Er den tilstøtende ytelsen behandlet i Infotrygd? ";

    SjekkOmTYBehandletIInfotrygd() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        return beregningsgrunnlag.getTilstøtendeYtelse().erKildeInfotrygd() ?
            ja() : nei();
    }
}
