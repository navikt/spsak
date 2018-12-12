package no.nav.foreldrepenger.domene.inngangsvilkaar.søknadsfrist;

import java.time.Period;

import no.nav.foreldrepenger.domene.inngangsvilkaar.Oppfylt;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.SoeknadsfristvilkarGrunnlag;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Denne implementerer regeltjenesten som validerer søknadsfristvilkåret (FP_VK_3)
 * <p>
 * Data underlag definisjoner:<br>
 * <p>
 * VilkårUtfall IKKE_OPPFYLT:<br>
 * <p>
 * VilkårUtfall OPPFYLT:<br>
 * - elektronisk søknad og innen 3 måneder
 *
 * <p>
 * VilkårUtfall IKKE_VURDERT:<br>
 * - Elektronisk søknad og ikke motatt innen 3 måneder: Vilkårutfallmerknad.VM_5007 + merknad
 * "antallDagerSoeknadLevertForSent"
 */
@RuleDocumentation(value = "FP_VK_3", specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=173827683")
public class Søknadsfristvilkår implements RuleService<SoeknadsfristvilkarGrunnlag> {

    @Override
    public Evaluation evaluer(SoeknadsfristvilkarGrunnlag data) {
        return getSpecification().evaluate(data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<SoeknadsfristvilkarGrunnlag> getSpecification() {
        Ruleset<SoeknadsfristvilkarGrunnlag> rs = new Ruleset<>();

        Specification<SoeknadsfristvilkarGrunnlag> vilkår = rs.hvisRegel("FP_VK_3", "Har søkt innen for tiden")
            .hvis(new SjekkFristForSøknad(Period.ofMonths(3), 0), new Oppfylt())
            .build();

        return vilkår;

    }

    @Override
    public String toString() {
        return getSpecification().toString();
    }
}
