package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.avkorting;

import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettAvkortetRefusjonPrAndel.ID)
class FastsettAvkortetRefusjonPrAndel extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.1";
    static final String BESKRIVELSE = "Fastsett avkortet refusjon pr andel";

    FastsettAvkortetRefusjonPrAndel() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        SingleEvaluation resultat = ja();
        BeregningsgrunnlagPrStatus atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atfl != null) {
            Map<String, Object> resultater = new HashMap<>();
            resultat.setEvaluationProperties(resultater);
            atfl.getArbeidsforhold().forEach(af -> {
                resultater.put("avkortetRefusjonPrÅr" + "." + af.getArbeidsgiverId(), af.getMaksimalRefusjonPrÅr());
            });
        }
        return resultat;
    }
}
