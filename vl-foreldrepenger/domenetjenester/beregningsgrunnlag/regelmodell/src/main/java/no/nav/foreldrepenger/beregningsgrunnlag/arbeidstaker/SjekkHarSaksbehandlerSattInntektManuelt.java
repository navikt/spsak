package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkHarSaksbehandlerSattInntektManuelt.ID)
class SjekkHarSaksbehandlerSattInntektManuelt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.5";
    static final String BESKRIVELSE = "Har saksbehandler fastsatt månedsinntekt manuelt?";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    SjekkHarSaksbehandlerSattInntektManuelt(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return Boolean.TRUE.equals(arbeidsforhold.getFastsattAvSaksbehandler()) ? ja() : nei();
    }
}
