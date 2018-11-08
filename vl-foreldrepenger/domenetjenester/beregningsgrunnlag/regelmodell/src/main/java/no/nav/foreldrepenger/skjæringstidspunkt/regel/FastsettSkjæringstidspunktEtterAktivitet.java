package no.nav.foreldrepenger.skjæringstidspunkt.regel;

import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringstidspunktEtterAktivitet.ID)
class FastsettSkjæringstidspunktEtterAktivitet extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 21.5";
    static final String BESKRIVELSE = "Skjæringstidspunkt for beregning settes til første dag etter siste aktivitetsdag";

    FastsettSkjæringstidspunktEtterAktivitet() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        regelmodell.setSkjæringstidspunktForBeregning(regelmodell.sisteAktivitetsdato().plusDays(1));
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktForBeregning", regelmodell.getSkjæringstidspunktForBeregning());
        return beregnet(resultater);
    }
}
