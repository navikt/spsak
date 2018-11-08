package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkHarBrukerKombinasjonsstatus.ID)
class SjekkHarBrukerKombinasjonsstatus extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.6";
    static final String BESKRIVELSE = "Har bruker kombinasjonsstatus med SN?";

    SjekkHarBrukerKombinasjonsstatus() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlagPrStatus().stream()
                .anyMatch(bgps -> AktivitetStatus.SN.equals(bgps.getAktivitetStatus())) ? ja() : nei();
    }
}
