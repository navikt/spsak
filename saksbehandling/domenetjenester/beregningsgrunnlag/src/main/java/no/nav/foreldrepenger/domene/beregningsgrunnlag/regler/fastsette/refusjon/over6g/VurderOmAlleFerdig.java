package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette.refusjon.over6g;

import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(VurderOmAlleFerdig.ID)
class VurderOmAlleFerdig extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.13.3";
    static final String BESKRIVELSE = "Vurder om refusjonskrav for alle beregningsgrunnlagsandeler kan settes til ferdig avkortet";

    VurderOmAlleFerdig() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Optional<BeregningsgrunnlagPrArbeidsforhold> ikkeFerdig = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream()
            .filter(af -> af.getMaksimalRefusjonPrÅr() != null)
            .filter(af -> af.getAvkortetRefusjonPrÅr() == null)
            .findAny();
        return ikkeFerdig.isPresent() ? nei() : ja();
    }
}
