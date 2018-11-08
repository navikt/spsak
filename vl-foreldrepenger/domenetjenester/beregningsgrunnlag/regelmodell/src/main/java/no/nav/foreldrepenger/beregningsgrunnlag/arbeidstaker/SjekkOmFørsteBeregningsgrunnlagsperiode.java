package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFørsteBeregningsgrunnlagsperiode.ID)
public class SjekkOmFørsteBeregningsgrunnlagsperiode extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR XX";
    static final String BESKRIVELSE = "Første beregningsgrunnlagperiode?";

    public SjekkOmFørsteBeregningsgrunnlagsperiode() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlagPeriode().getFom().equals(grunnlag.getSkjæringstidspunkt()) ? ja() : nei();
    }
}
