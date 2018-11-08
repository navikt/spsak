package no.nav.foreldrepenger.beregningsgrunnlag.foreslå;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;


public class RegelForeslåBeregningsgrunnlag extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "BG-FORESLÅ";

    public RegelForeslåBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // Fastsett alle BG per status

        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag =
                rs.beregningsRegel("FP_BR pr status", "Fastsett beregningsgrunnlag pr status", RegelForeslåBeregningsgrunnlagPrStatus.class, regelmodell, "aktivitetStatus", regelmodell.getAktivitetStatuser(), new Beregnet());

        return foreslåBeregningsgrunnlag;
    }
}
