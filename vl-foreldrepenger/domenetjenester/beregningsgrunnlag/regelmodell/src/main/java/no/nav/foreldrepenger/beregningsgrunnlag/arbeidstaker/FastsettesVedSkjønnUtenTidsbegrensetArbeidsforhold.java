package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;

@RuleDocumentation(FastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold.ID)
class FastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold extends IkkeBeregnet {

    static final String ID = "5038";
    static final String BESKRIVELSE = "Avvik er > 25%, beregningsgrunnlag fastsettes ved skjønn";
    private static final RuleReasonRef AVVIK_MER_ENN_25_PROSENT = new RuleReasonRefImpl(ID, BESKRIVELSE);

    FastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold() {
        super(AVVIK_MER_ENN_25_PROSENT);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        BigDecimal avvikProsent = sg.getAvvikProsent();
        return nei(new RuleReasonRefImpl(ID, String.valueOf(avvikProsent)));
    }
}
