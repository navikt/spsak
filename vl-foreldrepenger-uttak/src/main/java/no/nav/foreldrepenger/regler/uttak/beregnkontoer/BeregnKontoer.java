package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.util.Objects;

import no.nav.foreldrepenger.regler.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmBareFarHarRett;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmBareMorHarRett;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmBådeMorOgFarHarRett;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmDekningsgradEr100;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmFarHarAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmFødsel;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmMerEnnEttBarn;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmMorHarAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.betingelser.SjekkOmToBarn;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Denne implementerer regeltjenesten som beregner antall stønadsdager for foreldrepenger.
 */
@RuleDocumentation(value = BeregnKontoer.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=174837789")
public class BeregnKontoer implements RuleService<BeregnKontoerGrunnlag> {

    public static final String ID = "FP_VK 17";
    private static final String SJEKK_OM_MER_ENN_ETT_BARN = "Sjekk om det er mer enn ett barn?";
    private static final String SJEKK_OM_DET_ER_FØDSEL = "Sjekk om det er fødsel?";
    private static final String SJEKK_OM_100_PROSENT_DEKNINGSGRAD = "Sjekk om det er 100% dekningsgrad?";
    private static final String SJEKK_OM_TO_BARN = "Sjekk om det er to barn?";


    private Konfigurasjon konfigurasjon;

    public BeregnKontoer() {
        //For dokumentasjonsgenerering
    }

    public BeregnKontoer(Konfigurasjon konfigurasjon) {
        Objects.requireNonNull(konfigurasjon);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluer(BeregnKontoerGrunnlag beregnKontoerGrunnlag) {
        return getSpecification().evaluate(beregnKontoerGrunnlag);
    }

    @Override
    public Specification<BeregnKontoerGrunnlag> getSpecification() {
        Ruleset<BeregnKontoerGrunnlag> rs = new Ruleset<>();

        return rs.hvisRegel(SjekkOmMorHarAleneomsorg.ID, "Sjekk om mor har aleneomsorg?")
            .hvis(new SjekkOmMorHarAleneomsorg(), opprettKontoerForMorAleneomsorg(rs))
            .ellers(sjekkFarAleneomsorgNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkKunFarRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        // TODO (FL): Hva skal reasonCode være?
        RuleReasonRef ingenOpptjentRett = new RuleReasonRefImpl("", "Hverken far eller mor har opptjent rett til foreldrepenger.");
        return rs.hvisRegel(SjekkOmBareFarHarRett.ID, "Sjekk om kun far har rett til foreldrepenger?")
            .hvis(new SjekkOmBareFarHarRett(), opprettKontoerForBareFarHarRettTilForeldrepenger(rs))
            .ellers(new IkkeOppfylt<>(ingenOpptjentRett));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkKunMorRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBareMorHarRett.ID, "Sjekk om kun mor har rett til foreldrepenger?")
            .hvis(new SjekkOmBareMorHarRett(), opprettKontoerForMorAleneomsorg(rs))
            .ellers(sjekkKunFarRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBådeMorOgFarHarRett.ID, "Sjekk om begge har opptjent rett til foreldrepenger?")
            .hvis(new SjekkOmBådeMorOgFarHarRett(), opprettKontoerForBeggeHarRett(rs))
            .ellers(sjekkKunMorRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFarAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFarHarAleneomsorg.ID, "Sjekk om far har aleneomsorg?")
            .hvis(new SjekkOmFarHarAleneomsorg(), opprettKontoerForFarHarAleneomsorg(rs))
            .ellers(sjekkBeggeRettNode(rs));
    }
    /**
     * Delregel for å opprette kontoer dersom bare far har er rett til foreldrepenger.
     *
     * @param rs regelsett som skal brukes.
     * @return rot noden av denne delregelen.
     */
    private Specification<BeregnKontoerGrunnlag> opprettKontoerForBareFarHarRettTilForeldrepenger(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmMerEnnEttBarn.ID, SJEKK_OM_MER_ENN_ETT_BARN)
            .hvis(new SjekkOmMerEnnEttBarn(), sjekkOmToBarnKunFarRettNode(rs))
            .ellers(sjekk100ProsentEttBarnKunFarRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentEttBarnKunFarRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentToBarnKunFarRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentTreEllerFlereBarnKunFarRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkOmToBarnKunFarRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmToBarn.ID, SJEKK_OM_TO_BARN)
            .hvis(new SjekkOmToBarn(), sjekk100ProsentToBarnKunFarRettNode(rs))
            .ellers(sjekk100ProsentTreEllerFlereBarnKunFarRettNode(rs));
    }



    /**
     * Delregel for å opprette kontoer dersom far har aleneomsorg.
     *
     * @param rs regelsett som skal brukes.
     * @return rot noden av denne delregelen.
     */
    private Specification<BeregnKontoerGrunnlag> opprettKontoerForFarHarAleneomsorg(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmMerEnnEttBarn.ID, SJEKK_OM_MER_ENN_ETT_BARN)
            .hvis(new SjekkOmMerEnnEttBarn(), sjekkOmToBarnFarAleneomsorgNode(rs))
            .ellers(sjekk100ProsentEttBarnFarAleneomsorgNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentEttBarnFarAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentToBarnFarAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentTreEllerFlereBarnFarAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkOmToBarnFarAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmToBarn.ID, SJEKK_OM_TO_BARN)
            .hvis(new SjekkOmToBarn(), sjekk100ProsentToBarnFarAleneomsorgNode(rs))
            .ellers(sjekk100ProsentTreEllerFlereBarnFarAleneomsorgNode(rs));
    }

    /**
     * Delregel for å opprette kontoer dersom aleneomsorg eller bare mor har rett til foreldrepenger.
     *
     * @param rs regelsett som skal brukes.
     * @return rot noden av denne delregelen.
     */
    private Specification<BeregnKontoerGrunnlag> opprettKontoerForMorAleneomsorg(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmMerEnnEttBarn.ID, SJEKK_OM_MER_ENN_ETT_BARN)
            .hvis(new SjekkOmMerEnnEttBarn(), sjekkOmToBarnMorAleneomsorgNode(rs))
            .ellers(sjekk100ProsentEttBarnMorAleneomsorgNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselEttBarn100ProsentMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselEttBarn80ProsentMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselToBarn100ProsentMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselToBarn80ProsentMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselTreEllerFlereBarn100ProsentMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselTreEllerFlereBarn80ProsentMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentToBarnMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), sjekkFødselToBarn100ProsentMorAleneomsorgNode(rs))
            .ellers(sjekkFødselToBarn80ProsentMorAleneomsorgNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentTreEllerFlereBarnMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), sjekkFødselTreEllerFlereBarn100ProsentMorAleneomsorgNode(rs))
            .ellers(sjekkFødselTreEllerFlereBarn80ProsentMorAleneomsorgNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentEttBarnMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), sjekkFødselEttBarn100ProsentMorAleneomsorgNode(rs))
            .ellers(sjekkFødselEttBarn80ProsentMorAleneomsorgNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkOmToBarnMorAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmToBarn.ID, SJEKK_OM_TO_BARN)
            .hvis(new SjekkOmToBarn(), sjekk100ProsentToBarnMorAleneomsorgNode(rs))
            .ellers(sjekk100ProsentTreEllerFlereBarnMorAleneomsorgNode(rs));
    }

    /**
     * Delregel for å opprette kontoer dersom begge har rett til foreldrepenger.
     *
     * @param rs regelsett som skal brukes.
     * @return rot noden av denne delregelen.
     */
    private Specification<BeregnKontoerGrunnlag> opprettKontoerForBeggeHarRett(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmMerEnnEttBarn.ID, SJEKK_OM_MER_ENN_ETT_BARN)
            .hvis(new SjekkOmMerEnnEttBarn(), sjekkOmToBarnBeggeRettNode(rs))
            .ellers(sjekk100ProsentEttBarnBeggeRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselEttBarn100ProsentBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_100_PROSENT)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_100_PROSENT)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselEttBarn80ProsentBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_80_PROSENT)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_80_PROSENT)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselToBarn100ProsentBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselToBarn80ProsentBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselTreEllerFlereBarn100ProsentBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_100_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFødselTreEllerFlereBarn80ProsentBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
            .hvis(new SjekkOmFødsel(), new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80)))
            .ellers(new OpprettKontoer(konfigurasjon,
                new Kontokonfigurasjon(Stønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER),
                new Kontokonfigurasjon(Stønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER_80_PROSENT),
                new Kontokonfigurasjon(Stønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80)));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentTreEllerFlereBarnBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), sjekkFødselTreEllerFlereBarn100ProsentBeggeRettNode(rs))
            .ellers(sjekkFødselTreEllerFlereBarn80ProsentBeggeRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentToBarnBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), sjekkFødselToBarn100ProsentBeggeRettNode(rs))
            .ellers(sjekkFødselToBarn80ProsentBeggeRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekk100ProsentEttBarnBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDekningsgradEr100.ID, SJEKK_OM_100_PROSENT_DEKNINGSGRAD)
            .hvis(new SjekkOmDekningsgradEr100(), sjekkFødselEttBarn100ProsentBeggeRettNode(rs))
            .ellers(sjekkFødselEttBarn80ProsentBeggeRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkOmToBarnBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmToBarn.ID, SJEKK_OM_TO_BARN)
            .hvis(new SjekkOmToBarn(), sjekk100ProsentToBarnBeggeRettNode(rs))
            .ellers(sjekk100ProsentTreEllerFlereBarnBeggeRettNode(rs));
    }
}

