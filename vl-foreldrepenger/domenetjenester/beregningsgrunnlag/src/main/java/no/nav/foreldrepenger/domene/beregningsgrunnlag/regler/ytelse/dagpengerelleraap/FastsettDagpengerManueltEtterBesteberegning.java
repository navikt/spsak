package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse.dagpengerelleraap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettDagpengerManueltEtterBesteberegning.ID)
class FastsettDagpengerManueltEtterBesteberegning extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_10.1";
    static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for Dagpenger/AAP";

    FastsettDagpengerManueltEtterBesteberegning() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus dpStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        BeregningsgrunnlagHjemmel hjemmel = BeregningsgrunnlagHjemmel.F_14_7;
        BigDecimal beregnetPrÅr = dpStatus.getBesteberegningPrÅr();
        BeregningsgrunnlagPrStatus.builder(dpStatus)
            .medBeregnetPrÅr(beregnetPrÅr)
            .medÅrsbeløpFraTilstøtendeYtelse(beregnetPrÅr)
            .build();
        grunnlag.getBeregningsgrunnlag().getAktivitetStatus(dpStatus.getAktivitetStatus()).setHjemmel(hjemmel);

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("bruttoPrÅr." + dpStatus.getAktivitetStatus().name(), beregnetPrÅr);
        resultater.put("tilstøtendeYtelserPrÅr." + dpStatus.getAktivitetStatus().name(), beregnetPrÅr);
        resultater.put("hjemmel", hjemmel);
        return beregnet(resultater);
    }
}
