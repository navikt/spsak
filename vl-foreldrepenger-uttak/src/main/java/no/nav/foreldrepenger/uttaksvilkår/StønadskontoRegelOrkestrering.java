package no.nav.foreldrepenger.uttaksvilkår;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.BeregnKontoer;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerPropertyType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;
import no.nav.foreldrepenger.uttaksvilkår.feil.UttakRegelFeil;
import no.nav.foreldrepenger.uttaksvilkår.jackson.JacksonJsonConfig;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class StønadskontoRegelOrkestrering {

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    public StønadskontoResultat beregnKontoer(BeregnKontoerGrunnlag grunnlag) {
        return beregnKontoer(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
    }

    public StønadskontoResultat beregnKontoer(BeregnKontoerGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        String grunnlagJson = toJson(grunnlag);

        BeregnKontoer beregnKontoer = new BeregnKontoer(konfigurasjon);
        Evaluation evaluation = beregnKontoer.evaluer(grunnlag);
        String evaluationJson = EvaluationSerializer.asJson(evaluation);

        Map<Stønadskontotype, Integer> stønadskontoer = hentStønadskontoer(evaluation);
        Integer antallFlerbarnsdager = hentAntallFlerbarnsdager(evaluation);

        return new StønadskontoResultat(stønadskontoer, antallFlerbarnsdager, evaluationJson, grunnlagJson);
    }

    private Map<Stønadskontotype, Integer> hentStønadskontoer(Evaluation evaluation) {
        Regelresultat resultat = new Regelresultat(evaluation);
        if (resultat.oppfylt()) {
            Map<Stønadskontotype, Integer> kontoer = (Map<Stønadskontotype, Integer>) resultat.getProperty(BeregnKontoerPropertyType.KONTOER, Map.class);
            if (kontoer != null) {
                return kontoer;
            } else {
                throw new IllegalStateException("Noe har gått galt, har ikke fått beregnet noen stønadskontoer");
            }
        }
        return Collections.emptyMap();
    }

    private Integer hentAntallFlerbarnsdager(Evaluation evaluation) {
        Regelresultat regelresultat = new Regelresultat(evaluation);
        if (regelresultat.oppfylt()) {
            return regelresultat.getProperty(BeregnKontoerPropertyType.ANTALL_FLERBARN_DAGER, Integer.class);
        }
        return 0;
    }

    private String toJson(BeregnKontoerGrunnlag grunnlag) {
        try {
            return jacksonJsonConfig.toJson(grunnlag);
        } catch (JsonProcessingException e) {
            throw new UttakRegelFeil("Kunne ikke serialisere regelinput for beregning av stønadskontoer.", e);
        }
    }
}
