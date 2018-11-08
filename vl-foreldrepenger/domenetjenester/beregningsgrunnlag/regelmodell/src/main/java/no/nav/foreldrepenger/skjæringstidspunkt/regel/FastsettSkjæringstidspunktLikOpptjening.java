package no.nav.foreldrepenger.skjæringstidspunkt.regel;

import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringstidspunktLikOpptjening.ID)
class FastsettSkjæringstidspunktLikOpptjening extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 21.6";
    static final String BESKRIVELSE = "Skjæringstidspunkt for beregning settes lik Skjæringstidspunkt for opptjening";

    FastsettSkjæringstidspunktLikOpptjening() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        regelmodell.setSkjæringstidspunktForBeregning(regelmodell.getSkjæringstidspunktForOpptjening());
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktForBeregning", regelmodell.getSkjæringstidspunktForBeregning());
        return beregnet(resultater);
    }
}
