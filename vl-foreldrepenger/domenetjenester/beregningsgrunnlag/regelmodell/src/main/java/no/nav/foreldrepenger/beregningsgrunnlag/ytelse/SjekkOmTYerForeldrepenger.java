package no.nav.foreldrepenger.beregningsgrunnlag.ytelse;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTYerForeldrepenger.ID)
class SjekkOmTYerForeldrepenger extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    static final String ID = "FP_BR 25.8";
    static final String BESKRIVELSE = "Er den tilstøtende ytelsen foreldrepenger";

    SjekkOmTYerForeldrepenger() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        return RelatertYtelseType.FORELDREPENGER.equals(beregningsgrunnlag.getTilstøtendeYtelse().getRelatertYtelseType())
            ? ja() : nei();
    }
}
