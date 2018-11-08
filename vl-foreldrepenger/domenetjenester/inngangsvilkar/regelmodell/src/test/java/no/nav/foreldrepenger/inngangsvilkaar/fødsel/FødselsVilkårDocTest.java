package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import org.junit.Test;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.specification.Specification;

public class FødselsVilkårDocTest {

    @Test
    public void test_documentation() throws Exception {
        Specification<FødselsvilkårGrunnlag> vilkår = new FødselsvilkårMor().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(vilkår.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }
}
