package no.nav.foreldrepenger.beregningsgrunnlag.fastsette.refusjon.over6g;

import java.util.Arrays;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelBeregnRefusjonPrArbeidsforhold.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=180066764")
public class RegelBeregnRefusjonPrArbeidsforhold implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.13.1-3";
    static final String BESKRIVELSE = "Beregn arbeidsgivers andel av det som skal refunderes";

    @Override
    public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        Specification<BeregningsgrunnlagPeriode> beregnRefusjonPrArbeidsforhold = rs.beregningsRegel(ID, BESKRIVELSE, 
                Arrays.asList(new BeregnArbeidsgiversAndeler(), new BeregnAvkortetRefusjon()),
                new VurderOmAlleFerdig());

        return beregnRefusjonPrArbeidsforhold;
    }
}
