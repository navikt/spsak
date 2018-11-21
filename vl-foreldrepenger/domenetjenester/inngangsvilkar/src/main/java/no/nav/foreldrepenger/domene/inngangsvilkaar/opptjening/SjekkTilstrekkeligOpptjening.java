package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening;

import java.time.Period;

import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Sjekk om bruker har tilstrekkelig opptjening
 * </ul>
 * <p>
 * Følgende legges til grunn for vurdering av opptjeningsvilkåret:
 * <ul>
 * <li>Dersom det finnes minst 5 måneder og 26 kalenderdager med godkjent opptjening i løpet av opptjeningsperioden, er
 * vilkåret oppfylt</li>
 * <li>Dersom det finnes mindre enn 5 måneder og 26 kalenderdager med arbeid, næring, ytelser og likestilte aktiviteter
 * og det finnes godkjente mellomliggende perioder som gir opptjening, er vilkåret oppfylt</li>
 * <li>Dersom det finnes mindre enn 5 måneder og 26 kalenderdager med godkjent opptjening, så er vilkåret ikke
 * oppfylt.</li>
 * </ul>
 */
@RuleDocumentation(value = "FP_VK_23.2.1")
public class SjekkTilstrekkeligOpptjening extends LeafSpecification<OpptjeningsvilkårMellomregning> {
    public static final String ID = SjekkTilstrekkeligOpptjening.class.getSimpleName();

    public SjekkTilstrekkeligOpptjening() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(OpptjeningsvilkårMellomregning data) {
        Period opptjentPeriode = data.getBekreftetOpptjening().getOpptjentPeriode();

        if (data.sjekkErInnenforMinstePeriodeGodkjent(opptjentPeriode)) {
            data.setTotalOpptjening(data.getBekreftetOpptjening());
            return ja();
        } else {
            return nei();
        }

    }

}
