package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringsdatoMorFødsel.ID)
public class FastsettSkjæringsdatoMorFødsel extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK 21.5";
    static final String BESKRIVELSE = "opptjeningsvilkar for beregning settes til første dag etter siste aktivitetsdag";

    FastsettSkjæringsdatoMorFødsel() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        LocalDate skjæringsDatoOpptjening = regelmodell.getFørsteUttaksDato();

        LocalDate terminDato = regelmodell.getTerminDato();
        LocalDate hendelsesDato = regelmodell.getHendelsesDato();

        LocalDate tidligsteUttakDato = hendelsesDato.minus(regelmodell.getTidligsteUttakFørFødselPeriode());
        if (terminDato != null && terminDato.isBefore(hendelsesDato)) {
            tidligsteUttakDato = terminDato.minus(regelmodell.getTidligsteUttakFørFødselPeriode());
        }

        if (skjæringsDatoOpptjening.isBefore(tidligsteUttakDato)) {
            skjæringsDatoOpptjening = tidligsteUttakDato;
        }

        if (terminDato != null && skjæringsDatoOpptjening.isAfter(terminDato.minusWeeks(3))) {
            skjæringsDatoOpptjening = terminDato.minusWeeks(3);
        }
        // Tilfelle fødsel mer enn tre uker før termindato
        if (skjæringsDatoOpptjening.isAfter(hendelsesDato)) {
            skjæringsDatoOpptjening = hendelsesDato;
        }
        regelmodell.setSkjæringsdatoOpptjening(skjæringsDatoOpptjening);

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktOpptjening", String.valueOf(regelmodell.getSkjæringsdatoOpptjening()));
        return beregnet(resultater);
    }
}
