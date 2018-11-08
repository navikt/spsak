package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmManglendeSøktPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsetter uttak av foreldrepenger før fødsel.
 * <p>
 * Utfall definisjoner:<br>
 * <p>
 * Utfall AVSLÅTT:<br>
 * - Far søker om perioden
 * - Perioden starter før perioden forbeholdt mor før fødsel.<br>
 * - Perioden starter etter termin/fødsel.<br>
 * <p>
 * Utfall INNVILGET:<br>
 * - Perioden dekker perioden forbeholdt mor før fødsel og det er mor som søker.
 */

@RuleDocumentation(value = ForeldrepengerFørFødselDelregel.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=252823617")
public class ForeldrepengerFørFødselDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK XX10";

    private Konfigurasjon konfigurasjon;

    public ForeldrepengerFørFødselDelregel() {
        // For dokumentasjonsgenerering
    }


    ForeldrepengerFørFødselDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return sjekkOmSøkerErMorNode(new Ruleset<>());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøkerErMorNode(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, "Er søker mor?")
                .hvis(new SjekkOmSøkerErMor(), sjekkOmPeriodenStarterForTidligNode(rs))
                .ellers(Manuellbehandling.opprett("UT1076", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterForTidligNode(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent.ID, "Starter perioden for tidlig?")
                .hvis(new SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent(konfigurasjon), Manuellbehandling.opprett("UT1070", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, false, false))
                .ellers(sjekkOmManglendeSøktPeriodeNode(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmManglendeSøktPeriodeNode(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmManglendeSøktPeriode.ID, "Er det manglende søkt periode?")
                .hvis(new SjekkOmManglendeSøktPeriode(), IkkeOppfylt.opprett("UT1073", IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE, true, false))
                .ellers(sjekkOmGradering(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGradering(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(new SjekkOmGradertPeriode(), Oppfylt.opprettMedAvslåttGradering("UT1072", InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL,
                        GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING, true))
                .ellers(Oppfylt.opprett("UT1071", InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL, true));
    }

}
