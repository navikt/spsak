package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjeningsperiode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringsdatoAnnenAdopsjon.ID)
public class FastsettSkjæringsdatoAnnenAdopsjon extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK 21.8";
    static final String BESKRIVELSE = "Første uttaksdag, men aldri senere enn dagen for ankomst Norge ved adopsjon fra utlandet";

    FastsettSkjæringsdatoAnnenAdopsjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        LocalDate skjæringsdatoOpptjening = regelmodell.getFørsteUttaksDato();

        LocalDate hendelsesDato = regelmodell.getHendelsesDato();
        if(skjæringsdatoOpptjening.isBefore(hendelsesDato)) {
            skjæringsdatoOpptjening = hendelsesDato;
        }

        regelmodell.setSkjæringsdatoOpptjening(skjæringsdatoOpptjening);

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktOpptjening", String.valueOf(regelmodell.getSkjæringsdatoOpptjening()));
        return beregnet(resultater);
    }
}
