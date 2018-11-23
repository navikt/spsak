package no.nav.foreldrepenger.domene.beregning.regler.feriepenger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatPeriode;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.vedtak.konfig.Tid;

class FinnBrukersFeriepengePeriode extends LeafSpecification<BeregningsresultatFeriepengerRegelModell> {
    public static final String ID = "FP_BR 8.3";
    public static final String BESKRIVELSE = "Finner brukers feriepengeperiode.";


    FinnBrukersFeriepengePeriode() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsresultatFeriepengerRegelModell regelModell) {
        List<BeregningsresultatPeriode> beregningsresultatPerioder = regelModell.getBeregningsresultatPerioder();
        LocalDate feriepengePeriodeFom = finnFørsteUttaksdagTotalt(beregningsresultatPerioder);
        LocalDate feriepengePeriodeTom = finnFeriepengerPeriodeTom(regelModell, feriepengePeriodeFom);

        BeregningsresultatFeriepengerRegelModell.builder(regelModell)
            .medFeriepengerPeriode(feriepengePeriodeFom, feriepengePeriodeTom);

        //Regelsporing
        Map<String, Object> resultater = new LinkedHashMap<>();
        resultater.put("FeriepengePeriode.fom", feriepengePeriodeFom);
        resultater.put("FeriepengePeriode.tom", feriepengePeriodeTom);
        return beregnet(resultater);
    }

    private LocalDate finnFeriepengerPeriodeTom(BeregningsresultatFeriepengerRegelModell regelModell, LocalDate feriepengePeriodeFom) {
        List<BeregningsresultatPeriode> beregningsresultatPerioder = regelModell.getBeregningsresultatPerioder();
        Dekningsgrad dekningsgrad = regelModell.getDekningsgrad();
        int maksAntallDager = dekningsgrad == Dekningsgrad.DEKNINGSGRAD_100 ? 60 : 75;
        LocalDate sisteUttaksdag = finnSisteUttaksdagTotalt(beregningsresultatPerioder);
        int antallDager = 0;

        for (LocalDate dato = feriepengePeriodeFom; !dato.isAfter(sisteUttaksdag); dato = dato.plusDays(1)) {
            int antallDagerSomLeggesTilFeriepengeperioden = finnAntallDagerSomSkalLeggesTil(beregningsresultatPerioder, dato);
            antallDager += antallDagerSomLeggesTilFeriepengeperioden;
            if (antallDager == maksAntallDager) {
                return dato;
            }
            if (antallDager > maksAntallDager) {
                return dato;
            }
        }
        return sisteUttaksdag;
    }

    private int finnAntallDagerSomSkalLeggesTil(List<BeregningsresultatPeriode> beregningsresultatPerioder, LocalDate dato) {
        if (erHelg(dato)) {
            return 0;
        }
        int brukerHarUttakDager = harUttak(beregningsresultatPerioder, dato) ? 1 : 0;
        return brukerHarUttakDager;
    }

    private boolean harUttak(List<BeregningsresultatPeriode> beregningsresultatPerioder, LocalDate dato) {
        return beregningsresultatPerioder.stream().filter(p -> p.inneholder(dato))
            .flatMap(beregningsresultatPeriode -> beregningsresultatPeriode.getBeregningsresultatAndelList().stream())
            .anyMatch(andel -> andel.getDagsats() > 0);
    }

    private boolean erHelg(LocalDate dato) {
        return dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue();
    }

    private LocalDate finnFørsteUttaksdagTotalt(List<BeregningsresultatPeriode> beregningsresultatPerioder) {
        LocalDate førsteUttaksdagBruker = finnFørsteUttaksdag(beregningsresultatPerioder)
            .orElseThrow(() -> new IllegalStateException("Fant ingen perioder med utbetaling for bruker"));
        return førsteUttaksdagBruker.isBefore(Tid.TIDENES_ENDE) ? førsteUttaksdagBruker : Tid.TIDENES_ENDE;
    }

    private Optional<LocalDate> finnFørsteUttaksdag(List<BeregningsresultatPeriode> beregningsresultatPerioder) {
        return beregningsresultatPerioder.stream()
            .filter(periode -> periode.getBeregningsresultatAndelList().stream().anyMatch(andel -> andel.getDagsats() > 0))
            .map(BeregningsresultatPeriode::getFom)
            .min(Comparator.naturalOrder());
    }

    private LocalDate finnSisteUttaksdagTotalt(List<BeregningsresultatPeriode> beregningsresultatPerioder) {
        LocalDate sisteUttaksdagBruker = finnSisteUttaksdag(beregningsresultatPerioder)
            .orElseThrow(() -> new IllegalStateException("Fant ingen perioder med utbetaling for bruker"));
        return sisteUttaksdagBruker.isAfter(Tid.TIDENES_BEGYNNELSE) ? sisteUttaksdagBruker : Tid.TIDENES_BEGYNNELSE;
    }

    private Optional<LocalDate> finnSisteUttaksdag(List<BeregningsresultatPeriode> beregningsresultatPerioder) {
        return beregningsresultatPerioder.stream()
            .filter(periode -> periode.getBeregningsresultatAndelList().stream().anyMatch(andel -> andel.getDagsats() > 0))
            .map(BeregningsresultatPeriode::getTom)
            .max(Comparator.naturalOrder());
    }
}
