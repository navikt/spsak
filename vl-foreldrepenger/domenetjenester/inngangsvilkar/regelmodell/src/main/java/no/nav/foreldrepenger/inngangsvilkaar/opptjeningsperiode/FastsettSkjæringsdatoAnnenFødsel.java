package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringsdatoAnnenFødsel.ID)
public class FastsettSkjæringsdatoAnnenFødsel extends LeafSpecification<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK 21.6";
    static final String BESKRIVELSE = "Tidligste av: første uttaksdag, dagen etter mors seneste maxdato (fars første uttaksdag)";

    FastsettSkjæringsdatoAnnenFødsel() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(OpptjeningsperiodeGrunnlag regelmodell) {
        LocalDate skjæringsDatoOpptjening = regelmodell.getFørsteUttaksDato();

        if (skjæringsDatoOpptjening.isBefore(regelmodell.getHendelsesDato())) {
            skjæringsDatoOpptjening = regelmodell.getHendelsesDato();
        }

        Optional<LocalDate> morsMaksdato = regelmodell.getMorsMaksdato();
        if (morsMaksdato.isPresent()) {
            LocalDate førsteMuligeUttak = morsMaksdato.get().plusDays(1);
            if (skjæringsDatoOpptjening.isAfter(førsteMuligeUttak)) {
                skjæringsDatoOpptjening = førsteMuligeUttak;
            }
        }

        regelmodell.setSkjæringsdatoOpptjening(skjæringsDatoOpptjening);

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktOpptjening", String.valueOf(regelmodell.getSkjæringsdatoOpptjening()));
        return beregnet(resultater);
    }
}
