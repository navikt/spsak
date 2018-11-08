package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFastsettOpptjeningsperiode.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/OMR11+-+A1+Vurdering+for+opptjeningsvilkår+-+Funksjonell+beskrivelse")
public class RegelFastsettOpptjeningsperiode implements RuleService<OpptjeningsperiodeGrunnlag> {

    static final String ID = "FP_VK_21";
    static final String BESKRIVELSE = "Fastsett opptjeningsperiode";

    public RegelFastsettOpptjeningsperiode() {
    }

    @Override
    public Evaluation evaluer(OpptjeningsperiodeGrunnlag input, Object outputContainer) {
        Evaluation evaluation = getSpecification().evaluate(input);

        ((OpptjeningsPeriode) outputContainer).setOpptjeningsperiodeFom(input.getOpptjeningsperiodeFom());
        ((OpptjeningsPeriode) outputContainer).setOpptjeningsperiodeTom(input.getOpptjeningsperiodeTom());

        return evaluation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<OpptjeningsperiodeGrunnlag> getSpecification() {

        Ruleset<OpptjeningsperiodeGrunnlag> rs = new Ruleset<>();

        // FP_VK_21.9
        Specification<OpptjeningsperiodeGrunnlag> fastsettOpptjeningsperiode = new FastsettOpptjeningsperiode();

        // FP_VK_21.5 + FP_VK_21.9
        Specification<OpptjeningsperiodeGrunnlag> fastsettMorFødsel =
            rs.beregningsRegel("FP_VK 21.5", "Fastsett periode: Mor-Fødsel",
                new FastsettSkjæringsdatoMorFødsel(), fastsettOpptjeningsperiode);

        // FP_VK_21.6 + FP_VK_21.9
        Specification<OpptjeningsperiodeGrunnlag> fastsettAnnenFødsel =
            rs.beregningsRegel("FP_VK 21.6", "Fastsett periode: Annen-Fødsel",
                new FastsettSkjæringsdatoAnnenFødsel(), fastsettOpptjeningsperiode);

        // FP_VK_21.7 + FP_VK_21.9
        Specification<OpptjeningsperiodeGrunnlag> fastsettMorAdopsjon =
            rs.beregningsRegel("FP_VK 21.7", "Fastsett periode: Mor-Adopsjon/Omsorgsovertakelse",
                new FastsettSkjæringsdatoMorAdopsjon(), fastsettOpptjeningsperiode);

        // FP_VK_21.8 + FP_VK_21.9
        Specification<OpptjeningsperiodeGrunnlag> fastsettAnnenAdopsjon =
            rs.beregningsRegel("FP_VK 21.8", "Fastsett periode: Annen-Adopsjon/Omsorgsovertakelse",
                new FastsettSkjæringsdatoAnnenAdopsjon(), fastsettOpptjeningsperiode);

        // FP_VK_21.4
        Specification<OpptjeningsperiodeGrunnlag> adopsjonAnnenNode =
            rs.hvisRegel(SjekkAnnenAdopsjon.ID, SjekkAnnenAdopsjon.BESKRIVELSE).hvis(new SjekkAnnenAdopsjon(), fastsettAnnenAdopsjon).ellers(fastsettAnnenFødsel);

        // FP_VK_21.11
        Specification<OpptjeningsperiodeGrunnlag> omsorgNode =
            rs.hvisRegel(SjekkOmsorg.ID, SjekkOmsorg.BESKRIVELSE).hvis(new SjekkOmsorg(), fastsettMorAdopsjon).ellers(new IkkeGyldigUtgang());

        // FP_VK_21.3
        Specification<OpptjeningsperiodeGrunnlag> adopsjonNode =
            rs.hvisRegel(SjekkMorAdopsjon.ID, SjekkMorAdopsjon.BESKRIVELSE).hvis(new SjekkMorAdopsjon(), fastsettMorAdopsjon).ellers(adopsjonAnnenNode);

        // FP_VK_21.10
        Specification<OpptjeningsperiodeGrunnlag> adopsjonOmsorgNode =
            rs.hvisRegel(SjekkAdopsjon.ID, SjekkAdopsjon.BESKRIVELSE).hvis(new SjekkAdopsjon(), adopsjonNode).ellers(omsorgNode);

        // FP_VK_21.2
        Specification<OpptjeningsperiodeGrunnlag> fødselsNode =
            rs.hvisRegel(SjekkMorFødsel.ID, SjekkMorFødsel.BESKRIVELSE).hvis(new SjekkMorFødsel(), fastsettMorFødsel).ellers(fastsettAnnenFødsel);

        // FP_VK_21.1
        Specification<OpptjeningsperiodeGrunnlag> omhandlerFødselNode =
            rs.hvisRegel(SjekkFødsel.ID, SjekkFødsel.BESKRIVELSE).hvis(new SjekkFødsel(), fødselsNode).ellers(adopsjonOmsorgNode);

        // FP_VK_21: Start
        Specification<OpptjeningsperiodeGrunnlag> startFastsettOpptjeningsperiode =
            rs.regel(ID, BESKRIVELSE, omhandlerFødselNode);

        // Start fastsett opptjeningsperiode
        return startFastsettOpptjeningsperiode;
    }
}
