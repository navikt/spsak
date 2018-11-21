package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettGrunnbeløp.ID)
class FastsettGrunnbeløp extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    static final String ID = "FP_BR 25.7";
    static final String BESKRIVELSE = "Fastsett Grunnbeløp ved opprinnelig skjæringstidspunkt";

    FastsettGrunnbeløp() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        LocalDate opprinneligSkjæringstidspunkt = beregningsgrunnlag.getTilstøtendeYtelse().getOpprinneligSkjæringstidspunkt();
        BigDecimal fastsattGrunnbeløp = BigDecimal.valueOf(beregningsgrunnlag.verdiAvG(opprinneligSkjæringstidspunkt));
        BeregningsgrunnlagFraTilstøtendeYtelse.builder(beregningsgrunnlag)
            .medGrunnbeløp(fastsattGrunnbeløp)
            .medRedusertGrunnbeløp(fastsattGrunnbeløp)
            .build();

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("Opprinnelig skjæringstidspunkt", opprinneligSkjæringstidspunkt);
        resultater.put("Fastsatt grunnbeløp", fastsattGrunnbeløp);

        return beregnet(resultater);
    }
}
