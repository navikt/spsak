package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette;

import java.math.BigDecimal;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkSumMaxRefusjonskravStørreEnn6G.ID)
class SjekkSumMaxRefusjonskravStørreEnn6G extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR XX";
    static final String BESKRIVELSE = "Er totalt refusjonskrav større enn 6G";

    SjekkSumMaxRefusjonskravStørreEnn6G() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atfl == null) {
            return nei();
        }
        BigDecimal seksG = grunnlag.getRedusertGrunnbeløp().multiply(BigDecimal.valueOf(6));
        BigDecimal sum = atfl.getArbeidsforhold().stream()
            .filter(bpaf -> bpaf.getMaksimalRefusjonPrÅr() != null)
            .map(BeregningsgrunnlagPrArbeidsforhold::getMaksimalRefusjonPrÅr)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
        SingleEvaluation resultat = sum.compareTo(seksG) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("totaltRefusjonskravPrÅr", sum);
        resultat.setEvaluationProperty("redusertGrunnbeløp", grunnlag.getRedusertGrunnbeløp());
        resultat.setEvaluationProperty("seksG", seksG);
        return resultat;
    }
}
