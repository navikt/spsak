package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkGyldigGrunnForTidligOppstartHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOmsorgHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeUavklart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenSlutterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakSkjerEtterDeFørsteUkene;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakSkjerFørDeFørsteUkene;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttaketStarterFørLovligUttakFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsette uttak av fellesperiode.
 * <p>
 * Utfall definisjoner:<br>
 * <p>
 * Utfall AVSLÅTT:<br>
 * - Det er ikke nok dager igjen på stønadskontoen for fellesperioden.<br>
 * - Perioden starter for tidlig før familiehendelsen (termin/fødsel)
 * - Perioden starter i periode etter fødsel som er forbeholdt mor.<br>
 * <p>
 * Utfall INNVILGET:<br>
 * - Perioden starter før fødsel og det er nok dager på stønadskonto for fellesperiode. <br>
 * - Perioden er etter ukene etter fødsel som er forbeholdt mor og det er nok dager på stønadskontoen for fellesperiode.<br>
 */

@RuleDocumentation(value = FellesperiodeDelregel.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=252823617")
public class FellesperiodeDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 19";

    private Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    private static final String GRADERING_UAVKLART = "Er graderingen uavklart?";


    private Konfigurasjon konfigurasjon;

    public FellesperiodeDelregel() {
        // For dokumentasjonsgenerering
    }

    FellesperiodeDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, "Gjelder søknaden fellesperiode for mor")
            .hvis(new SjekkOmSøkerErMor(), sjekkOmUttaketStarterFørLovligUttakFørFødsel())
            .ellers(sjekkOmUttakSkjerFørDeFørsteUkene());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttaketStarterFørLovligUttakFørFødsel() {
        return rs.hvisRegel(SjekkOmUttaketStarterFørLovligUttakFørFødsel.ID, "Skal uttaket starte tidligere enn 12 uker før termin/fødsel?")
            .hvis(new SjekkOmUttaketStarterFørLovligUttakFørFødsel(konfigurasjon), Manuellbehandling.opprett("UT1040", IkkeOppfyltÅrsak.MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL, Manuellbehandlingårsak.SØKNADSFRIST, false, false))
            .ellers(sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel() {
        return rs.hvisRegel(SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel.ID, "Starter perioden før 3 uker før termin/fødsel?")
            .hvis(new SjekkOmUttakStarterFørUttakForForeldrepengerFørFødsel(konfigurasjon), sjekkOmGraderingIPerioden())
            .ellers(sjekkOmUttakSkjerEtterDeFørsteUkene());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, GRADERING_UAVKLART)
            .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprettMedAvslåttGradering("UT1064", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING, true))
            .ellers(Oppfylt.opprett("UT1041", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkjerEtterDeFørsteUkene() {
        return rs.hvisRegel(SjekkOmUttakSkjerEtterDeFørsteUkene.ID, "Er perioden tidligst fra uke 7 etter termin/fødsel?")
            .hvis(new SjekkOmUttakSkjerEtterDeFørsteUkene(konfigurasjon), sjekkSaldoForMor())
            .ellers(Manuellbehandling.opprett("UT1048", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkSaldoForMor() {
        Specification<FastsettePeriodeGrunnlag> erGraderingenUavklart =
            rs.hvisRegel(SjekkOmPeriodeUavklart.ID, GRADERING_UAVKLART)
                .hvis(new SjekkOmPeriodeUavklart(), Manuellbehandling.opprett("UT1169", null, Manuellbehandlingårsak.PERIODE_UAVKLART, true, false))
                .ellers(Oppfylt.opprett("UT1219", InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER, true));

        Specification<FastsettePeriodeGrunnlag> erDetGraderingIPeriodenNode =
            rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), erGraderingenUavklart)
                .ellers(Oppfylt.opprett("UT1047", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, true));

        Specification<FastsettePeriodeGrunnlag> noenTilgjengligeDagerNode =
            rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, "Er det tilgjengelige dager på fellesperioden?")
                .hvis(new SjekkOmTilgjengeligeDager(), erDetGraderingIPeriodenNode)
                .ellers(Manuellbehandling.opprett("UT1043", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), noenTilgjengligeDagerNode)
            .ellers(Manuellbehandling.opprett("UT1046", IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUttakSkjerFørDeFørsteUkene() {
        return rs.hvisRegel(SjekkOmUttakSkjerFørDeFørsteUkene.ID, "Starter perioden før uke 7 etter termin/fødsel?")
            .hvis(new SjekkOmUttakSkjerFørDeFørsteUkene(konfigurasjon), sjekkOmPeriodenSlutterFørFamiliehendelse())
            .ellers(delFlytForVanligUttak());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenSlutterFørFamiliehendelse() {
        return rs.hvisRegel(SjekkOmPeriodenSlutterFørFamiliehendelse.ID, "Skal uttaksperioden være før termin/fødsel?")
            .hvis(new SjekkOmPeriodenSlutterFørFamiliehendelse(), Manuellbehandling.opprett("UT1049", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO,
                    false,false))
            .ellers(sjekkGyldigGrunnForTidligOppstartHelePerioden());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkGyldigGrunnForTidligOppstartHelePerioden() {
        return rs.hvisRegel(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID, "Foreligger det gyldig grunn for hele perioden for tidlig oppstart?")
            .hvis(new SjekkGyldigGrunnForTidligOppstartHelePerioden(), delFlytForTidligUttak())
            .ellers(Manuellbehandling.opprett("UT1050", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO,false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForTidligUttak() {

        Specification<FastsettePeriodeGrunnlag> erGraderingenUavklart =
            rs.hvisRegel(SjekkOmPeriodeUavklart.ID, GRADERING_UAVKLART)
                .hvis(new SjekkOmPeriodeUavklart(), Manuellbehandling.opprett("UT1170", null, Manuellbehandlingårsak.PERIODE_UAVKLART, false, false))
                .ellers(Oppfylt.opprett("UT1220", InnvilgetÅrsak.GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER, true));

        Specification<FastsettePeriodeGrunnlag> omGradertPeriodeNode =
            rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), erGraderingenUavklart)
                .ellers(Oppfylt.opprett("UT1055", InnvilgetÅrsak.FELLESPERIODE_ELLER_FORELDREPENGER, true));

        Specification<FastsettePeriodeGrunnlag> noenDisponibleDagerNode =
            rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, SjekkOmTilgjengeligeDager.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDager(), omGradertPeriodeNode)
                .ellers(Manuellbehandling.opprett("UT1051", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), noenDisponibleDagerNode)
            .ellers(Manuellbehandling.opprett("UT1054", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForVanligUttak() {
        Specification<FastsettePeriodeGrunnlag> erGraderingenUavklart =
            rs.hvisRegel(SjekkOmPeriodeUavklart.ID, GRADERING_UAVKLART)
                .hvis(new SjekkOmPeriodeUavklart(), Manuellbehandling.opprett("UT1179", null, Manuellbehandlingårsak.PERIODE_UAVKLART, false, false))
                .ellers(Manuellbehandling.opprett("UT1061", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false));

        Specification<FastsettePeriodeGrunnlag> omGradertPeriodeNode =
            rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), erGraderingenUavklart)
                .ellers(Manuellbehandling.opprett("UT1061", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false));

        Specification<FastsettePeriodeGrunnlag> noenDisponibleDagerNode =
            rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, "Er det disponibelt antall stønadsdager på fedrekvoten?")
                .hvis(new SjekkOmTilgjengeligeDager(), omGradertPeriodeNode)
                .ellers(Manuellbehandling.opprett("UT1146", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), noenDisponibleDagerNode)
            .ellers(Manuellbehandling.opprett("UT1060", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG, true, false));
    }

}
