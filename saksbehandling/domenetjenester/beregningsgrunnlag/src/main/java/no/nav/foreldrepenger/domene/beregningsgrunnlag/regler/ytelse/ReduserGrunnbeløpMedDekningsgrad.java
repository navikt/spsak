package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ReduserGrunnbeløpMedDekningsgrad.ID)
class ReduserGrunnbeløpMedDekningsgrad extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    static final String ID = "FP_BR 25.10";
    static final String BESKRIVELSE = "Redusere grunnbeløp iht dekningsgrad forrige ytelse";

    ReduserGrunnbeløpMedDekningsgrad() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        BigDecimal redusertGrunnbeløp = beregningsgrunnlag.getGrunnbeløp().multiply(beregningsgrunnlag.getTilstøtendeYtelse().getDekningsgrad().getVerdi());
        BeregningsgrunnlagFraTilstøtendeYtelse.builder(beregningsgrunnlag).medRedusertGrunnbeløp(redusertGrunnbeløp);
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("RedusertGrunnbeløp", redusertGrunnbeløp);
        return beregnet(resultater);
    }
}
