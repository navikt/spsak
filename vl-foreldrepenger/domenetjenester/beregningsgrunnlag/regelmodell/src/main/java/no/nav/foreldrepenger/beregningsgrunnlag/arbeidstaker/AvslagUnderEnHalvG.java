package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(AvslagUnderEnHalvG.ID)
public class AvslagUnderEnHalvG extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_VK_32.2";
    static final String BESKRIVELSE = "Opprett regelmerknad om avslag under 0.5G";
    private static final String AVSLAGSÅRSAK = "1041";

    public AvslagUnderEnHalvG() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return nei(new RuleReasonRefImpl(AVSLAGSÅRSAK, BESKRIVELSE));
    }
}
