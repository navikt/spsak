package no.nav.foreldrepenger.beregningsgrunnlag.kombinasjon;

import java.util.Arrays;

import no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker.SjekkOmFørsteBeregningsgrunnlagsperiode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.selvstendig.BeregnGjennomsnittligPGI;
import no.nav.foreldrepenger.beregningsgrunnlag.selvstendig.BeregnOppjustertInntekt;
import no.nav.foreldrepenger.beregningsgrunnlag.selvstendig.FastsettBeregningsperiode;
import no.nav.foreldrepenger.beregningsgrunnlag.selvstendig.FastsettSammenligningsgrunnlagForSN;
import no.nav.foreldrepenger.beregningsgrunnlag.selvstendig.SjekkOmBrukerErNyIArbeidslivet;
import no.nav.foreldrepenger.beregningsgrunnlag.selvstendig.SjekkOmDifferanseStørreEnn25Prosent;
import no.nav.foreldrepenger.beregningsgrunnlag.selvstendig.SjekkOmManueltFastsattBeregningsgrunnlagSN;
import no.nav.foreldrepenger.beregningsgrunnlag.selvstendig.SjekkOmVarigEndringIVirksomhetEllerNyoppstartetNæring;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_2";

    public RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR 2.15 Opprett regelmerknad dersom avvik > 25 %
        Specification<BeregningsgrunnlagPeriode> opprettRegelmerknad = new IkkeBeregnet(SjekkOmDifferanseStørreEnn25Prosent.VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT);

        // FP_BR 2.14 Avvik > 25 %?
        Specification<BeregningsgrunnlagPeriode> sjekkOmAvvikStørrenEnn25Prosent = rs.beregningHvisRegel(new SjekkOmDifferanseStørreEnn25Prosent(),
            opprettRegelmerknad, new Beregnet());

        // FP_BR 2.4 Fastsett sammenligninsggrunnlag og beregn avvik
        Specification<BeregningsgrunnlagPeriode> fastsettSammenligningsgrunnlag = rs.beregningsRegel("FP_BR 2.4",
            "Fastsett sammenlignignsgrunnlag, beregn avvik og sjekk om avvik > 25%",
           new FastsettSammenligningsgrunnlagForSN(), sjekkOmAvvikStørrenEnn25Prosent);

        // Første beregningsgrunnlagsperiode? Sammenligninggrunnlag skal fastsettes og sjekkes mot bare om det er første periode
        Specification<BeregningsgrunnlagPeriode> sjekkOmFørstePeriode =
            rs.beregningHvisRegel(new SjekkOmFørsteBeregningsgrunnlagsperiode(), fastsettSammenligningsgrunnlag, new Beregnet());

        // FP_BR 2.3 Har bruker oppgitt varig ending eller nyoppstartet virksomhet?
        Specification<BeregningsgrunnlagPeriode> sjekkOmVarigEndringIVirksomhet =
            rs.beregningHvisRegel(new SjekkOmVarigEndringIVirksomhetEllerNyoppstartetNæring(), sjekkOmFørstePeriode, new Beregnet());

        // FP_BR 2.10 Beregn SN-andel for kombinasjon ATFL+SN
        // FP_BR 2.2 Beregn gjennomsnittlig PGI
        Specification<BeregningsgrunnlagPeriode> beregnBruttoSN =
            rs.beregningsRegel("FP_BR 2.2 - 2.10", "Beregn gjennomsnittlig PGI og beregn SN-andel", Arrays.asList(new BeregnGjennomsnittligPGI(), new BeregnSelvstendigAndelForKombinasjon()), sjekkOmVarigEndringIVirksomhet);

        // FP_BR 2.18 Er bruker SN som er ny i arbeidslivet?
        Specification<BeregningsgrunnlagPeriode> sjekkOmNyIArbeidslivetSN =
            rs.beregningHvisRegel(new SjekkOmBrukerErNyIArbeidslivet(), new IkkeBeregnet(SjekkOmBrukerErNyIArbeidslivet.FASTSETT_BG_FOR_SN_NY_I_ARBEIDSLIVET),
                beregnBruttoSN);

        //FP_BR 2.19 Har saksbehandler fastsatt beregningsgrunnlaget manuelt?
        Specification<BeregningsgrunnlagPeriode> sjekkOmManueltFastsattInntekt =
            rs.beregningHvisRegel(new SjekkOmManueltFastsattBeregningsgrunnlagSN(), new Beregnet(),
                sjekkOmNyIArbeidslivetSN);

        // FP_BR 2.9 Beregn oppjustert inntekt for årene i beregningsperioden
        // FP_BR 2.1 Fastsett beregningsperiode
        Specification<BeregningsgrunnlagPeriode> beregnPGI =
            rs.beregningsRegel("FP_BR 2", "Foreslå beregningsgrunnlag for selvstendig næringsdrivende",
                Arrays.asList(new FastsettBeregningsperiode(), new BeregnOppjustertInntekt()), sjekkOmManueltFastsattInntekt);

        // FP_BR 21 Fastsett beregningsgrunnlag for arbeidstakerandelen
        Specification<BeregningsgrunnlagPeriode> beregningsgrunnlagKombinasjon =
            rs.beregningsRegel("FP_BR_14-15-27-28", "Beregn beregningsgrunnlag for arbeidstaker/frilanser)",
                new RegelBeregningsgrunnlagATFL(regelmodell).getSpecification(), beregnPGI);

        return beregningsgrunnlagKombinasjon;
    }
}
