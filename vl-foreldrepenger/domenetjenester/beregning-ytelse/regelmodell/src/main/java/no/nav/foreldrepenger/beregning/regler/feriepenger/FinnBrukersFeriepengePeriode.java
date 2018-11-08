package no.nav.foreldrepenger.beregning.regler.feriepenger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatPeriode;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Dekningsgrad;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
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
        List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder = regelModell.getAnnenPartsBeregningsresultatPerioder();
        boolean erForelder1 = regelModell.erForelder1();
        LocalDate feriepengePeriodeFom = finnFørsteUttaksdag(beregningsresultatPerioder, annenPartsBeregningsresultatPerioder);
        LocalDate feriepengePeriodeTom = finnFeriepengerPeriodeTom(regelModell, feriepengePeriodeFom, erForelder1);

        BeregningsresultatFeriepengerRegelModell.builder(regelModell)
            .medFeriepengerPeriode(feriepengePeriodeFom, feriepengePeriodeTom);

        //Regelsporing
        Map<String, Object> resultater = new LinkedHashMap<>();
        resultater.put("FeriepengePeriode.fom", feriepengePeriodeFom);
        resultater.put("FeriepengePeriode.tom", feriepengePeriodeTom);
        return beregnet(resultater);
    }

    private LocalDate finnFeriepengerPeriodeTom(BeregningsresultatFeriepengerRegelModell regelModell, LocalDate feriepengePeriodeFom, boolean erForelder1) {
        List<BeregningsresultatPeriode> beregningsresultatPerioder = regelModell.getBeregningsresultatPerioder();
        List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder = regelModell.getAnnenPartsBeregningsresultatPerioder();
        Dekningsgrad dekningsgrad = regelModell.getDekningsgrad();
        int maksAntallDager = dekningsgrad == Dekningsgrad.DEKNINGSGRAD_100 ? 60 : 75;
        boolean annenpartRettPåFeriepenger = regelModell.getInntektskategorierAnnenPart().stream().anyMatch(Inntektskategori::erArbeidstakerEllerSjømann);
        LocalDate sisteUttaksdag = finnSisteUttaksdag(beregningsresultatPerioder, annenPartsBeregningsresultatPerioder);
        int antallDager = 0;

        for (LocalDate dato = feriepengePeriodeFom; !dato.isAfter(sisteUttaksdag); dato = dato.plusDays(1)) {
            int antallDagerSomLeggesTilFeriepengeperioden = finnAntallDagerSomSkalLeggesTil(beregningsresultatPerioder, annenPartsBeregningsresultatPerioder, annenpartRettPåFeriepenger, dato);
            antallDager += antallDagerSomLeggesTilFeriepengeperioden;
            if (antallDager == maksAntallDager) {
                return dato;
            }
            if (antallDager > maksAntallDager) {
                return erForelder1 ? dato : dato.minusDays(1);
            }
        }
        return sisteUttaksdag;
    }

    private int finnAntallDagerSomSkalLeggesTil(List<BeregningsresultatPeriode> beregningsresultatPerioder, List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder, boolean annenpartRettPåFeriepenger, LocalDate dato) {
        if (erHelg(dato)) {
            return 0;
        }
        int brukerHarUttakDager = harUttak(beregningsresultatPerioder, dato) ? 1 : 0;
        int annenpartUttakOgRettDager = annenpartRettPåFeriepenger && harUttak(annenPartsBeregningsresultatPerioder, dato) ? 1 : 0;
        return brukerHarUttakDager + annenpartUttakOgRettDager;
    }

    private boolean harUttak(List<BeregningsresultatPeriode> beregningsresultatPerioder, LocalDate dato) {
        return beregningsresultatPerioder.stream().filter(p -> p.inneholder(dato))
            .flatMap(beregningsresultatPeriode -> beregningsresultatPeriode.getBeregningsresultatAndelList().stream())
            .anyMatch(andel -> andel.getDagsats() > 0);
    }

    private boolean erHelg(LocalDate dato) {
        return dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue();
    }

    private LocalDate finnFørsteUttaksdag(List<BeregningsresultatPeriode> beregningsresultatPerioder, List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder) {
        LocalDate førsteUttaksdagBruker = finnFørsteUttaksdag(beregningsresultatPerioder)
            .orElseThrow(() -> new IllegalStateException("Fant ingen perioder med utbetaling for bruker"));
        LocalDate førsteUttaksdagAnnenPart = finnFørsteUttaksdag(annenPartsBeregningsresultatPerioder)
            .orElse(Tid.TIDENES_ENDE);
        return førsteUttaksdagBruker.isBefore(førsteUttaksdagAnnenPart) ? førsteUttaksdagBruker : førsteUttaksdagAnnenPart;
    }

    private Optional<LocalDate> finnFørsteUttaksdag(List<BeregningsresultatPeriode> beregningsresultatPerioder) {
        return beregningsresultatPerioder.stream()
            .filter(periode -> periode.getBeregningsresultatAndelList().stream().anyMatch(andel -> andel.getDagsats() > 0))
            .map(BeregningsresultatPeriode::getFom)
            .min(Comparator.naturalOrder());
    }

    private LocalDate finnSisteUttaksdag(List<BeregningsresultatPeriode> beregningsresultatPerioder, List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder) {
        LocalDate sisteUttaksdagBruker = finnSisteUttaksdag(beregningsresultatPerioder)
            .orElseThrow(() -> new IllegalStateException("Fant ingen perioder med utbetaling for bruker"));
        LocalDate sisteUttaksdagAnnenPart = finnSisteUttaksdag(annenPartsBeregningsresultatPerioder)
            .orElse(Tid.TIDENES_BEGYNNELSE);
        return sisteUttaksdagBruker.isAfter(sisteUttaksdagAnnenPart) ? sisteUttaksdagBruker : sisteUttaksdagAnnenPart;
    }

    private Optional<LocalDate> finnSisteUttaksdag(List<BeregningsresultatPeriode> beregningsresultatPerioder) {
        return beregningsresultatPerioder.stream()
            .filter(periode -> periode.getBeregningsresultatAndelList().stream().anyMatch(andel -> andel.getDagsats() > 0))
            .map(BeregningsresultatPeriode::getTom)
            .max(Comparator.naturalOrder());
    }
}
