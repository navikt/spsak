package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmArbeidIPerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmErGradertFørEndringssøknadMottattdato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmFulltArbeidIPerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmHvisOverlapperSåSamtykkeMellomParter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmManglendeSøktPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErFedrekvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErFellesperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErForeldrepengerFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErMødrekvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErUtsettelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeUavklartUtenomNoenTyper;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErEtterMaksgrenseForUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenErFørGyldigDato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmRevurderingAvBerørtSak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSamtidigUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadGjelderFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadsperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktGradering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktGraderingHundreProsentEllerMer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøktOmOverføringAvKvote;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttaketStarterFørLovligUttakFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureToggles;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Regeltjeneste som fastsetter uttaksperioder som er søkt om for foreldrepenger.
 */
@RuleDocumentation(value = FastsettePeriodeRegel.ID, specificationReference = "TODO")
public class FastsettePeriodeRegel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 14";
    private static final String GJELDER_FPFF_PERIODE_FØDSEL = "Gjelder foreldrepenger før fødsel periode fødsel?";
    private static final String ER_PERIODEN_FPFF = "Er det søkt om uttak av foreldrepenger før fødsel?";

    private Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();
    private Konfigurasjon konfigurasjon;
    private FeatureToggles featureToggles = new FeatureToggles() {};

    public FastsettePeriodeRegel() {
        // For dokumentasjonsgenerering
    }

    public FastsettePeriodeRegel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    public FastsettePeriodeRegel(Konfigurasjon konfigurasjon, FeatureToggles featureToggles) {
        this(konfigurasjon);
        this.featureToggles = featureToggles;
    }

    @Override
    public Evaluation evaluer(FastsettePeriodeGrunnlag grunnlag) {
        return getSpecification().evaluate(grunnlag);
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmSøknadsperiode.ID, "Er perioden en søknadsperiode?")
                .hvis(new SjekkOmSøknadsperiode(), sjekkOmPeriodeFørGyldigDato())
                .ellers(sjekkOmSamtykke());
    }

    public Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeFørGyldigDato() {
        return rs.hvisRegel(SjekkOmPeriodenErFørGyldigDato.ID, "Er uttaksperiode før \"gyldig dato\"?")
            .hvis(new SjekkOmPeriodenErFørGyldigDato(), sjekkOmManglendePeriode())
            .ellers(sjekkPeriodeInnenforMaksgrense());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse() {
        return rs.hvisRegel(SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse.ID, "Sammenfaller uttaksperioden med en periode hos den andre parten som er en innvilget utsettelse?")
                .hvis(new SjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse(), IkkeOppfylt.opprett("UT1166", IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE, false, false))
                .ellers(sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmAnnenPartsPeriodeHarUtbetalingsgrad() {
        return rs.hvisRegel(SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad.ID, "Sammenfaller uttaksperioden med en periode hos den andre parten som har utbetaling > 0?")
                .hvis(new SjekkOmAnnenPartsPeriodeHarUtbetalingsgrad(), sjekkOmSamtidigUttak())
                .ellers(sjekkOmPeriodeErUtsettelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSamtidigUttak() {
        return rs.hvisRegel(SjekkOmSamtidigUttak.ID, "Har en av foreldrene huket av for samtidig uttak?")
                .hvis(new SjekkOmSamtidigUttak(), Manuellbehandling.opprett("UT1164", null, Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK, false, false))
                .ellers(IkkeOppfylt.opprett("UT1162", IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmManglendePeriode() {
        return rs.hvisRegel(SjekkOmManglendeSøktPeriode.ID, "Er det \"Manglende søkt periode\"?")
                .hvis(new SjekkOmManglendeSøktPeriode(), Manuellbehandling.opprett("UT1084", IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, Manuellbehandlingårsak.MANGLENDE_SØKT_PERIODE, true, false))
                .ellers(sjekkOmPeriodeErForTidlig());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErForTidlig() {
        return rs.hvisRegel(SjekkOmUttaketStarterFørLovligUttakFørFødsel.ID, "Gjelder det periode tidligere enn 12 uker før fødsel/termin?")
                .hvis(new SjekkOmUttaketStarterFørLovligUttakFørFødsel(konfigurasjon), IkkeOppfylt.opprett("UT1080", IkkeOppfyltÅrsak.SØKNADSFRIST, false, false))
                .ellers(sjekkOmNoenDagerIgjen());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmNoenDagerIgjen() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDager.ID, SjekkOmTilgjengeligeDager.BESKRIVELSE)
                .hvis(new SjekkOmTilgjengeligeDager(), Manuellbehandling.opprett("UT1082", IkkeOppfyltÅrsak.SØKNADSFRIST, Manuellbehandlingårsak.SØKNADSFRIST, false, false))
                .ellers(Manuellbehandling.opprett("UT1081", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkPeriodeInnenforMaksgrense() {
        return rs.hvisRegel(SjekkOmPeriodenErEtterMaksgrenseForUttak.ID, "Er hele perioden innenfor maksimalgrense for foreldrepenger?")
                .hvis(new SjekkOmPeriodenErEtterMaksgrenseForUttak(konfigurasjon), IkkeOppfylt.opprett("UT1085", IkkeOppfyltÅrsak.UTTAK_ETTER_3_ÅRSGRENSE, false, false))
                .ellers(sjekkOmSamtykke());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSamtykke() {
        return rs.hvisRegel(SjekkOmHvisOverlapperSåSamtykkeMellomParter.ID, "Er det samtykke og overlappende periode?")
            .hvis(new SjekkOmHvisOverlapperSåSamtykkeMellomParter(), sjekkOmRevurderingAvBerørtSak())
            .ellers(Manuellbehandling.opprett("UT1063", IkkeOppfyltÅrsak.IKKE_SAMTYKKE, Manuellbehandlingårsak.IKKE_SAMTYKKE, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmRevurderingAvBerørtSak() {
        return rs.hvisRegel(SjekkOmRevurderingAvBerørtSak.ID, "Er behandlingen en revurdering av berørt sak?")
            .hvis(new SjekkOmRevurderingAvBerørtSak(), sjekkOmAnnenPartsPeriodeErInnvilgetUtsettelse())
            .ellers(sjekkOmGradertEtterEndringssøknadMottattdato());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGradertEtterEndringssøknadMottattdato() {
        return rs.hvisRegel(SjekkOmErGradertFørEndringssøknadMottattdato.ID, "Er perioden gradert etter mottattdato?")
                .hvis(new SjekkOmErGradertFørEndringssøknadMottattdato(), Manuellbehandling.opprett("UT1165", null, Manuellbehandlingårsak.SØKNADSFRIST, true, false))
                .ellers(sjekkOmPeriodeErUtsettelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErUtsettelse() {
        Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelseFørFamiliehendelse = rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Er utsettelse før familiehendelse?")
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), Manuellbehandling.opprett("UT1151", IkkeOppfyltÅrsak.UTSETTELSE_FØR_TERMIN_FØDSEL, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false))
            .ellers(new UtsettelseDelregel(konfigurasjon).getSpecification());

        return rs.hvisRegel(SjekkOmPeriodeErUtsettelse.ID, "Er det utsettelse?")
                .hvis(new SjekkOmPeriodeErUtsettelse(), sjekkOmUtsettelseFørFamiliehendelse)
                .ellers(sjekkOmManglendeSøktPeriode());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmForeldrepengerFørFødsel() {
        return rs.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, ER_PERIODEN_FPFF)
            .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(), sjekkOmFPFFGjelderFødsel())
            .ellers(Manuellbehandling.opprett("UT1087", IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, Manuellbehandlingårsak.MANGLENDE_SØKT_PERIODE, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmManglendeSøktPeriode() {
        Specification<FastsettePeriodeGrunnlag>  sjekkOmPeriodeUavklart =
            rs.hvisRegel(SjekkOmPeriodeUavklartUtenomNoenTyper.ID, "Er uttaksperioden uavklart?")
                .hvis(new SjekkOmPeriodeUavklartUtenomNoenTyper(konfigurasjon), Manuellbehandling.opprett("UT1148", null, Manuellbehandlingårsak.PERIODE_UAVKLART, false, false))
                .ellers(sjekkOmSøktGradering());

        Specification<FastsettePeriodeGrunnlag> sjekkOmSøktOverføringAvKvoteNode =
            rs.hvisRegel(SjekkOmSøktOmOverføringAvKvote.ID, "Er det søkt om overføring av kvote")
            .hvis(new SjekkOmSøktOmOverføringAvKvote(), Manuellbehandling.opprett("UT1161", IkkeOppfyltÅrsak.AKTIVITETSKRAVET, Manuellbehandlingårsak.VURDER_OVERFØRING, false, false))
            .ellers(IkkeOppfylt.opprett("UT1160", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false));

        Specification<FastsettePeriodeGrunnlag> sjekkOmTomForAlleSineKontoerNode =
            rs.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, "Er søker tom for alle sine kontoer?")
            .hvis(new SjekkOmTomForAlleSineKontoer(), sjekkOmSøktOverføringAvKvoteNode)
            .ellers(sjekkOmPeriodeUavklart);

        Specification<FastsettePeriodeGrunnlag> sjekkOmForeldrepengerFørFødselNode =
                rs.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, ER_PERIODEN_FPFF)
                        .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(), sjekkOmPeriodeUavklart)
                        .ellers(sjekkOmTomForAlleSineKontoerNode);

        return rs.hvisRegel(SjekkOmManglendeSøktPeriode.ID, "Er det \"Manglende søkt periode\"?")
            .hvis(new SjekkOmManglendeSøktPeriode(), sjekkOmForeldrepengerFørFødsel())
            .ellers(sjekkOmForeldrepengerFørFødselNode);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøktGradering() {
        return rs.hvisRegel(SjekkOmSøktGradering.ID, "Er det søkt om gradering?")
                .hvis(new SjekkOmSøktGradering(), sjekkOmSøktGradering100ProsentEllerMer())
                .ellers(sjekkOmPeriodeMedArbeid());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøktGradering100ProsentEllerMer() {
        return rs.hvisRegel(SjekkOmSøktGraderingHundreProsentEllerMer.ID, "Er søkt arbeid 100 prosent eller mer i perioden?")
                .hvis(new SjekkOmSøktGraderingHundreProsentEllerMer(), Manuellbehandling.opprett("UT1180", IkkeOppfyltÅrsak.ARBEID_HUNDRE_PROSENT_ELLER_MER, Manuellbehandlingårsak.AVKLAR_ARBEID, false, false))
                .ellers(sjekkOmPeriodeMedFulltArbeid());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeMedArbeid() {
        return rs.hvisRegel(SjekkOmArbeidIPerioden.ID, "Er arbeid i perioden større enn 0 og mindre enn 100 prosent?")
                .hvis(new SjekkOmArbeidIPerioden(), Manuellbehandling.opprett("UT088", IkkeOppfyltÅrsak.ARBEID_MER_ENN_NULL_PROSENT, Manuellbehandlingårsak.AVKLAR_ARBEID, false, false))
                .ellers(sjekkOmPeriodeErMødrekvote());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeMedFulltArbeid() {
        return rs.hvisRegel(SjekkOmFulltArbeidIPerioden.ID, "Er arbeid i perioden 100 prosent eller mer?")
                .hvis(new SjekkOmFulltArbeidIPerioden(), Manuellbehandling.opprett("UT095", IkkeOppfyltÅrsak.ARBEID_HUNDRE_PROSENT_ELLER_MER, Manuellbehandlingårsak.AVKLAR_ARBEID, false, false))
                .ellers(sjekkOmPeriodeErMødrekvote());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErMødrekvote() {
        return rs.hvisRegel(SjekkOmPeriodeErMødrekvote.ID, "Er det søkt om uttak av mødrekvote?")
                .hvis(new SjekkOmPeriodeErMødrekvote(), sjekkOmMødrekvoteGjelderFødsel())
                .ellers(sjekkOmFedrekvote());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMødrekvoteGjelderFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderFødsel.ID, "Gjelder søknaden fødsel?")
                .hvis(new SjekkOmSøknadGjelderFødsel(), new MødrekvoteDelregel(konfigurasjon).getSpecification())
                .ellers(Manuellbehandling.opprett("UT1089", null, Manuellbehandlingårsak.ADOPSJON_IKKE_IMPLEMENTERT, false, false)); //TODO midlertidig inntil adopsjon er implemmentert
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFedrekvote() {
        return rs.hvisRegel(SjekkOmPeriodeErFedrekvote.ID, "Er det søkt om uttak av fedrekvote?")
                .hvis(new SjekkOmPeriodeErFedrekvote(), sjekkOmFedrekvotePeriodeGjelderFødsel())
                .ellers(sjekkOmFellesperiode());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFedrekvotePeriodeGjelderFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderFødsel.ID, "Gjelder fedrekvote periode fødsel?")
                        .hvis(new SjekkOmSøknadGjelderFødsel(), new FedrekvoteDelregel(konfigurasjon).getSpecification())
                        .ellers(Manuellbehandling.opprett("UT1090", null, Manuellbehandlingårsak.ADOPSJON_IKKE_IMPLEMENTERT, false, false)); //TODO midlertidig inntil adopsjon er implemmentert
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFellesperiode() {
        return rs.hvisRegel(SjekkOmPeriodeErFellesperiode.ID, "Er det søkt om uttak av fellesperiode?")
                        .hvis(new SjekkOmPeriodeErFellesperiode(), sjekkOmFellesperiodeGjelderFødsel())
                        .ellers(sjekkOmPeriodeErForeldrepengerFørFødsel());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFellesperiodeGjelderFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderFødsel.ID, GJELDER_FPFF_PERIODE_FØDSEL)
                .hvis(new SjekkOmSøknadGjelderFødsel(), new FellesperiodeDelregel(konfigurasjon).getSpecification())
                .ellers(Manuellbehandling.opprett("UT1091", null, Manuellbehandlingårsak.ADOPSJON_IKKE_IMPLEMENTERT, false, false)); //TODO midlertidig inntil adopsjon er implemmentert
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErForeldrepengerFørFødsel() {
        return rs.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, ER_PERIODEN_FPFF)
                        .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(), sjekkOmFPFFGjelderFødsel())
                        .ellers(sjekkOmForeldrepengerGjelderFødsel());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFPFFGjelderFødsel() {
        return rs.hvisRegel(SjekkOmSøknadGjelderFødsel.ID, GJELDER_FPFF_PERIODE_FØDSEL)
                .hvis(new SjekkOmSøknadGjelderFødsel(), new ForeldrepengerFørFødselDelregel(konfigurasjon).getSpecification())
                .ellers(Manuellbehandling.opprett("UT1092", null, Manuellbehandlingårsak.ADOPSJON_IKKE_IMPLEMENTERT, false, false)); //TODO midlertidig inntil adopsjon er implemmentert
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmForeldrepengerGjelderFødsel() {
        final Specification<FastsettePeriodeGrunnlag> ja;
        if (featureToggles.foreldrepengerFødsel()) {
            ja = new ForeldrepengerFødselDelregel(konfigurasjon).getSpecification();
        } else {
            ja = Manuellbehandling.opprett("UT1093", null, Manuellbehandlingårsak.FORELDREPENGER_IKKE_IMPLEMENTERT, false, false);
        }
        return rs.hvisRegel(SjekkOmSøknadGjelderFødsel.ID, GJELDER_FPFF_PERIODE_FØDSEL)
                .hvis(new SjekkOmSøknadGjelderFødsel(), ja)
                .ellers(Manuellbehandling.opprett("UT1094", null, Manuellbehandlingårsak.ADOPSJON_IKKE_IMPLEMENTERT, false, false)); //TODO midlertidig inntil adopsjon er implemmentert
    }
}
