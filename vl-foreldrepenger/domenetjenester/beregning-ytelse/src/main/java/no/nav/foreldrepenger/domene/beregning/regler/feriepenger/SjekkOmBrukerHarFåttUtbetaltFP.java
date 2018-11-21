package no.nav.foreldrepenger.domene.beregning.regler.feriepenger;

import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkOmBrukerHarFåttUtbetaltFP extends LeafSpecification<BeregningsresultatFeriepengerRegelModell> {
    public static final String ID = "FP_BR 8.2";
    public static final String BESKRIVELSE = "Har bruker fått utbetalt foreldrepenger i den totale stønadsperioden?";


    SjekkOmBrukerHarFåttUtbetaltFP() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsresultatFeriepengerRegelModell regelModell) {
        boolean utbetaltForeldrepenger = regelModell.getBeregningsresultatPerioder().stream()
            .flatMap(p -> p.getBeregningsresultatAndelList().stream())
            .anyMatch(andel -> andel.getDagsats() > 0);

        return utbetaltForeldrepenger ? ja() : nei();
    }
}
