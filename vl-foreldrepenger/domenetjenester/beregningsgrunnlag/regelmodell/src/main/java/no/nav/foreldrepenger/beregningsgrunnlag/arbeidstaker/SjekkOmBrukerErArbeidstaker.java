package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBrukerErArbeidstaker.ID)
class SjekkOmBrukerErArbeidstaker extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.1";
    static final String BESKRIVELSE = "Er bruker arbeidstaker?";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    SjekkOmBrukerErArbeidstaker(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return arbeidsforhold.erFrilanser() ? nei() : ja();
    }   
}
