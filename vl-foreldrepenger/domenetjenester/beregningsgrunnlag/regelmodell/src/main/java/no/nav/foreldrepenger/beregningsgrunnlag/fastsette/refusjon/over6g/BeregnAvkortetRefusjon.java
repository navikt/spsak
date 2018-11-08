package no.nav.foreldrepenger.beregningsgrunnlag.fastsette.refusjon.over6g;

import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnAvkortetRefusjon.ID)
class BeregnAvkortetRefusjon extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.13.2";
    static final String BESKRIVELSE = "For hvert arbeidsforhold der refusjonskrav ikke er fordelt: Beregn avkortet refusjon";

    BeregnAvkortetRefusjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        // Finn alle arbeidsforhold som ikke er avkortet allerede
        grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream()
            .filter(af -> af.getAvkortetRefusjonPrÅr() != null)
            .filter(af -> af.getAvkortetPrÅr() == null)
            .forEach(af -> {
                BeregningsgrunnlagPrArbeidsforhold.builder(af).medAvkortetPrÅr(af.getAvkortetRefusjonPrÅr()).build();
                resultater.put("avkortetPrÅr." + af.getArbeidsgiverId(), af.getAvkortetPrÅr());
            });
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;
    }
}
