package no.nav.foreldrepenger.beregningsgrunnlag.selvstendig;

import java.util.Arrays;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelBeregningsgrunnlagSN.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=174163430")
public class RegelBeregningsgrunnlagSN implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_2";

    @Override
    public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

//      FP_BR 2.7 Fastsette beregnet pr år
        Specification<BeregningsgrunnlagPeriode> fastsettBeregnetPrÅr = new FastsettBeregnetPrÅr();

//      FP_BR 2.6 Opprette regelmerknad for å fastsette brutto_pr_aar manuelt
        Specification<BeregningsgrunnlagPeriode> opprettRegelmerknad =
            rs.beregningsRegel("FP_BR 2.6", "Opprett regelmerknad", fastsettBeregnetPrÅr,
                new IkkeBeregnet(SjekkOmDifferanseStørreEnn25Prosent.VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT));

//      FP_BR 2.5 Er avvik > 25 %
        Specification<BeregningsgrunnlagPeriode> sjekkOmDifferanseStørreEnn25Prosent =
            rs.beregningHvisRegel(new SjekkOmDifferanseStørreEnn25Prosent(), opprettRegelmerknad, fastsettBeregnetPrÅr);

//      FP_BR 2.4 Fastsett sammenligningsgrunnlag og beregn avvik
        Specification<BeregningsgrunnlagPeriode> beregnAvvik =
            rs.beregningsRegel("FP_BR 2.4", "Fastsett sammenligningsgrunnlag og beregn avvik",
                new FastsettSammenligningsgrunnlagForSN(), sjekkOmDifferanseStørreEnn25Prosent);

//      FP_BR 2.3/2.3.3 Har bruker oppgitt varig endring eller nyoppstartet virksomhet?
        Specification<BeregningsgrunnlagPeriode> sjekkOmVarigEndringIVirksomhet =
            rs.beregningHvisRegel(new SjekkOmVarigEndringIVirksomhetEllerNyoppstartetNæring(), beregnAvvik, fastsettBeregnetPrÅr);

//      FP_BR 2.8 Beregn beregningsgrunnlag SN
        Specification<BeregningsgrunnlagPeriode> beregnBruttoSN =
            rs.beregningsRegel("FP_BR 2.8", "Beregn brutto beregningsgrunnlag selvstendig næringsdrivende",
                new BeregnBruttoBeregningsgrunnlagSN(), sjekkOmVarigEndringIVirksomhet);

//      FP_BR 2.18 Er bruker SN som er ny i arbeidslivet?
        Specification<BeregningsgrunnlagPeriode> sjekkOmNyIArbeidslivetSN =
            rs.beregningHvisRegel(new SjekkOmBrukerErNyIArbeidslivet(), new IkkeBeregnet(SjekkOmBrukerErNyIArbeidslivet.FASTSETT_BG_FOR_SN_NY_I_ARBEIDSLIVET),
                beregnBruttoSN);

//      FP_BR 2.19 Har saksbehandler fastsatt beregningsgrunnlaget manuelt?
        Specification<BeregningsgrunnlagPeriode> sjekkOmManueltFastsattInntekt =
            rs.beregningHvisRegel(new SjekkOmManueltFastsattBeregningsgrunnlagSN(), new Beregnet(),
                sjekkOmNyIArbeidslivetSN);

//      FP_BR 2.2 Beregn gjennomsnittlig PGI
//      FP_BR 2.9 Beregn oppjustert inntekt for årene i beregningsperioden
//      FP_BR 2.1 Fastsett beregningsperiode
        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende =
            rs.beregningsRegel("FP_BR 2", "Foreslå beregningsgrunnlag for selvstendig næringsdrivende",
                Arrays.asList(new FastsettBeregningsperiode(), new BeregnOppjustertInntekt(), new BeregnGjennomsnittligPGI()), sjekkOmManueltFastsattInntekt);

        return foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende;
    }
}
