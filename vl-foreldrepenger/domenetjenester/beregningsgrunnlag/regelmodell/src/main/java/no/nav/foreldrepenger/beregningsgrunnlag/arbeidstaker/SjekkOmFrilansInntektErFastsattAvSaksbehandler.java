package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFrilansInntektErFastsattAvSaksbehandler.ID)
class SjekkOmFrilansInntektErFastsattAvSaksbehandler extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.9";
    static final String BESKRIVELSE = "Er frilans inntekt fastsatt av saksbehandler?";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    SjekkOmFrilansInntektErFastsattAvSaksbehandler(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return Boolean.TRUE.equals(arbeidsforhold.getFastsattAvSaksbehandler()) ? ja() : nei();
    }
}
