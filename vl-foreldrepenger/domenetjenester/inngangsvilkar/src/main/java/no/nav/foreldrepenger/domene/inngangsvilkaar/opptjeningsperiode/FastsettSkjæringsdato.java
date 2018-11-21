package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjeningsperiode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringsdato.ID)
public class FastsettSkjæringsdato extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK xx.todo";
    static final String BESKRIVELSE = "Første dag i sykdomsperioden";

    FastsettSkjæringsdato() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        LocalDate skjæringsdatoOpptjening = finnFørsteDag(regelmodell);
        regelmodell.setSkjæringsdatoOpptjening(skjæringsdatoOpptjening);
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktOpptjening", String.valueOf(regelmodell.getSkjæringsdatoOpptjening()));
        return beregnet(resultater);
    }

    private LocalDate finnFørsteDag(OpptjeningsperiodeGrunnlag regelmodell) {
        Set<LocalDate> datoer = new HashSet<>();

        datoer.add(regelmodell.getFørsteDagIArbeidsgiverPerioden());
        datoer.add(regelmodell.getFørsteDagISykemelding());
        datoer.add(regelmodell.getFørsteDagISøknad());
        datoer.add(regelmodell.getFørsteEgenmeldingsDag());

        return datoer.stream().filter(Objects::nonNull).min(LocalDate::compareTo).orElseThrow(IllegalStateException::new);
    }
}
