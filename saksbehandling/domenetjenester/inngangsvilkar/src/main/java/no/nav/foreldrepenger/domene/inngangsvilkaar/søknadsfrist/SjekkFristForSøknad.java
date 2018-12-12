package no.nav.foreldrepenger.domene.inngangsvilkaar.søknadsfrist;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.EnumSet;

import no.nav.foreldrepenger.behandling.impl.PeriodeCompareUtil;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.SoeknadsfristvilkarGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.doc.RuleOutcomeDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Vurderer frist for Søknad.
 * <p>
 * Sjekkes som frist for mottak av søknad målt mot skjæringtidspunkt.
 * Eksempelvis kan frist være skjæringstidspunkt + 6 måneder for elektroniske søknader. For papirsøknader 2 ekstra
 * virkedager.
 */
@RuleDocumentation(value = SjekkFristForSøknad.ID, outcomes = {
    @RuleOutcomeDocumentation(code = SjekkFristForSøknad.ÅRSAKKODE_5007, result = Resultat.IKKE_VURDERT, description = "Søknadsdato har passert frist. Output variabel: '"
        + SjekkFristForSøknad.DAGER_FOR_SENT_PROPERTY + "'")
})
public class SjekkFristForSøknad extends LeafSpecification<SoeknadsfristvilkarGrunnlag> {
    static final String ÅRSAKKODE_5007 = "5007";

    static final String DAGER_FOR_SENT_PROPERTY = "antallDagerSoeknadLevertForSent";
    static final String ID = "FP_VK_3.2";
    private static final RuleReasonRefImpl IKKE_OPPFYLT_ETTER_FRIST = new RuleReasonRefImpl(ÅRSAKKODE_5007,
        "Søknadsdato {0} er {1} dager etter frist ({2}) fra skjæringstidspunkt {3}");
    private static final EnumSet<DayOfWeek> WEEKEND = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private final Period fristFørSøknad;

    private final int utvidAntallVirkedager;

    SjekkFristForSøknad(Period fristFørSøknad, int utvidAntallVirkedager) {
        super(ID);
        this.fristFørSøknad = fristFørSøknad;
        this.utvidAntallVirkedager = utvidAntallVirkedager;
    }

    @Override
    public Evaluation evaluate(SoeknadsfristvilkarGrunnlag t) {
        LocalDate skjæringstidspunktDato = t.getFørsteSykedagISøknaden();
        LocalDate søknadsDato = t.getSoeknadMottatDato();

        if (skjæringstidspunktDato == null) {
            throw new IllegalArgumentException("Mangler skjæringstidspunktDato i :" + t);
        }
        if (søknadsDato == null) {
            throw new IllegalArgumentException("Mangler søknadsDato i :" + t);
        }
        søknadsDato = søknadsDato.withDayOfMonth(1);
        skjæringstidspunktDato = skjæringstidspunktDato.withDayOfMonth(1);

        Period between = Period.between(skjæringstidspunktDato, søknadsDato);

        if (PeriodeCompareUtil.størreEnn(fristFørSøknad, between)) {
            return ja();
        } else {
            Period distanse = between.minus(fristFørSøknad);
            int dager = PeriodeCompareUtil.tilDager(distanse);
            SingleEvaluation kanIkkeVurdere = kanIkkeVurdere(IKKE_OPPFYLT_ETTER_FRIST, søknadsDato, dager, fristFørSøknad,
                skjæringstidspunktDato);
            kanIkkeVurdere.setEvaluationProperty(DAGER_FOR_SENT_PROPERTY, dager);
            return kanIkkeVurdere;
        }

    }

    @Override
    public String beskrivelse() {
        return "Frist: (skjæringstidspunkt) + (" + fristFørSøknad
            + (utvidAntallVirkedager > 0 ? " + " + utvidAntallVirkedager + " virkedager" : "") + ")";
    }
}
