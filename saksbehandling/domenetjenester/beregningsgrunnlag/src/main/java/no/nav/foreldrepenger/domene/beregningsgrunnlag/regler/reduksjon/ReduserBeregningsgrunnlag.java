package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.reduksjon;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ReduserBeregningsgrunnlag.ID)
public class ReduserBeregningsgrunnlag extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR 6.2";
    public static final String BESKRIVELSE = "Reduser beregningsgrunnlag iht dekningsgrad";

    public ReduserBeregningsgrunnlag() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal dekningsgrad = grunnlag.getDekningsgrad().getVerdi();
        Map<String, Object> resultater = new HashMap<>();
        //TODO(OMR-61): Regelsporing
        resultater.put("dekningsgrad", grunnlag.getDekningsgrad());

        grunnlag.getBeregningsgrunnlagPrStatus().forEach(bps -> {
            if (bps.erArbeidstakerEllerFrilanser()) {
                bps.getArbeidsforhold().forEach(af -> {
                    BigDecimal redusertAF = dekningsgrad.multiply(af.getAvkortetPrÅr());
                    BeregningsgrunnlagPrArbeidsforhold.builder(af)
                        .medRedusertPrÅr(dekningsgrad.multiply(af.getAvkortetPrÅr()))
                        .medRedusertRefusjonPrÅr(dekningsgrad.multiply(af.getAvkortetRefusjonPrÅr()))
                        .medRedusertBrukersAndelPrÅr(dekningsgrad.multiply(af.getAvkortetBrukersAndelPrÅr()))
                        .build();
                    resultater.put("redusertPrÅr.ATFL." + af.getArbeidsgiverId(), redusertAF);
                });
            } else if(AktivitetStatus.MS.equals(bps.getAktivitetStatus())) {
                 // hopper over MS til det støttes
            } else {
                BigDecimal redusertPS = dekningsgrad.multiply(bps.getAvkortetPrÅr());
                BeregningsgrunnlagPrStatus.builder(bps).medRedusertPrÅr(redusertPS).build();
                resultater.put("redusertPrÅr." + bps.getAktivitetStatus().name(), redusertPS);
            }
        });
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }
}
