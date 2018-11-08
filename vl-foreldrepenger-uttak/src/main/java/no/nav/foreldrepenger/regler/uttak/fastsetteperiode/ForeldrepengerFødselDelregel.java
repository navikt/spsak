package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkGyldigGrunnForTidligOppstartHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareFarMedmorHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareMorHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmErAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOmsorgHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeUavklart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenInnenforUkerReservertMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenSlutterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakSkjerFørDeFørsteUkene;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttaketStarterFørLovligUttakFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.FastsettePeriodeUtfall;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.ConditionalOrSpecification;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = ForeldrepengerFødselDelregel.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=292407153")
public class ForeldrepengerFødselDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK XX10";

    private Konfigurasjon konfigurasjon;

    private Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    public ForeldrepengerFødselDelregel() {
        // For dokumentasjonsgenerering
    }

    ForeldrepengerFødselDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, "Er søker mor?")
                .hvis(new SjekkOmSøkerErMor(), sjekkOmUttaketStarterFørLovligUttakFørFødsel())
                .ellers(sjekkOmUttakSkalVæreFørFamileHendelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttaketStarterFørLovligUttakFørFødsel() {
        return rs.hvisRegel(SjekkOmUttaketStarterFørLovligUttakFørFødsel.ID, "Skal uttaket starte tidligere enn 12 uker før termindato?")
                .hvis(new SjekkOmUttaketStarterFørLovligUttakFørFødsel(konfigurasjon), Manuellbehandling.opprett("UT1185", IkkeOppfyltÅrsak.MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL, Manuellbehandlingårsak.SØKNADSFRIST, false, false))
                .ellers(sjekkErDetAleneomsorgMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetAleneomsorgMor() {
        return rs.hvisRegel(SjekkOmErAleneomsorg.ID, "Er det aleneomsorg?")
                .hvis(new SjekkOmErAleneomsorg(), sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel())
                .ellers(sjekkErDetBareMorSomHarRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetBareMorSomHarRett() {
        return rs.hvisRegel(SjekkOmBareMorHarRett.ID, "Er det bare mor som har rett?")
                .hvis(new SjekkOmBareMorHarRett(), sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel())
                .ellers(Manuellbehandling.opprett("UT1209", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel() {
        return rs.hvisRegel(SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel.ID, "Starter perioden før 3 uker før termin/fødsel?")
                .hvis(new SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel(konfigurasjon), sjekkErDetNoenDisponibleStønadsdagerPåKvotenMor())
                .ellers(sjekkOmPeriodenStarterFørFamilieHendelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetNoenDisponibleStønadsdagerPåKvotenMor() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, SjekkOmTilgjengeligeDager.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDager(), sjekkOmGraderingIPeriodenFørXUkerEtterFamiliehendelseMor())
                .ellers(Manuellbehandling.opprett("UT1205", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPeriodenFørXUkerEtterFamiliehendelseMor() {
        Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettUtenGradering = erDetBareMorSomHarRettSjekk(
                Oppfylt.opprett("UT1211", InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, true),
                Oppfylt.opprett("UT1186", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true));
        Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettVedGradering = erDetBareMorSomHarRettSjekk(
                Oppfylt.opprettMedAvslåttGradering("UT1212", InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING, true),
                Oppfylt.opprettMedAvslåttGradering("UT1187", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING, true));
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), erDetBareMorSomHarRettVedGradering)
                .ellers(erDetBareMorSomHarRettUtenGradering);
    }

    private Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettSjekk(FastsettePeriodeUtfall utfallJa, FastsettePeriodeUtfall utfallNei) {
        return rs.hvisRegel(SjekkOmBareMorHarRett.ID, "Er det bare mor som har rett?")
                .hvis(new SjekkOmBareMorHarRett(), utfallJa)
                .ellers(utfallNei);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterFørFamilieHendelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før termin/fødsel?")
                .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), Manuellbehandling.opprett("UT1192", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false))
                .ellers(sjekkErPeriodenInnenforUkerReservertMor());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErPeriodenInnenforUkerReservertMor() {
        return rs.hvisRegel(SjekkOmPeriodenInnenforUkerReservertMor.ID, "Er perioden innenfor 6 uker etter fødsel?")
                .hvis(new SjekkOmPeriodenInnenforUkerReservertMor(konfigurasjon), sjekkErDetNoenDisponibleStønadsdagerPåKvotenMor())
                .ellers(sjekkOmMorHarOmsorgForBarnet());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorHarOmsorgForBarnet() {
        ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmTilgjengeligeDager =
                rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, SjekkOmTilgjengeligeDager.BESKRIVELSE)
                        .hvis(new SjekkOmTilgjengeligeDager(), sjekkOmGraderingIPeriodenXUkerEtterFamilieHendelseForMor())
                        .ellers(IkkeOppfylt.opprett("UT1188", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmTilgjengeligeDager)
                .ellers(IkkeOppfylt.opprett("UT1191", IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPeriodenXUkerEtterFamilieHendelseForMor() {
        Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettSjekk = erDetBareMorSomHarRettSjekk(
                Oppfylt.opprett("UT1214", InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, true),
                Oppfylt.opprett("UT1190", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true));
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), sjekkOmPeriodenAvklartMor())
                .ellers(erDetBareMorSomHarRettSjekk);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenAvklartMor() {
        Specification<FastsettePeriodeGrunnlag> erDetBareMorSomHarRettSjekk = erDetBareMorSomHarRettSjekk(
                Oppfylt.opprett("UT1213", InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT, true),
                Oppfylt.opprett("UT1210", InnvilgetÅrsak.GRADERING_ALENEOMSORG, true));
        return rs.hvisRegel(SjekkOmPeriodeUavklart.ID, "Er graderingen uavklart?")
                .hvis(new SjekkOmPeriodeUavklart(), Manuellbehandling.opprett("UT1189", null, Manuellbehandlingårsak.PERIODE_UAVKLART, false, false))
                .ellers(erDetBareMorSomHarRettSjekk);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkalVæreFørFamileHendelse() {
        return rs.hvisRegel(SjekkOmPeriodenSlutterFørFamiliehendelse.ID, "Skal uttak være før termin/fødsel?")
                .hvis(new SjekkOmPeriodenSlutterFørFamiliehendelse(), Manuellbehandling.opprett("UT1193", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false))
                .ellers(sjekkErDetAleneomsorgFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetAleneomsorgFar() {
        return rs.hvisRegel(SjekkOmErAleneomsorg.ID, "Er det aleneomsorg?")
                .hvis(new SjekkOmErAleneomsorg(), sjekkOmFarMedAleneomsorgHarOmsorgForBarnet())
                .ellers(sjekkErDetBareFarMedmorSomHarRett());
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgHarOmsorgForBarnet() {
        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                    .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmFarMedAleneomsorgHarDisponibleDager())
                    .ellers(IkkeOppfylt.opprett("UT1194", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgHarDisponibleDager() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, SjekkOmTilgjengeligeDager.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDager(), sjekkOmFarMedAleneomsorgGraderingIPerioden())
                .ellers(IkkeOppfylt.opprett("UT1195", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false));
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), sjekkOmFarMedAleneomsorgErUavklart())
                .ellers(Oppfylt.opprett("UT1198", InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, true));
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> sjekkOmFarMedAleneomsorgErUavklart() {
        return rs.hvisRegel(SjekkOmPeriodeUavklart.ID, "Er perioden uavklart?")
                .hvis(new SjekkOmPeriodeUavklart(), Manuellbehandling.opprett("UT1197", null, Manuellbehandlingårsak.PERIODE_UAVKLART, false, false))
                .ellers(Oppfylt.opprett("UT1196", InnvilgetÅrsak.GRADERING_ALENEOMSORG, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkErDetBareFarMedmorSomHarRett() {
        return rs.hvisRegel(SjekkOmBareFarMedmorHarRett.ID, "Er det bare far/medmor som har rett?")
                .hvis(new SjekkOmBareFarMedmorHarRett(), sjekkOmFarUtenAleneomsorgHarOmsorgForBarnet())
                .ellers(Manuellbehandling.opprett("UT1204", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFarUtenAleneomsorgHarOmsorgForBarnet() {
        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmUttakSkjerFørDeFørsteUkene())
                .ellers(IkkeOppfylt.opprett("UT1199", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkjerFørDeFørsteUkene() {
        return rs.hvisRegel(SjekkOmUttakSkjerFørDeFørsteUkene.ID, "Starter perioden før uke 7 etter termin/fødsel?")
                .hvis(new SjekkOmUttakSkjerFørDeFørsteUkene(konfigurasjon), sjekkOmGyldigGrunnForTidligOppstart())
                .ellers(sjekkFarUtenAleneomsorgHarDisponibleDager());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigGrunnForTidligOppstart() {
        return rs.hvisRegel(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID, "Foreligger et gyldig grunn for hele perioden for tidlig oppstart?")
                .hvis(new SjekkGyldigGrunnForTidligOppstartHelePerioden(), sjekkOmFarUtenAleneomsorgGraderingIPerioden())
                .ellers(Manuellbehandling.opprett("UT1200", null, Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFarUtenAleneomsorgGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), sjekkOmPeriodenAvklartFar())
                .ellers(Manuellbehandling.opprett("UT1201", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenAvklartFar() {
        return rs.hvisRegel(SjekkOmPeriodeUavklart.ID, "Er perioden uavklart?")
                .hvis(new SjekkOmPeriodeUavklart(), Manuellbehandling.opprett("UT1202", null, Manuellbehandlingårsak.PERIODE_UAVKLART, false, false))
                .ellers(Manuellbehandling.opprett("1216", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkFarUtenAleneomsorgHarDisponibleDager() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, SjekkOmTilgjengeligeDager.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDager(), sjekkOmFarUtenAleneomsorgGraderingIPerioden())
                .ellers(IkkeOppfylt.opprett("UT1203", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false));
    }
}
