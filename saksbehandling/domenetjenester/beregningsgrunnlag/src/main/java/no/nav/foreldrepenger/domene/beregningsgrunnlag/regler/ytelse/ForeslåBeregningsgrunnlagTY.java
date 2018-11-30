package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ForeslåBeregningsgrunnlagTY.ID)
class ForeslåBeregningsgrunnlagTY extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 30";
    static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for status tilstøtende ytelse";
    private static final BeregningsgrunnlagHjemmel HJEMMEL = BeregningsgrunnlagHjemmel.F_14_7;
    private static final BeregningsgrunnlagHjemmel HJEMMEL_INAKTIV = BeregningsgrunnlagHjemmel.F_14_7_8_47;

    ForeslåBeregningsgrunnlagTY() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        boolean erInaktiv = grunnlag.getBeregningsgrunnlag().erArbeidskategoriInaktiv();
        boolean harSykepengerPåStp = grunnlag.getBeregningsgrunnlag().harSykepengerPåSkjæringstidpunkt();
        BeregningsgrunnlagHjemmel hjemmel = erInaktiv && harSykepengerPåStp ? HJEMMEL_INAKTIV : HJEMMEL;
        grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.TY).setHjemmel(hjemmel);
        BigDecimal brutto = grunnlag.getBruttoPrÅr();
        resultater.put("beregnetPrÅr", brutto);
        resultater.put("hjemmel", hjemmel);
        return beregnet(resultater);
    }
}
