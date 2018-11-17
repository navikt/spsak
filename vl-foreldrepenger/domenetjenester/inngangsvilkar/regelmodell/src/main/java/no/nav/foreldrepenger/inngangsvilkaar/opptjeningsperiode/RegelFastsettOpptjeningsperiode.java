package no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Fastsett opptjeningsperiode i forkant av sykemelding
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
        
        Specification<OpptjeningsperiodeGrunnlag> fastsettSkjæringstidspunktSykepengerDummy =
                rs.beregningsRegel("FP_VK xx.todo", "Fastsett periode: Skjæringstidspunkt fra første uttaksdag",
                    new FastsettSkjæringsdato(), fastsettOpptjeningsperiode);

        return fastsettSkjæringstidspunktSykepengerDummy;
    }
}
