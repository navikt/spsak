package no.nav.foreldrepenger.beregning.regler.feriepenger;

import no.nav.foreldrepenger.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.fpsak.tidsserie.LocalDateInterval;

class SjekkOmFeriepengePeriodenGårOverFlereÅr extends LeafSpecification<BeregningsresultatFeriepengerRegelModell> {
    public static final String ID = "FP_BR 8.5";
    public static final String BESKRIVELSE = "Går feriepengeperioden over flere kalenderår?";


    SjekkOmFeriepengePeriodenGårOverFlereÅr() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsresultatFeriepengerRegelModell regelModell) {
        LocalDateInterval feriepengerPeriode = regelModell.getFeriepengerPeriode();
        return feriepengerPeriode.getFomDato().getYear() < feriepengerPeriode.getTomDato().getYear() ? ja() : nei();
    }
}
