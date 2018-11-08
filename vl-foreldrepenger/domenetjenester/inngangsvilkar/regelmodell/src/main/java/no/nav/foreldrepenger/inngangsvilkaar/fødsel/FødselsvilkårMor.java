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
 * Denne implementerer regeltjenesten som validerer fødselsvilkåret (FP_VK_1)
 * Data underlag definisjoner:<br>
 * - Bekreftet passet 26 svangerskapsuker dato: termindato - 3 dager - 14 uker<br>
 * - Bekreftet passert 26 svangerskapsuker: søknadsdato>=passet 26 svangerskapsuker<br>
 * <p>
 * VilkårUtfall IKKE_OPPFYLT:<br>
 * - Hvis ikke kvinne: Returner VilkårUtfallMerknad 1003. <br>
 * - Hvis kvinne, fødsel registerert, søker ikke barnets mor: 1002<br>
 * - Hvis kvinne, fødsel ikke registrert, ikke passert 26 svangerskapsuker: 1001<br>
 * <p>
 * VilkårUtfall OPPFYLT:<br>
 * - Fødsel registrert og søker er barnets mor (MORA)<br>
 * - Fødsel ikke registert, søker er kvinne, og passert 26 svangerskapsuker.<br>
 *
 */
@RuleDocumentation(value = FødselsvilkårMor.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=173827762")
public class FødselsvilkårMor implements RuleService<FødselsvilkårGrunnlag> {

    public static final String ID = "FP_VK_1";

    @Override
    public Evaluation evaluer(FødselsvilkårGrunnlag data) {
        return getSpecification().evaluate(data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<FødselsvilkårGrunnlag> getSpecification() {
        Ruleset<FødselsvilkårGrunnlag> rs = new Ruleset<>();

        Specification<FødselsvilkårGrunnlag> søknadsdatoPassertXSvangerskapsukeNode =
            rs.hvisRegel("FP_VK_1.2.1.1", "Hvis søknadsdato har passert X svangerskapsuke ...")
                .hvis(new SjekkSøknadsdatoPassertXSvangerskapsUker(), new Oppfylt())
                .ellers(new IkkeOppfylt(SjekkSøknadsdatoPassertXSvangerskapsUker.IKKE_OPPFYLT_PASSERT_TIDLIGSTE_SVANGERSKAPSUKE_KAN_SØKE));

        Specification<FødselsvilkårGrunnlag> burdeFødselHaInntruffetNode =
            rs.hvisRegel("FP_VK_1.2.1", "Hvis ikke fødsel burde ha inntruffet ...")
                .hvis(new SjekkErDetForTidligeForAtFødselBurdeHaInntruffet(), søknadsdatoPassertXSvangerskapsukeNode)
                .ellers(new IkkeOppfylt(SjekkErDetForTidligeForAtFødselBurdeHaInntruffet.FØDSEL_BURDE_HA_INNTRUFFET));

        Specification<FødselsvilkårGrunnlag> søkerErMorNode =
            rs.hvisRegel("FP_VK_1.2.2", "Hvis søker er mor ...")
                .hvis(new SjekkSøkerErMor(), new Oppfylt())
                .ellers(new IkkeOppfylt(SjekkSøkerErMor.IKKE_OPPFYLT_FØDSEL_REGISTRERT_SØKER_IKKE_BARNETS_MOR));

        Specification<FødselsvilkårGrunnlag> harSøkerFødtNode = rs
            .hvisRegel("FP_VK_1.2", "Hvis ikke fødsel er registert ...")
            .hvisIkke(new SjekkFødselErRegistrert(), burdeFødselHaInntruffetNode)
            .ellers(søkerErMorNode);

        Specification<FødselsvilkårGrunnlag> søkerErKvinneNode =
            rs.hvisRegel("FP_VK_1.1", "Hvis søker er kvinne ...")
                .hvis(new SjekkSøkerErKvinne(), harSøkerFødtNode)
                .ellers(new IkkeOppfylt(SjekkSøkerErKvinne.IKKE_OPPFYLT_SØKER_ER_KVINNE));

        return søkerErKvinneNode;
    }
}
