package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse.dagpengerelleraap;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBGForDagpengerFatsattManuelt.ID)
class SjekkOmBGForDagpengerFatsattManuelt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_10.4";
    static final String BESKRIVELSE = "Er beregnngsgrunnlag for dagpenger fastsatt manuelt? ";

    SjekkOmBGForDagpengerFatsattManuelt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus dagpengerStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        boolean manueltFastsattDagpenger = dagpengerStatus != null && dagpengerStatus.erFastsattAvSaksbehandler();
        return manueltFastsattDagpenger ? ja() : nei();
    }
}
