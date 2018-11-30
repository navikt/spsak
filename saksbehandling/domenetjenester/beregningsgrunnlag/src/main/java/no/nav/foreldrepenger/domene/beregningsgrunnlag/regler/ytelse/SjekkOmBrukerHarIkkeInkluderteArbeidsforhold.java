package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBrukerHarIkkeInkluderteArbeidsforhold.ID)
class SjekkOmBrukerHarIkkeInkluderteArbeidsforhold extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    static final String ID = "FP_BR 25.3";
    static final String BESKRIVELSE = "Har bruker løpende arbeidsforhold som ikke var inkludert i den tilstøtende ytelsen?";

    SjekkOmBrukerHarIkkeInkluderteArbeidsforhold() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        boolean fantAndelIkkeFraTilstøtendeYtelse = beregningsgrunnlag.getBeregningsgrunnlagAndeler().stream()
            .anyMatch(andel -> !andel.erFraTilstøtendeYtelse());
        return fantAndelIkkeFraTilstøtendeYtelse ? ja() : nei();
    }
}
