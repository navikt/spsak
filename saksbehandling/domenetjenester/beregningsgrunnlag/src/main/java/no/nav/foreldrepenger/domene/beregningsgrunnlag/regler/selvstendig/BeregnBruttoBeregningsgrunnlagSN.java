package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.selvstendig;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnBruttoBeregningsgrunnlagSN.ID)
class BeregnBruttoBeregningsgrunnlagSN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.8";
    static final String BESKRIVELSE = "Beregn brutto beregningsgrunnlag selvstendig næringsdrivende";

    BeregnBruttoBeregningsgrunnlagSN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgAAP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        BeregningsgrunnlagPrStatus bgDP = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        if ((bgAAP != null && bgAAP.getBeregnetPrÅr() == null) || (bgDP != null && bgDP.getBeregnetPrÅr() == null)) {
            throw new IllegalStateException("Utviklerfeil: Aktivitetstatuser AAP og DP må beregnes før SN");
        }

        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BigDecimal gjennomsnittligPGI = bgps.getGjennomsnittligPGI() == null ? BigDecimal.ZERO : bgps.getGjennomsnittligPGI();

        BigDecimal bruttoAAP = bgAAP != null ? bgAAP.getBeregnetPrÅr() : BigDecimal.ZERO;
        BigDecimal bruttoDP = bgDP != null ? bgDP.getBeregnetPrÅr() : BigDecimal.ZERO;
        BigDecimal oppjustertAAP = bruttoAAP.divide(BigDecimal.valueOf(0.66), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal oppjustertDP = bruttoDP.divide(BigDecimal.valueOf(0.624), 10, BigDecimal.ROUND_HALF_UP);

        BigDecimal bruttoSN = gjennomsnittligPGI.subtract(oppjustertAAP).subtract(oppjustertDP).max(BigDecimal.ZERO);

        BeregningsgrunnlagPrStatus.builder(bgps).medBeregnetPrÅr(bruttoSN).build();

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("gjennomsnittligPGI", gjennomsnittligPGI);
        if (oppjustertAAP.compareTo(BigDecimal.ZERO) > 0) {
            resultater.put("oppjustertBruttoBeregningsgrunnlagAAP", oppjustertAAP);
        }
        if (oppjustertDP.compareTo(BigDecimal.ZERO) > 0) {
            resultater.put("oppjustertBruttoBeregningsgrunnlagDP", oppjustertDP);
        }
        resultater.put("bruttoBeregningsgrunnlagSN", bruttoSN);

        return beregnet(resultater);
    }

}
