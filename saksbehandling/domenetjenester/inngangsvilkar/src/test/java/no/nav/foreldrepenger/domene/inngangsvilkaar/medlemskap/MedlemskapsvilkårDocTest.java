package no.nav.foreldrepenger.domene.inngangsvilkaar.medlemskap;

import org.junit.Test;

import no.nav.foreldrepenger.domene.inngangsvilkaar.medlemskap.Medlemskapsvilkår;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.specification.Specification;

public class MedlemskapsvilkårDocTest {

    @Test
    public void test_documentation() throws Exception {
        Specification<MedlemskapsvilkårGrunnlag> vilkår = new Medlemskapsvilkår().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(vilkår.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }
}
