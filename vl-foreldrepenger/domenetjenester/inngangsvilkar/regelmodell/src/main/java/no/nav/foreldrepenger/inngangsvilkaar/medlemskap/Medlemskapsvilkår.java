package no.nav.foreldrepenger.inngangsvilkaar.medlemskap;

import no.nav.foreldrepenger.inngangsvilkaar.IkkeOppfylt;
import no.nav.foreldrepenger.inngangsvilkaar.Oppfylt;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Denne implementerer regeltjenesten som validerer medlemskapsvilkåret (FP_VK_2)
 * <p>
 * Data underlag definisjoner:<br>
 * <p>
 * VilkårUtfall IKKE_OPPFYLT:<br>
 * - Bruker har ikke lovlig opphold<br>
 * - Bruker har ikke oppholdsrett<br>
 * - Bruker er utvandret<br>
 * - Bruker er avklart som ikke bosatt<br>
 * - Bruker er registrert som ikke medlem<br>
 * <p>
 * VilkårUtfall OPPFYLT:<br>
 * - Bruker er avklart som EU/EØS statsborger og har avklart oppholdsrett<br>
 * - Bruker har lovlig opphold<br>
 * - Bruker er nordisk statsborger<br>
 * - Bruker er pliktig eller frivillig medlem<br>
 *
 */

