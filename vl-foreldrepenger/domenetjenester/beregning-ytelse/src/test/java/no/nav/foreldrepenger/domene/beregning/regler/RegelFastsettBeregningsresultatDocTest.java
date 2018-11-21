package no.nav.foreldrepenger.domene.beregning.regler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatRegelmodellMellomregning;
import no.nav.foreldrepenger.domene.beregning.regler.RegelFastsettBeregningsresultat;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsettBeregningsresultatDocTest {

    @Test
    public void test_documentation() throws Exception {
        Specification<BeregningsresultatRegelmodellMellomregning> beregning = new RegelFastsettBeregningsresultat().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        String json = digraph.toJson();

        assertThat(json.indexOf("\"edges\" : [ ]")).isLessThan(0);
    }
}
