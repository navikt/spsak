package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettOpptjeningsperiode.ID)
public class FastsettOpptjeningsperiode extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK 21.9";
    static final String BESKRIVELSE = "Regnes relativt til Skjæringstidspunkt fra konfig(opptjeningsperiode.lengde) til dagen før (siste dag i opptjeningsperioden)";

    FastsettOpptjeningsperiode() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        regelmodell.setOpptjeningsperiodeTom(regelmodell.getSkjæringsdatoOpptjening().minusDays(1));
        regelmodell.setOpptjeningsperiodeFom(regelmodell.getSkjæringsdatoOpptjening().minus(regelmodell.getPeriodeLengde()));

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("OpptjeningsperiodeFOM", String.valueOf(regelmodell.getOpptjeningsperiodeFom()));
        resultater.put("OpptjeningsperiodeTOM", String.valueOf(regelmodell.getOpptjeningsperiodeTom()));
        return beregnet(resultater);
    }
}
