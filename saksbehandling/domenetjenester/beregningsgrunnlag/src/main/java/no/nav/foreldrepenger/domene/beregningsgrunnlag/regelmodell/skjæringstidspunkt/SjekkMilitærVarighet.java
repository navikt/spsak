package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkMilitærVarighet.ID)
class SjekkMilitærVarighet extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 21.3";
    static final String BESKRIVELSE = "Har tjenesten vart mer enn 28 dager eller er tjenesten ment å vare mer enn 28 dager?";
    static final int MINIMUM_ANTALL_DAGER = 28;

    SjekkMilitærVarighet() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        boolean forKort = regelmodell.finnSisteAktivePerioder().stream()
            .filter(ap -> Aktivitet.MILITÆR_ELLER_SIVILTJENESTE.equals(ap.getAktivitet()))
            .anyMatch(ap -> ap.getPeriode().getVarighetDager() <= MINIMUM_ANTALL_DAGER);
        return forKort ? nei() : ja();
    }
}
