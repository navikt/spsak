package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import no.nav.foreldrepenger.inngangsvilkaar.IkkeOppfylt;
import no.nav.foreldrepenger.inngangsvilkaar.Oppfylt;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Denne implementerer regeltjenesten som validerer fødselsvilkåret (FP_VK_11)
 * Data underlag definisjoner:<br>
 * VilkårUtfall IKKE_OPPFYLT:<br>
 * - Fødsel registrert og søker er ikke barnets far/medmor<br>
 * - Fødsel ikke registert, mor er for syk til å ta vare på barnet, og søker er ikke barnets far/medmor (FARA)<br>
 * - Fødsel ikke registert, mor er ikke for syk til å ta vare på barnet<br>
 * <p>
 * VilkårUtfall OPPFYLT:<br>
 * - Fødsel registrert og søker er barnets far/medmor (FARA)<br>
 * - Fødsel ikke registert, mor er for syk til å ta vare på barnet, og søker er barnets far/medmor (FARA)<br>
 *
 */
@RuleDocumentation(value = FødselsvilkårFar.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=198890456")
public class FødselsvilkårFar implements RuleService<FødselsvilkårGrunnlag> {

    public static final String ID = "FP_VK_11";

    @Override
    public Evaluation evaluer(FødselsvilkårGrunnlag data) {
        return getSpecification().evaluate(data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<FødselsvilkårGrunnlag> getSpecification() {
        Ruleset<FødselsvilkårGrunnlag> rs = new Ruleset<>();

        Specification<FødselsvilkårGrunnlag> kanMorTaSegAvBarnetEtterFødselNode =
            rs.hvisRegel("FP_VK_11.4", "Er det sannsynlig at mor er så syk etter fødselen at hun ikke kan ta seg av barnet ...")
                .hvis(new SjekkMorForSykTilÅTaVarePåBarn(), new Oppfylt())
                .ellers(new IkkeOppfylt(SjekkMorForSykTilÅTaVarePåBarn.MOR_IKKE_FOR_SYK_TIL_Å_TA_VARE_PÅ_BARN));

        Specification<FødselsvilkårGrunnlag> harSøktOmTerminNode =
            rs.hvisRegel("FP_VK_11.3", "Har søker familierelasjon far/medmor til barnet ...")
                .hvis(new SjekkSøktOmTermin(), kanMorTaSegAvBarnetEtterFødselNode)
                .ellers(new IkkeOppfylt(SjekkSøktOmTermin.IKKE_OPPFYLT_BARN_DOKUMENTERT_PÅ_FAR_MEDMOR));

        return rs.hvisRegel("FP_VK_11.2", "Er fødsel bekreftet ...")
                .hvis(new SjekkFødselErRegistrert(), new Oppfylt())
                .ellers(harSøktOmTerminNode);
    }
}
