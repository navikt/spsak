package no.nav.foreldrepenger.domene.beregning.regler.feriepenger;

import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregnetFeriepenger;
import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelBeregnFeriepenger.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/27c+Beregn+feriepenger+PK-51965+OMR-49")
public class RegelBeregnFeriepenger implements RuleService<BeregningsresultatFeriepengerRegelModell> {

    public static final String ID = "";
    public static final String BESKRIVELSE = "RegelBeregnFeriepenger";

    @Override
    public Evaluation evaluer(BeregningsresultatFeriepengerRegelModell regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsresultatFeriepengerRegelModell> getSpecification() {
        Ruleset<BeregningsresultatFeriepengerRegelModell> rs = new Ruleset<>();

        // FP_BR 8.7 Beregn feriepenger (Ett kalenderår)
        Specification<BeregningsresultatFeriepengerRegelModell> beregnFeriepengerEttÅr =
            rs.beregningsRegel(BeregnFeriepengerEttÅr.ID, BeregnFeriepengerEttÅr.BESKRIVELSE, new BeregnFeriepengerEttÅr(), new BeregnetFeriepenger());

        // FP_BR 8.6 Beregn feriepenger (Flere kalenderår)
        Specification<BeregningsresultatFeriepengerRegelModell> beregnFeriepengerFlereÅr =
            rs.beregningsRegel(BeregnFeriepengerFlereÅr.ID, BeregnFeriepengerFlereÅr.BESKRIVELSE, new BeregnFeriepengerFlereÅr(), new BeregnetFeriepenger());

        // FP_BR 8.5 Går feriepengeperioden over flere kalenderår?
        Specification<BeregningsresultatFeriepengerRegelModell> sjekkOmFeriepengePeriodenGårOverFlereÅr =
            rs.beregningHvisRegel(new SjekkOmFeriepengePeriodenGårOverFlereÅr(), beregnFeriepengerFlereÅr, beregnFeriepengerEttÅr);

        //FP_BR 8.4 Har bruker uttak i feriepengeperiode?
        Specification<BeregningsresultatFeriepengerRegelModell> sjekkOmUttakIFeriepengePeriode =
            rs.beregningHvisRegel(new SjekkBrukerHarOmUttakIFeriepengePeriode(), sjekkOmFeriepengePeriodenGårOverFlereÅr, new BeregnetFeriepenger());

        //FP_BR 8.3 Finn brukers feriepengeperiode
        Specification<BeregningsresultatFeriepengerRegelModell> finnBrukersFeriepengePeriode =
            rs.beregningsRegel(FinnBrukersFeriepengePeriode.ID, FinnBrukersFeriepengePeriode.BESKRIVELSE, new FinnBrukersFeriepengePeriode(), sjekkOmUttakIFeriepengePeriode);

        // FP_BR 8.2 Har bruker fått utbetalt foreldrepenger i den totale stønadsperioden?
        Specification<BeregningsresultatFeriepengerRegelModell> sjekkOmBrukerHarFåttUtbetaltFP =
            rs.beregningHvisRegel(new SjekkOmBrukerHarFåttUtbetaltFP(), finnBrukersFeriepengePeriode, new BeregnetFeriepenger());

        // FP_BR 8.1 Er brukers inntektskategori arbeidstaker eller sjømann?
        Specification<BeregningsresultatFeriepengerRegelModell> sjekkInntektskatoriATellerSjømann =
            rs.beregningHvisRegel(new SjekkOmBrukerHarInntektkategoriATellerSjømann(), sjekkOmBrukerHarFåttUtbetaltFP, new BeregnetFeriepenger());

        return sjekkInntektskatoriATellerSjømann;
    }
}
