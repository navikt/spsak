package no.nav.foreldrepenger.beregningsgrunnlag.foreslå;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettTilNull.ID)
class FastsettTilNull extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final BeregningsgrunnlagHjemmel HJEMMEL = BeregningsgrunnlagHjemmel.F_14_7;
    static final String ID = "FP_BR XX";
    static final String BESKRIVELSE = "Mangler beregningsregel for denne status, sett beregnet pr år til 0";
    private AktivitetStatus aktivitetStatus;

    FastsettTilNull(AktivitetStatus aktivitetStatus) {
        super(ID, BESKRIVELSE);
        this.aktivitetStatus = aktivitetStatus;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
        grunnlag.getBeregningsgrunnlag().getAktivitetStatus(aktivitetStatus).setHjemmel(HJEMMEL);
        BeregningsgrunnlagPrStatus.builder(bgps).medBeregnetPrÅr(BigDecimal.ZERO).build();
        resultater.put("aktitivetStatus", aktivitetStatus);
        resultater.put("beregnetPrÅr", bgps.getBeregnetPrÅr());
        resultater.put("hjemmel", HJEMMEL);
        return beregnet(resultater);
    }
}
