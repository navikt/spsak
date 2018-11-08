package no.nav.foreldrepenger.beregningsgrunnlag.avkorting;

import java.math.BigDecimal;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTotaltBGForArbeidsforholdStørreEnnAntallG.ID)
class SjekkOmTotaltBGForArbeidsforholdStørreEnnAntallG extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.8.2";
    static final String BESKRIVELSE = "Er totalt beregningsgrunnlag for beregningsgrunnlagsandeler fra arbeidsforhold større enn 6G";
    private BigDecimal antallGrunnbeløp;

    SjekkOmTotaltBGForArbeidsforholdStørreEnnAntallG(BigDecimal antallGrunnbeløp) {
        super(ID, BESKRIVELSE);
        this.antallGrunnbeløp = antallGrunnbeløp;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal beløp = grunnlag.getRedusertGrunnbeløp().multiply(antallGrunnbeløp);

        BeregningsgrunnlagPrStatus atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BigDecimal totaltBG = atfl == null ? BigDecimal.ZERO : atfl.getArbeidsforholdIkkeFrilans().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        SingleEvaluation resultat = totaltBG.compareTo(beløp) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("totaltBeregningsgrunnlagFraArbeidsforhold", totaltBG);
        resultat.setEvaluationProperty("redusertGrunnbeløp", grunnlag.getRedusertGrunnbeløp());
        resultat.setEvaluationProperty("gittBeløp", beløp);
        return resultat;
    }
}
