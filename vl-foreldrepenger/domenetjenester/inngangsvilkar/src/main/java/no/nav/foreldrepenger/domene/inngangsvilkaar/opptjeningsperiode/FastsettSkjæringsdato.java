package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjeningsperiode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringsdato.ID)
public class FastsettSkjæringsdato extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK xx.todo";
    static final String BESKRIVELSE = "Første uttaksdag";

    FastsettSkjæringsdato() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        LocalDate skjæringsdatoOpptjening = regelmodell.getFørsteUttaksDato();
        regelmodell.setSkjæringsdatoOpptjening(skjæringsdatoOpptjening);
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktOpptjening", String.valueOf(regelmodell.getSkjæringsdatoOpptjening()));
        return beregnet(resultater);
    }
}
