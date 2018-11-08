package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkGyldigGrunnForTidligOppstartHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGyldigOverføringPgaInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGyldigOverføringPgaSykdomSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOmsorgHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOppholdKvoteAnnenForelder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOverføringPgaInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeUavklart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenSlutterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørUke7;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktOmOverføringAvKvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
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
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsetter uttaksperioder med fedrekvote.
 * <p>
 * Utfall definisjoner:<br>
 * <p>
 * Utfall AVSLÅTT:<br>
 * - Det er ikke nok dager igjen på stønadskontoen for fedrekvote.<br>
 * - Perioden starter før familiehendelsen (termin/fødsel).<br>
 * - Perioden starter i periode etter fødsel som er forbeholdt mor og har ikke gyldig grunn for dette. <br>
 * <p>
 * Utfall INNVILGET:<br>
 * - Perioden er etter ukene etter fødsel som er forbeholdt mor og det er nok dager på stønadskontoen for fedrekvote.<br>
 * - Perioden har gyldig grunn for å starte i ukene etter fødsel som er forbeholdt mor og det er nok dager på stønadskontoen for fedrekvote.<br>
 */

@RuleDocumentation(value = FedrekvoteDelregel.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=252823417")
public class FedrekvoteDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 13";

    private static final String ER_SØKER_FAR = "Er søker far?";
    private static final String ER_DET_SØKT_OM_OVERFØRING = "Er det søkt om overføring som følge av sykdom/skade eller innleggelse på institusjon?";

    private Konfigurasjon konfigurasjon;
    private Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    public FedrekvoteDelregel() {
        // For dokumentasjonsgenerering
    }

    FedrekvoteDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmOppholdKvoteAnnenForelder.ID, "TODO HN")
            .hvis(new SjekkOmOppholdKvoteAnnenForelder(), sjekkOmPeriodeStarterFørFamiliehendelse())
            .ellers(sjekkOmSøkerErFar());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøkerErFar() {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, ER_SØKER_FAR)
                .hvis(new SjekkOmSøkerErMor(), sjekkOmMorSøktOmOverføringAvFedrekvote())
                .ellers(sjekkOmPeriodeStarterFørFamiliehendelse());
                }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOverføringPgaSykdomSkadeEllerInnleggelse() {
        return rs.hvisRegel(SjekkOmOverføringPgaInnleggelse.ID, ER_DET_SØKT_OM_OVERFØRING)
                .hvis(new SjekkOmOverføringPgaInnleggelse(), sjekkOmGyldigOverføringPgaInnleggelse())
                .ellers(sjekkOmGyldigOverføringPgaSykdomSkade());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigOverføringPgaInnleggelse() {
        return rs.hvisRegel(SjekkOmGyldigOverføringPgaInnleggelse.ID, "Er det avklart at overføring av kvoten er gyldig grunn av innleggelse på institusjon?")
                .hvis(new SjekkOmGyldigOverføringPgaInnleggelse(), sjekkOmPeriodeStarterFørFamiliehendelse())
                .ellers(Manuellbehandling.opprett("UT1033", IkkeOppfyltÅrsak.DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGyldigOverføringPgaSykdomSkade() {
        return rs.hvisRegel(SjekkOmGyldigOverføringPgaSykdomSkade.ID, "Er det avklart at overføring av kvoten er gyldig grunn av sykdom/skade?")
                .hvis(new SjekkOmGyldigOverføringPgaSykdomSkade(), sjekkOmPeriodeStarterFørFamiliehendelse())
                .ellers(Manuellbehandling.opprett("UT1034", IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorSøktOmOverføringAvFedrekvote() {
        return rs.hvisRegel(SjekkOmSøktOmOverføringAvKvote.ID, "Har mor søkt om overføring av fedrekvoten?")
                .hvis(new SjekkOmSøktOmOverføringAvKvote(), sjekkOmOverføringPgaSykdomSkadeEllerInnleggelse())
                .ellers(Manuellbehandling.opprett("UT1032", IkkeOppfyltÅrsak.DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }

    public Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeStarterFørFamiliehendelse() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørUke7.ID, "Starter perioden før uke 7 etter termin/fødsel?")
                .hvis(new SjekkOmPeriodenStarterFørUke7(konfigurasjon), uttakFørTerminFødsel())
                .ellers(delFlytForVanligUttak());
    }

    private Specification<FastsettePeriodeGrunnlag> uttakFørTerminFødsel() {
        return rs.hvisRegel(SjekkOmPeriodenSlutterFørFamiliehendelse.ID, "Skal uttaksperioden være før termin/fødsel?")
                .hvis(new SjekkOmPeriodenSlutterFørFamiliehendelse(), Manuellbehandling.opprett("UT1020", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO,
                        false, false)).ellers(gyldigGrunnForTidligUttak());
    }

    private Specification<FastsettePeriodeGrunnlag> gyldigGrunnForTidligUttak() {
        return rs.hvisRegel(SjekkGyldigGrunnForTidligOppstartHelePerioden.ID, "Foreligger det gyldig grunn for hele perioden for tidlig oppstart?")
                .hvis(new SjekkGyldigGrunnForTidligOppstartHelePerioden(), delFlytForTidligUttak())
                .ellers(Manuellbehandling.opprett("UT1021", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO,false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForOverføringVedTidligUttak() {
        return rs.hvisRegel(SjekkOmOverføringPgaInnleggelse.ID, ER_DET_SØKT_OM_OVERFØRING)
                .hvis(new SjekkOmOverføringPgaInnleggelse(), Oppfylt.opprett("UT1174", InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT, true))
                .ellers(Oppfylt.opprett("UT1177", InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE, true));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForTidligUttak() {

        Specification<FastsettePeriodeGrunnlag> erGraderingenUavklartNode =
                rs.hvisRegel(SjekkOmPeriodeUavklart.ID, "Er graderingen uavklart?")
                        .hvis(new SjekkOmPeriodeUavklart(), Manuellbehandling.opprett("UT1167", null, Manuellbehandlingårsak.PERIODE_UAVKLART, false, false))
                        .ellers(Oppfylt.opprett("UT1217", InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE, true));

        Specification<FastsettePeriodeGrunnlag> graderingIPeriodenNode =
                rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                        .hvis(new SjekkOmGradertPeriode(), erGraderingenUavklartNode)
                        .ellers(Oppfylt.opprett("UT1026", InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE, true));

        Specification<FastsettePeriodeGrunnlag> erSøkerFar =
                rs.hvisRegel(SjekkOmSøkerErMor.ID, ER_SØKER_FAR)
                        .hvis(new SjekkOmSøkerErMor(), delFlytForOverføringVedTidligUttak())
                        .ellers(graderingIPeriodenNode);

        Specification<FastsettePeriodeGrunnlag> noenDisponibleDagerNode =
                rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, "Er det disponibelt antall stønadsdager på fedrekvoten?")
                        .hvis(new SjekkOmTilgjengeligeDager(), erSøkerFar)
                        .ellers(Manuellbehandling.opprett("UT1022", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
                .hvis(new SjekkOmOmsorgHelePerioden(), noenDisponibleDagerNode)
                .ellers(Manuellbehandling.opprett("UT1025", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForOverføringVedVanligUttak() {
        return rs.hvisRegel(SjekkOmOverføringPgaInnleggelse.ID, ER_DET_SØKT_OM_OVERFØRING)
                .hvis(new SjekkOmOverføringPgaInnleggelse(), Oppfylt.opprett("UT1176", InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT, true))
                .ellers(Oppfylt.opprett("UT1175", InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE, true));
    }

    private Specification<FastsettePeriodeGrunnlag> delFlytForVanligUttak() {

        Specification<FastsettePeriodeGrunnlag> erGraderingenUavklartNode =
                rs.hvisRegel(SjekkOmPeriodeUavklart.ID, "Er graderingen uavklart?")
                        .hvis(new SjekkOmPeriodeUavklart(), Manuellbehandling.opprett("UT1168", null, Manuellbehandlingårsak.PERIODE_UAVKLART, false, false))
                        .ellers(Oppfylt.opprett("UT1218", InnvilgetÅrsak.GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE, true));

        Specification<FastsettePeriodeGrunnlag> graderingIPeriodenNode =
                rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                        .hvis(new SjekkOmGradertPeriode(), erGraderingenUavklartNode)
                        .ellers(Oppfylt.opprett("UT1031", InnvilgetÅrsak.KVOTE_ELLER_OVERFØRT_KVOTE, true));

        Specification<FastsettePeriodeGrunnlag> erSøkerFar =
                rs.hvisRegel(SjekkOmSøkerErMor.ID, ER_SØKER_FAR)
                        .hvis(new SjekkOmSøkerErMor(), delFlytForOverføringVedVanligUttak())
                        .ellers(graderingIPeriodenNode);

        Specification<FastsettePeriodeGrunnlag> noenDisponibleDagerNode =
                rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, "Er det disponibelt antall stønadsdager på fedrekvoten?")
                        .hvis(new SjekkOmTilgjengeligeDager(), erSøkerFar)
                        .ellers(Manuellbehandling.opprett("UT1178", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));

        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, SjekkOmOmsorgHelePerioden.BESKRIVELSE)
            .hvis(new SjekkOmOmsorgHelePerioden(), noenDisponibleDagerNode)
            .ellers(Manuellbehandling.opprett("UT1030", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG, true, false));
    }
}
