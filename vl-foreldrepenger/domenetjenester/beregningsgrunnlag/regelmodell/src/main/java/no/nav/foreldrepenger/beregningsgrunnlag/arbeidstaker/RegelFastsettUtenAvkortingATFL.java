package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFastsettUtenAvkortingATFL.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/FP_BR+29+-+Avkorte+beregningsgrunnlag")
public class RegelFastsettUtenAvkortingATFL implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.6";
    static final String BESKRIVELSE = "Fastsett BG uten avkorting";

    @Override
    public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR_29.5.1 Fastsett Brukers Andel av Brutto beregningsgrunnlag totalt
        Specification<BeregningsgrunnlagPeriode> fastsettBrukersAndel = new FastsettBrukersAndelUtenAvkorting();

        // FP_BR_29.5.2 Fastsett Avkortet pr år pr beregningsgrunnlagsandel OG Fastsett Avkortet per år
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortet = new FastsettAvkortetLikBruttoBG();

        Specification<BeregningsgrunnlagPeriode> fastsettUtenAvkorting =
                rs.beregningsRegel(ID, BESKRIVELSE, fastsettBrukersAndel, fastsettAvkortet);

        return fastsettUtenAvkorting;
    }
}
