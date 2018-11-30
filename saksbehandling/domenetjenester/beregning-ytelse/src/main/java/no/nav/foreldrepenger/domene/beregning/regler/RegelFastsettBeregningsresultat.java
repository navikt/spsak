package no.nav.foreldrepenger.domene.beregning.regler;

import no.nav.foreldrepenger.domene.beregning.regelmodell.Beregnet;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatFP;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatRegelmodell;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatRegelmodellMellomregning;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFastsettBeregningsresultat.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=262414229")
public class RegelFastsettBeregningsresultat implements RuleService<BeregningsresultatRegelmodell> {

    public static final String ID = "FP_BR 20.1";
    public static final String BESKRIVELSE = "RegelFastsettBeregningsresultat";

    @Override
    public Evaluation evaluer(BeregningsresultatRegelmodell input, Object outputContainer) {
        if (outputContainer instanceof BeregningsresultatFP) {
            BeregningsresultatFP beregningsresultatFP = (BeregningsresultatFP) outputContainer;
            BeregningsresultatRegelmodellMellomregning mellomregning = new BeregningsresultatRegelmodellMellomregning(input, beregningsresultatFP);
            return getSpecification().evaluate(mellomregning);
        } else {
            throw new IllegalArgumentException("Invalid output container class " + outputContainer.getClass());
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsresultatRegelmodellMellomregning> getSpecification() {
        Ruleset<BeregningsresultatRegelmodellMellomregning> rs = new Ruleset<>();
        Specification<BeregningsresultatRegelmodellMellomregning> fastsettBeregningsresultat =
                rs.beregningsRegel(FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder.ID, FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder.BESKRIVELSE,
                        new FinnOverlappendeBeregningsgrunnlagOgUttaksPerioder(),
                        new Beregnet());
        return fastsettBeregningsresultat;
    }
}
