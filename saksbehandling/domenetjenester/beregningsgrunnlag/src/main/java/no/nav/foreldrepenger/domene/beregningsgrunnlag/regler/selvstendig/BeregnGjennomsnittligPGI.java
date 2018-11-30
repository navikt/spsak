package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.selvstendig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnGjennomsnittligPGI.ID)
public class BeregnGjennomsnittligPGI extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.2";
    static final String BESKRIVELSE = "Beregn gjennomsnittlig PGI oppjustert til G";

    private static final String BIDRAG_TIL_BG = "bidragTilBG";

    public BeregnGjennomsnittligPGI() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BigDecimal bidragTilBGSum = BigDecimal.ZERO;
        Map<String, Object> resultater = new HashMap<>();
        for (int årSiden = 0; årSiden <= 2; årSiden++) {
            int årstall = bgps.getBeregningsperiode().getTom().getYear() - årSiden;
            BigDecimal gSnitt = BigDecimal.valueOf(grunnlag.getBeregningsgrunnlag().snittverdiAvG(årstall));
            BigDecimal treGsnitt = gSnitt.multiply(BigDecimal.valueOf(3));
            BigDecimal seksGsnitt = gSnitt.multiply(BigDecimal.valueOf(6));
            BigDecimal tolvGsnitt = gSnitt.multiply(BigDecimal.valueOf(12));
            BigDecimal pgiÅr = grunnlag.getInntektsgrunnlag().getÅrsinntekt(årstall, Inntektskilde.SIGRUN).orElse(BigDecimal.ZERO);
            if (pgiÅr.compareTo(seksGsnitt) < 1) {
                BigDecimal bidragTilBG = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? pgiÅr.divide(gSnitt, 10, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
                resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
                bidragTilBGSum = bidragTilBGSum.add(bidragTilBG);
            } else if (pgiÅr.compareTo(seksGsnitt) > 0 && pgiÅr.compareTo(tolvGsnitt) < 0) {
                BigDecimal bidragTilBG = pgiÅr.subtract(seksGsnitt).abs().divide(treGsnitt, 10, RoundingMode.HALF_EVEN).add(BigDecimal.valueOf(6));
                resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
                bidragTilBGSum = bidragTilBGSum.add(bidragTilBG);
            } else {
                BigDecimal bidragTilBG = BigDecimal.valueOf(8);
                resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
                bidragTilBGSum = bidragTilBGSum.add(bidragTilBG);
            }
        }
        BigDecimal gjeldendeG = grunnlag.getGrunnbeløp();
        BigDecimal gjennomsnittligPGI = bidragTilBGSum.compareTo(BigDecimal.ZERO) != 0 ? bidragTilBGSum.divide(BigDecimal.valueOf(3), 10, RoundingMode.HALF_EVEN).multiply(gjeldendeG)
            : BigDecimal.ZERO;
        resultater.put("GjennomsnittligPGI", gjennomsnittligPGI);
        BeregningsgrunnlagPrStatus.builder(bgps)
            .medGjennomsnittligPGI(gjennomsnittligPGI)
            .build();

        return beregnet(resultater);
    }

}
