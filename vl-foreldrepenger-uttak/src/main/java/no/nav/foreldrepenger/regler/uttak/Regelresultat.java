package no.nav.foreldrepenger.regler.uttak;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodePropertyType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Årsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;

public class Regelresultat {

    private EvaluationSummary evaluationSummary;

    public Regelresultat(Evaluation evaluation) {
        this.evaluationSummary = new EvaluationSummary(evaluation);
    }

    public <T> T getProperty(String tag, Class<T> clazz) {
        Object obj = getProperty(tag);
        if (obj != null && !clazz.isAssignableFrom(obj.getClass())) {
            throw new IllegalArgumentException("Kan ikke hente property " + tag + ". Forventet " + clazz.getSimpleName() + " men fant " + obj.getClass());
        }
        return (T) obj;
    }

    public LocalDate getKnekkpunkt() {
        return getProperty(FastsettePeriodePropertyType.KNEKKPUNKT, LocalDate.class);
    }

    public UtfallType getGradering() {
        return getProperty(FastsettePeriodePropertyType.GRADERING, UtfallType.class);
    }

    public UtfallType getUtfallType() {
        return getProperty(FastsettePeriodePropertyType.UTFALL, UtfallType.class);
    }

    public Manuellbehandlingårsak getManuellbehandlingårsak() {
        return getProperty(FastsettePeriodePropertyType.MANUELL_BEHANDLING_ÅRSAK, Manuellbehandlingårsak.class);
    }

    public Årsak getAvklaringÅrsak() {
        return getProperty(FastsettePeriodePropertyType.AVKLARING_ÅRSAK, Årsak.class);
    }

    public Årsak getInnvilgetÅrsak() {
        return getProperty(FastsettePeriodePropertyType.INNVILGET_ÅRSAK, Årsak.class);
    }

    public GraderingIkkeInnvilgetÅrsak getGraderingIkkeInnvilgetÅrsak() {
        return getProperty(FastsettePeriodePropertyType.GRADERING_IKKE_OPPFYLT_ÅRSAK, GraderingIkkeInnvilgetÅrsak.class);
    }

    public boolean isTrekkDagerFraSaldo() {
        Boolean trekkDagerFraSaldo = getProperty(FastsettePeriodePropertyType.TREKK_DAGER_FRA_SALDO, Boolean.class);
        return trekkDagerFraSaldo != null && trekkDagerFraSaldo;
    }

    public boolean isUtbetal() {
        Boolean  trekkDagerFraSaldo = getProperty(FastsettePeriodePropertyType.UTBETAL, Boolean.class);
        return trekkDagerFraSaldo != null && trekkDagerFraSaldo;
    }

    public boolean oppfylt() {
        return !evaluationSummary.leafEvaluations(Resultat.JA).isEmpty();
    }

    private Object getProperty(String tag) {
        Optional<Evaluation> first = evaluationSummary.leafEvaluations().stream()
            .filter(e -> e.getEvaluationProperties() != null)
            .findFirst();

        if (!first.isPresent()) {
            return null;
        }
        return first.get().getEvaluationProperties().get(tag);
    }

}
