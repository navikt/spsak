package no.nav.foreldrepenger.domene.beregning.regler.feriepenger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.spsak.tidsserie.LocalDateInterval;

class BeregnFeriepengerFlereÅr extends LeafSpecification<BeregningsresultatFeriepengerRegelModell> {
    public static final String ID = "FP_BR 8.6";
    public static final String BESKRIVELSE = "Beregn feriepenger for periode som går over flere kalenderår.";


    BeregnFeriepengerFlereÅr() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsresultatFeriepengerRegelModell regelModell) {
        Map<String, Object> resultater = new LinkedHashMap<>();

        LocalDateInterval feriepengerPeriode = regelModell.getFeriepengerPeriode();
        List<LocalDateInterval> feriepengerPerioderPrÅr = periodiserLocalDateIntervalPrÅr(feriepengerPeriode);

        feriepengerPerioderPrÅr.forEach(feriepengerPeriodePrÅr ->
            BeregnFeriepengerForPeriode.beregn(resultater, regelModell.getBeregningsresultatPerioder(), feriepengerPeriodePrÅr)
        );

        return beregnet(resultater);
    }

    private static List<LocalDateInterval> periodiserLocalDateIntervalPrÅr(LocalDateInterval feriepengerPeriode) {
        LocalDate fom = feriepengerPeriode.getFomDato();
        LocalDate tom = feriepengerPeriode.getTomDato();
        List<LocalDateInterval> perioder = new ArrayList<>();
        while (fom.getYear() != tom.getYear()) {
            LocalDate sisteDagIÅr = fom.withMonth(12).withDayOfMonth(31);
            LocalDateInterval dateInterval = new LocalDateInterval(fom, sisteDagIÅr);
            perioder.add(dateInterval);
            fom = sisteDagIÅr.plusDays(1);
        }
        LocalDateInterval dateInterval = new LocalDateInterval(fom, tom);
        perioder.add(dateInterval);
        return perioder;
    }
}
