package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.selvstendig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnOppjustertInntekt.ID)
public class BeregnOppjustertInntekt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.9 BP";
    private static final String BESKRIVELSE = "Beregn oppjustert inntekt for årene i beregningsperioden";

    public BeregnOppjustertInntekt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        LocalDate beregningsperiodeTom = bgps.getBeregningsperiode().getTom();
        BigDecimal gjeldendeG = grunnlag.getGrunnbeløp();
        Map<String, Object> resultater = new HashMap<>();
        List<BigDecimal> pgiListe = new ArrayList<>();

        for (int årSiden = 0; årSiden <= 2; årSiden++) {
            int årstall = beregningsperiodeTom.getYear() - årSiden;
            BigDecimal gSnitt = BigDecimal.valueOf(grunnlag.getBeregningsgrunnlag().snittverdiAvG(årstall));
            BigDecimal pgiÅr = grunnlag.getInntektsgrunnlag().getÅrsinntekt(årstall, Inntektskilde.SIGRUN).orElse(BigDecimal.ZERO);
            BigDecimal pgiPrG = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? pgiÅr.divide(gSnitt, 10, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
            BigDecimal pgiJustert = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? gjeldendeG.multiply(pgiPrG) : BigDecimal.ZERO;
            resultater.put("PGI/G." + årstall, pgiPrG);
            resultater.put("PGIjustert." + årstall, pgiJustert);
            pgiListe.add(pgiJustert);
        }
        BeregningsgrunnlagPrStatus.builder(bgps)
            .medPGI(pgiListe)
            .build();
        return beregnet(resultater);
    }
}
