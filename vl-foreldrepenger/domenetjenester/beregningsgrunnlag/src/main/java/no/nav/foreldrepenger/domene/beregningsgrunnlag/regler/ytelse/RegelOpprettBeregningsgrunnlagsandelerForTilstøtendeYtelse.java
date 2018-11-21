package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregnetTY;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=267637219")
public class RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse implements RuleService<BeregningsgrunnlagFraTilstøtendeYtelse> {

    public static final String ID = "Fastsett andeler for aktivitetstatus tilstøtende ytelse";

    @Override
    public Evaluation evaluer(BeregningsgrunnlagFraTilstøtendeYtelse regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagFraTilstøtendeYtelse> getSpecification() {
        Ruleset<BeregningsgrunnlagFraTilstøtendeYtelse> rs = new Ruleset<>();

        //FP_BR 25.10-11 Redusere grunnbeløp iht dekningsgrad
        Specification<BeregningsgrunnlagFraTilstøtendeYtelse> reduserGrunnbeløp = new ReduserGrunnbeløpMedDekningsgrad();

        //FP_BR 25.9 Er den tilstøtende ytelsen sykepenger med reduksjon 65% og bruker har Arbeidskategori 07 Inaktiv?
        Specification<BeregningsgrunnlagFraTilstøtendeYtelse> sjekkOmTYerSPmedKriterier =
            rs.beregningHvisRegel(new SjekkOmTYerSykepengerMedKriterier(), reduserGrunnbeløp, new BeregnetTY());

        //FP_BR 25.8 Er den tilstøtende ytelsen foreldrepenger
        Specification<BeregningsgrunnlagFraTilstøtendeYtelse> sjekkOmTYerFP =
            rs.beregningHvisRegel(new SjekkOmTYerForeldrepenger(), reduserGrunnbeløp, sjekkOmTYerSPmedKriterier);

        //FP_BR 25.7 Fastsett grunnbeløp ved opprinnelig skjæringstidspunkt
        Specification<BeregningsgrunnlagFraTilstøtendeYtelse> fastsettGrunnbeløp =
            rs.beregningsRegel("FP_BR 25.7", "Fastsett grunnbeløp ved opprinnelig skjæringstidspunkt", new FastsettGrunnbeløp(),
                sjekkOmTYerFP);

        //FP_BR 25.6 Oppjuster inntekt til årsbeløp
        Specification<BeregningsgrunnlagFraTilstøtendeYtelse> oppjusterInntektTilÅrsbeløp =
            rs.beregningsRegel("FP_BR 25.6", " Oppjuster inntekt til årsbeløp", new OppjusterInntektTilÅrbeløp(),
                fastsettGrunnbeløp);

        //FP_BR 25.5 Er den tilstøtende ytelsen behandlet i Infotrygd?
        Specification<BeregningsgrunnlagFraTilstøtendeYtelse> sjekkOmBehandletIInfortrygd =
            rs.beregningHvisRegel(new SjekkOmTYBehandletIInfotrygd(), oppjusterInntektTilÅrsbeløp, fastsettGrunnbeløp);

        //FP_BR 25.4 Opprette eventuelle andeler for nye arbeidsforhold,
        Specification<BeregningsgrunnlagFraTilstøtendeYtelse> opprettEkstraAndelerTY =
            rs.beregningsRegel("FP_BR 25.4", "Opprette eventuelle andeler for nye arbeidsforhold", new OpprettAndelerForNyeArbeidsforhold(),
                sjekkOmBehandletIInfortrygd);

        //FP_BR 25.3 Opprette eventuelle andeler for nye arbeidsforhold,
        Specification<BeregningsgrunnlagFraTilstøtendeYtelse> sjekkOmBrukerHarIkkeInkluderteArbeidsforhold =
            rs.beregningHvisRegel(new SjekkOmBrukerHarIkkeInkluderteArbeidsforhold(), opprettEkstraAndelerTY, sjekkOmBehandletIInfortrygd);

        //FP_BR 25.2 Opprette andeler basert på tilstøtende ytelse
        Specification<BeregningsgrunnlagFraTilstøtendeYtelse> opprettAndelerTY =
            rs.beregningsRegel("FP_BR 25.2", "Opprette andeler basert på tilstøtende ytelse", new OpprettAndelerBasertPåTilstøtendeYtelse(),
                sjekkOmBrukerHarIkkeInkluderteArbeidsforhold);

        return opprettAndelerTY;
    }
}
