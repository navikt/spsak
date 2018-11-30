package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker;

import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.avkorting.RegelFastsettUtbetalingsbeløpTilBruker;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.dok.DokumentasjonRegelBeregnBruttoPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.dok.DokumentasjonRegelBeregningsgrunnlagATFL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.dok.DokumentasjonRegelFastsettAvkortetBGOver6GNårRefusjonUnder6G;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.dok.DokumentasjonRegelFastsettAvkortetVedRefusjonOver6G;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.dok.DokumentasjonRegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.dok.DokumentasjonRegelForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.dok.DokumentasjonRegelFullføreBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette.refusjon.over6g.RegelBeregnRefusjonPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse.dagpengerelleraap.RegelFastsettBeregningsgrunnlagDPellerAAP;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.specification.Specification;

public class BeregningsgrunnlagDocTest {

    @Test
    public void testKombinasjonATFLSN() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }

    @Test
    public void testRegelFastsettBeregningsgrunnlagDPellerAAP() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new RegelFastsettBeregningsgrunnlagDPellerAAP().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }

    @Test
    public void testRegelFastsettAvkortetVedRefusjonOver6G() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelFastsettAvkortetVedRefusjonOver6G().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }

    @Test
    public void testRegelFastsettAvkortetBGOver6GNårRefusjonUnder6G() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelFastsettAvkortetBGOver6GNårRefusjonUnder6G().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }

    @Test
    public void testRegelBeregnRefusjonPrArbeidsforhold() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new RegelBeregnRefusjonPrArbeidsforhold().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }

    @Test
    public void testRegelBeregningsgrunnlagATFL() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelBeregningsgrunnlagATFL().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }

    @Test
    public void testRegelBeregnBruttoPrArbeidsforhold() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelBeregnBruttoPrArbeidsforhold().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }

    @Test
    public void testRegelFastsettUtbetalingsbeløpTilBruker() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new RegelFastsettUtbetalingsbeløpTilBruker().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }

    @Test
    public void testRegelForeslåBeregningsgrunnlag() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelForeslåBeregningsgrunnlag().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }

    @Test
    public void testRegelFullføreBeregningsgrunnlag() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new DokumentasjonRegelFullføreBeregningsgrunnlag().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }
}
