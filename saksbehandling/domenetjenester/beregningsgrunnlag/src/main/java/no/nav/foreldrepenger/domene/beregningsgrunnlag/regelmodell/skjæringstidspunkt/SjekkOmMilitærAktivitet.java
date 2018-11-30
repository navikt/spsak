package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMilitærAktivitet.ID)
class SjekkOmMilitærAktivitet extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 21.2";
    static final String BESKRIVELSE = "Inngår aktiviteten militær eller obligatorisk sivilforsvarstjeneste som siste aktivitet?";

    SjekkOmMilitærAktivitet() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        List<AktivPeriode> sistAktive = regelmodell.finnSisteAktivePerioder();
        Optional<AktivPeriode> militær = sistAktive.stream().filter(ap -> Aktivitet.MILITÆR_ELLER_SIVILTJENESTE.equals(ap.getAktivitet())).findAny();
        return militær.isPresent() ? ja() : nei();
    }
}