@RuleDocumentation(value = Medlemskapsvilkår.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=173827808")
public class Medlemskapsvilkår implements RuleService<MedlemskapsvilkårGrunnlag> {

    public static final String ID = "FP_VK_2";

    @Override
    public Evaluation evaluer(MedlemskapsvilkårGrunnlag grunnlag) {
        return getSpecification().evaluate(grunnlag);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<MedlemskapsvilkårGrunnlag> getSpecification() {
        Ruleset<MedlemskapsvilkårGrunnlag> rs = new Ruleset<>();

        Specification<MedlemskapsvilkårGrunnlag> brukerAvklartOppholdsrettNode =
            rs.hvisRegel(SjekkBrukerErAvklartMedOppholdsrett.ID, "Hvis bruker er avklart med oppholdsrett ...")
                .hvis(new SjekkBrukerErAvklartMedOppholdsrett(), new Oppfylt())
                .ellers(new IkkeOppfylt(SjekkBrukerErAvklartMedOppholdsrett.IKKE_OPPFYLT_BRUKER_HAR_IKKE_OPPHOLDSRETT));

        Specification<MedlemskapsvilkårGrunnlag> brukerAvklartLovligOppholdNode =
            rs.hvisRegel(SjekkBrukerErAvklartMedLovligOpphold.ID, "Hvis bruker er avklart med lovlig opphold ...")
                .hvis(new SjekkBrukerErAvklartMedLovligOpphold(), new Oppfylt())
                .ellers(new IkkeOppfylt(SjekkBrukerErAvklartMedLovligOpphold.IKKE_OPPFYLT_BRUKER_HAR_IKKE_LOVLIG_OPPHOLD));

        Specification<MedlemskapsvilkårGrunnlag> brukerAvklartEuEøsStatsborgerNode =
            rs.hvisRegel(SjekkBrukerErAvklartSomEUEØSStatsborger.ID, "Hvis ikke bruker er avklart som EU/EØS statsborger ...")
                .hvisIkke(new SjekkBrukerErAvklartSomEUEØSStatsborger(), brukerAvklartLovligOppholdNode)
                .ellers(brukerAvklartOppholdsrettNode);

        Specification<MedlemskapsvilkårGrunnlag> brukerAvklartNordiskStatsborgerNode =
            rs.hvisRegel(SjekkBrukerErAvklartSomNordiskStatsborger.ID, "Hvis ikke bruker er avklart som nordisk statsborger ...")
                .hvisIkke(new SjekkBrukerErAvklartSomNordiskStatsborger(), brukerAvklartEuEøsStatsborgerNode)
                .ellers(new Oppfylt());

        Specification<MedlemskapsvilkårGrunnlag> sjekkOmBrukHarArbeidsforholdOgInntektVedStatusIkkeBosattNode =
            rs.hvisRegel(SjekkOmBrukerHarArbeidsforholdOgInntekt.ID, "Har bruker minst ett aktivt arbeidsforhold med inntekt i relevant periode")
                .hvis(new SjekkOmBrukerHarArbeidsforholdOgInntekt(), new Oppfylt())
                .ellers(new IkkeOppfylt(SjekkOmBrukerHarArbeidsforholdOgInntekt.IKKE_OPPFYLT_IKKE_BOSATT));

        Specification<MedlemskapsvilkårGrunnlag> sjekkOmBrukHarArbeidsforholdOgInntektVedStatusUtvandretNode =
            rs.hvisRegel(SjekkOmBrukerHarArbeidsforholdOgInntekt.ID, "Har bruker minst ett aktivt arbeidsforhold med inntekt i relevant periode")
                .hvis(new SjekkOmBrukerHarArbeidsforholdOgInntekt(), new Oppfylt())
                .ellers(new IkkeOppfylt(SjekkBrukerErAvklartSomBosattEllerDød.IKKE_OPPFYLT_BRUKER_ER_UTVANDRET));

        Specification<MedlemskapsvilkårGrunnlag> brukerAvklartSomIkkeBosattNode =
            rs.hvisRegel(SjekkBrukerErAvklartSomIkkeBosatt.ID, "Hvis ikke bruker er avklart som ikke bosatt")
                .hvisIkke(new SjekkBrukerErAvklartSomIkkeBosatt(), brukerAvklartNordiskStatsborgerNode)
                .ellers(sjekkOmBrukHarArbeidsforholdOgInntektVedStatusIkkeBosattNode);

        Specification<MedlemskapsvilkårGrunnlag> brukerRegistrertSomBosattNode =
            rs.hvisRegel(SjekkBrukerErAvklartSomBosattEllerDød.ID, "Hvis bruker er avklart som bosatt eller død ...")
                .hvis(new SjekkBrukerErAvklartSomBosattEllerDød(), brukerAvklartSomIkkeBosattNode)
                .ellers(sjekkOmBrukHarArbeidsforholdOgInntektVedStatusUtvandretNode); //Hvis utvandret

        Specification<MedlemskapsvilkårGrunnlag> brukerPliktigEllerFrivilligMedlemNode =
            rs.hvisRegel(SjekkBrukerErAvklartSomPliktigEllerFrivilligMedlem.ID, "Hvis bruker ikke er avklart som pliktig eller frivillig medlem ...")
                .hvisIkke(new SjekkBrukerErAvklartSomPliktigEllerFrivilligMedlem(), brukerRegistrertSomBosattNode)
                .ellers(new Oppfylt());

        Specification<MedlemskapsvilkårGrunnlag> opphørPåGrunAvEndringerITps =
            rs.hvisRegel(SjekkOmSaksbehandlerHarSattOpphørPåGrunnAvEndringerITps.ID, "Hvis saksbehandler setter opphør av medlemskap på grunn av endringer i tps ...")
                .hvisIkke(new SjekkOmSaksbehandlerHarSattOpphørPåGrunnAvEndringerITps(), brukerPliktigEllerFrivilligMedlemNode)
                .ellers(new IkkeOppfylt(SjekkOmSaksbehandlerHarSattOpphørPåGrunnAvEndringerITps.IKKE_OPPFYLT_SAKSBEHANDLER_SETTER_OPPHØR_PGA_ENDRINGER_I_TPS));

        Specification<MedlemskapsvilkårGrunnlag> brukerIkkeMedlemNode =
            rs.hvisRegel(SjekkBrukerErAvklartSomIkkeMedlem.ID, "Hvis ikke bruker er avklart som ikke medlem ...")
                .hvisIkke(new SjekkBrukerErAvklartSomIkkeMedlem(), opphørPåGrunAvEndringerITps)
                .ellers(new IkkeOppfylt(SjekkBrukerErAvklartSomIkkeMedlem.IKKE_OPPFYLT_BRUKER_ER_OPPFØRT_SOM_IKKE_MEDLEM));

        return brukerIkkeMedlemNode;
    }
}
