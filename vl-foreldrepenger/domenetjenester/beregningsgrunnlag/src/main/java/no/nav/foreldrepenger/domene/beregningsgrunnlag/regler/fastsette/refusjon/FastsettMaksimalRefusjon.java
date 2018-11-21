package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette.refusjon;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettMaksimalRefusjon.ID)
public class FastsettMaksimalRefusjon extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29_3";
    public static final String BESKRIVELSE = "Fastsett maksimal refusjon for hvert arbeidsforhold";

    public FastsettMaksimalRefusjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        Map<String, Object> resultater = new HashMap<>();
        grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .flatMap(bgs -> bgs.getArbeidsforhold().stream())
            .forEach(af -> {
                BigDecimal refusjonskravPrArbeidsforholdPrÅr =  af.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO);

                if (af.getMaksimalRefusjonPrÅr() == null) {
                    BeregningsgrunnlagPrArbeidsforhold.Builder bgArbeidsforholdBuilder = BeregningsgrunnlagPrArbeidsforhold.builder(af);
                    BigDecimal maksimalRefusjon = af.getBruttoPrÅr().min(refusjonskravPrArbeidsforholdPrÅr);
                    bgArbeidsforholdBuilder.medMaksimalRefusjonPrÅr(maksimalRefusjon);
                    bgArbeidsforholdBuilder.build();
                }
                resultater.put("maksimalRefusjonPrÅr." + af.getArbeidsgiverId(), af.getMaksimalRefusjonPrÅr());
                resultater.put("refusjonskravPrÅr." + af.getArbeidsgiverId(), refusjonskravPrArbeidsforholdPrÅr);
            });

        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;
    }
}
