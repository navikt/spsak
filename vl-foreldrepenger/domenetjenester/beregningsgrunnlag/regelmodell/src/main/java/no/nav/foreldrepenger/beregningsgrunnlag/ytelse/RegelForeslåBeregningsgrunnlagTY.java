package no.nav.foreldrepenger.beregningsgrunnlag.ytelse;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelForeslåBeregningsgrunnlagTY.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=216009135")
public class RegelForeslåBeregningsgrunnlagTY extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 30";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR 30 Foreslå beregningsgrunnlag for status tilstøtende ytelse
        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlagTY = rs.beregningsRegel(ForeslåBeregningsgrunnlagTY.ID, ForeslåBeregningsgrunnlagTY.BESKRIVELSE,
            new ForeslåBeregningsgrunnlagTY(), new Beregnet());

        return foreslåBeregningsgrunnlagTY;
    }
}
