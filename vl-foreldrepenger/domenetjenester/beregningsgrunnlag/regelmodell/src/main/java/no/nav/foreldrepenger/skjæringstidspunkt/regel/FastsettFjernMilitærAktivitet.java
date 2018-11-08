package no.nav.foreldrepenger.skjæringstidspunkt.regel;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettFjernMilitærAktivitet.ID)
class FastsettFjernMilitærAktivitet extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 21.3X";
    static final String BESKRIVELSE = "Fjern militær eller obligatorisk sivilforsvarstjeneste fra aktivitetsliste";

    FastsettFjernMilitærAktivitet() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        List<AktivPeriode> fjernes = regelmodell.finnSisteAktivePerioder().stream()
                .filter(ap -> Aktivitet.MILITÆR_ELLER_SIVILTJENESTE.equals(ap.getAktivitet()))
                .filter(ap -> ap.getPeriode().getVarighetDager() <= SjekkMilitærVarighet.MINIMUM_ANTALL_DAGER)
                .collect(Collectors.toList());
        regelmodell.fjernAktivePerioder(fjernes);
        return ja();
    }
}
