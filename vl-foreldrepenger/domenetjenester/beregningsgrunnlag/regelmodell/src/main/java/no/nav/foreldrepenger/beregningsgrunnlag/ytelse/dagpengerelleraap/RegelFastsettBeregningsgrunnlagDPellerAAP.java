package no.nav.foreldrepenger.beregningsgrunnlag.ytelse.dagpengerelleraap;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFastsettBeregningsgrunnlagDPellerAAP.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/13t.+Beregningsgrunnlag+dagpenger+og+AAP+PK-47492")
public class RegelFastsettBeregningsgrunnlagDPellerAAP implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_10";

    @Override
    public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        //FP_BR_ 10.3 Har bruker kun status Dagpenger/AAP? -> 10.1 eller 10.2
        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag = rs.beregningHvisRegel(new SjekkOmBrukerKunHarStatusDPellerAAP(),
            new ForeslåBeregningsgrunnlagDPellerAAP(), new ForeslåBeregningsgrunnlagDPellerAAPKombinasjonMedAnnenStatus());

        //FP_BR 10.4 Er beregnngsgrunnlag for dagpenger fastsatt manuelt?
        Specification<BeregningsgrunnlagPeriode> dagpengerFastsattManuelt = rs.beregningHvisRegel(new SjekkOmBGForDagpengerFatsattManuelt(),
            new FastsettDagpengerManueltEtterBesteberegning(), foreslåBeregningsgrunnlag);

        return dagpengerFastsattManuelt;
    }
}
