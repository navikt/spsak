package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGyldigOverføringPgaInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGyldigOverføringPgaSykdomSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOmsorgHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOppholdKvoteAnnenForelder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOverføringPgaInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeUavklart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenInnenforUkerReservertMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktOmOverføringAvKvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDager;
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
import no.nav.fpsak.nare.specification.ConditionalOrSpecification;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsetter uttakperioder med mødrekvote.
 * <p>
 * Utfall definisjoner:<br>
 * <p>
 * Utfall AVSLÅTT:<br>
 * - Det er ikke nok dager igjen på stønadskontoen for mødrekvote.<br>
 * - Perioden starter før familiehendelsen (termin/fødsel).<br>
 * <p>
 * Utfall INNVILGET:<br>
 * - Perioden er etter familiehendelse og det er nok dager på stønadskontoen for mødrekvote.<br>
 * <p>
 * UTFALL UGYLDIG_UTSETTELSE:<br>
 * - Perioden forbeholdt mor etter fødsel er ikke søkt om og har ikke gyldig utsettelsesgrunn.<br>
 * <p>
 * UTFALL GYLDIG_UTSETTELSE:<br>
 * - Perioden forbeholdt mor etter fødsel er ikke søkt om men mor har gyldig utsettelsesgrunn.<br>
 */

@RuleDocumentation(value = MødrekvoteDelregel.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=252823417")
public class MødrekvoteDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10";
    private static final String ER_SØKER_MOR = "Er søker mor?";
    private static final String RESERVERT_MØDREKVOTE = "Er perioden innenfor dager reservert for mødrekvote etter fødsel?";

    private Konfigurasjon konfigurasjon;
    private Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    public MødrekvoteDelregel() {
        // For regeldokumentasjon
    }

    public MødrekvoteDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmOppholdKvoteAnnenForelder.ID, "TODO HN")
                .hvis(new SjekkOmOppholdKvoteAnnenForelder(), sjekkOmPeriodeStarterFørFamiliehendelse())
                .ellers(sjekkOmMor());
    }

    public Specification<FastsettePeriodeGrunnlag> sjekkOmMor() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, ER_SØKER_MOR)
                .hvis(new SjekkOmSøkerErMor(), sjekkOmPeriodeStarterFørFamiliehendelse())
                .ellers(sjekkOmFarSøktOmOverføringAvMødrekvote());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOverføringPgaSykdomSkadeEllerInnleggelse() {
        return rs.hvisRegel(SjekkOmOverføringPgaInnleggelse.ID, "Er det søkt om overføring som følge av sykdom/skade eller innleggelse på institusjon?")
                .hvis(new SjekkOmOverføringPgaInnleggelse(), sjekkOmGyldigOverføringPgaInnleggelse())
                .ellers(sjekkOmGyldigOverføringPgaSykdomSkade());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigOverføringPgaInnleggelse() {
        return rs.hvisRegel(SjekkOmGyldigOverføringPgaInnleggelse.ID, "Er det avklart at overføring av kvoten er gyldig grunn av innleggelse på institusjon?")
                .hvis(new SjekkOmGyldigOverføringPgaInnleggelse(), sjekkOmPeriodeStarterFørFamiliehendelse())
                .ellers(Manuellbehandling.opprett("UT1016", IkkeOppfyltÅrsak.DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigOverføringPgaSykdomSkade() {
        return rs.hvisRegel(SjekkOmGyldigOverføringPgaSykdomSkade.ID, "Er det avklart at overføring av kvoten er gyldig grunn av sykdom/skade?")
                .hvis(new SjekkOmGyldigOverføringPgaSykdomSkade(), sjekkOmPeriodeStarterFørFamiliehendelse())
                .ellers(Manuellbehandling.opprett("UT1017", IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFarSøktOmOverføringAvMødrekvote() {
        return rs.hvisRegel(SjekkOmSøktOmOverføringAvKvote.ID, "Har far søkt om overføring av mødrekvote?")
                .hvis(new SjekkOmSøktOmOverføringAvKvote(), sjekkOmOverføringPgaSykdomSkadeEllerInnleggelse())
                .ellers(Manuellbehandling.opprett("UT1015", IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }

    public Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeStarterFørFamiliehendelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før familiehendelse?")
                .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), Manuellbehandling.opprett("UT1001", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false))
                .ellers(sjekkOmPeriodeInnenfor6ukerEtterFødsel()); // ENDRET
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeInnenfor6ukerEtterFødsel() {
        return rs.hvisRegel(SjekkOmPeriodenInnenforUkerReservertMor.ID, RESERVERT_MØDREKVOTE)
                .hvis(new SjekkOmPeriodenInnenforUkerReservertMor(konfigurasjon), sjekkOmSøkerErMor())
                .ellers(sjekkOmSøkerHarOmsorgForBarnet());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøkerErMor() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, ER_SØKER_MOR)
                .hvis(new SjekkOmSøkerErMor(), sjekkOmGraderingIPerioden())
                .ellers(sjekkOmSøkerHarOmsorgForBarnet());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøkerHarOmsorgForBarnet() {
        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmNoenDisponibleDager())
                .ellers(Manuellbehandling.opprett("UT1006", IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprettMedAvslåttGradering("UT1008", InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING, true))
                .ellers(sjekkOmNoenDisponibleDager());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmNoenDisponibleDager() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, "Er det noen disponible stønadsdager på mødrekvote?")
                .hvis(new SjekkOmTilgjengeligeDager(), sjekkOmAvklartEllerUavklartGraderingIPerioden())
                .ellers(Manuellbehandling.opprett("UT1002", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAvklartEllerUavklartGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, ER_SØKER_MOR)
                .hvis(new SjekkOmSøkerErMor(), erDetGraderingIPeriode2())
                .ellers(sjekkOmOverføringPgaInnleggelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOverføringPgaInnleggelse() {
        return rs.hvisRegel(SjekkOmOverføringPgaInnleggelse.ID, "Er det søkt om overføring som følge av sykdom/skade eller innleggelse på institusjon?")
                .hvis(new SjekkOmOverføringPgaInnleggelse(), Oppfylt.opprett("UT1172", InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT, true))
                .ellers(Oppfylt.opprett("UT1173", InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE, true));
    }

    private ConditionalOrSpecification<FastsettePeriodeGrunnlag> erDetGraderingIPeriode2() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), sjekkOmUavklart())
                .ellers(Oppfylt.opprett("UT1007", InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE, true));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUavklart() {
        return rs.hvisRegel(SjekkOmPeriodeUavklart.ID, "Er graderingen uavklart?")
                .hvis(new SjekkOmPeriodeUavklart(), Manuellbehandling.opprett("UT1005", null, Manuellbehandlingårsak.PERIODE_UAVKLART, false, false))
                .ellers(Oppfylt.opprett("UT1221", InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE, true));
    }
}
