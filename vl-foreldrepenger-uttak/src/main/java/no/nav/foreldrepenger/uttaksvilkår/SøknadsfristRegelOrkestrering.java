package no.nav.foreldrepenger.uttaksvilkår;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.regler.søknadsfrist.SøknadsfristRegel;
import no.nav.foreldrepenger.regler.søknadsfrist.grunnlag.SøknadsfristGrunnlag;
import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.uttaksvilkår.feil.UttakRegelFeil;
import no.nav.foreldrepenger.uttaksvilkår.jackson.JacksonJsonConfig;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;

public class SøknadsfristRegelOrkestrering {

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    public SøknadsfristResultat vurderSøknadsfrist(SøknadsfristGrunnlag grunnlag) {
        SøknadsfristRegel søknadsfristRegel = new SøknadsfristRegel();
        Evaluation evaluation = søknadsfristRegel.evaluer(grunnlag);

        String grunnlagJson = toJson(grunnlag);
        String evaluationJson = EvaluationSerializer.asJson(evaluation);
        SøknadsfristResultat.Builder regelResultatBuilder = new SøknadsfristResultat.Builder(evaluationJson, grunnlagJson)
            .medTidligsteLovligeUttak(grunnlag.getFørsteLovligeUttaksdato());

        Regelresultat regelresultat = new Regelresultat(evaluation);
        if (!regelresultat.oppfylt()) {
            Optional<String> årsakskode = finnÅrsakskode(evaluation);
            if (årsakskode.isPresent()) {
                regelResultatBuilder.medSøknadsfristIkkeOppfylt(årsakskode.get());
            }
        } else {
            regelResultatBuilder.medSøknadsfristOppfylt();
        }
        return regelResultatBuilder.build();
    }

    private String toJson(SøknadsfristGrunnlag grunnlag) {
        try {
            return jacksonJsonConfig.toJson(grunnlag);
        } catch (JsonProcessingException e) {
            throw new UttakRegelFeil("Kunne ikke serialisere regelinput for avklaring av søknadsfrist for foreldrepenger.", e);
        }
    }

    private Optional<String> finnÅrsakskode(Evaluation evaluation) {
        EvaluationSummary summary = new EvaluationSummary(evaluation);
        for (Evaluation e : summary.leafEvaluations(Resultat.IKKE_VURDERT)) {
            if (e.getOutcome() != null) {
                return Optional.of(e.getOutcome().getReasonCode());
            }
        }
        return Optional.empty();
    }

}
