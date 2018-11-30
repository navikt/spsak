package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.avkorting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBrukersAndelFraArbeidsforholdSomIkkeErFordelt.ID)
class FastsettBrukersAndelFraArbeidsforholdSomIkkeErFordelt extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.8";
    static final String BESKRIVELSE = "For hver beregningsgrunnlagsandel som kommer fra arbeidsforhold, som enda ikke er fordelt," +
        " må andelen som totalt skal refunderes og utbetales til bruker beregnes";

    FastsettBrukersAndelFraArbeidsforholdSomIkkeErFordelt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);

        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdene = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        arbeidsforholdene.stream()
                .filter(af -> af.getMaksimalRefusjonPrÅr() != null)
                .filter(af -> af.getAvkortetRefusjonPrÅr() != null)
                .forEach(af -> {
                    resultater.put("avkortetRefusjon." + af.getArbeidsgiverId(), af.getAvkortetRefusjonPrÅr());
                    resultater.put("brukersAndel." + af.getArbeidsgiverId(), af.getAvkortetBrukersAndelPrÅr());
                });
        return resultat;

    }



}
