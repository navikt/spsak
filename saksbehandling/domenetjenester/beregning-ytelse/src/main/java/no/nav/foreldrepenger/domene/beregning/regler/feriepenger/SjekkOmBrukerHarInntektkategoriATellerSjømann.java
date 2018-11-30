package no.nav.foreldrepenger.domene.beregning.regler.feriepenger;

import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkOmBrukerHarInntektkategoriATellerSjømann extends LeafSpecification<BeregningsresultatFeriepengerRegelModell> {
    public static final String ID = "FP_BR 8.1";
    public static final String BESKRIVELSE = "Er brukers inntektskategori arbeidstaker eller sjømann?";


    SjekkOmBrukerHarInntektkategoriATellerSjømann() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsresultatFeriepengerRegelModell regelModell) {
        boolean erArbeidstakerEllerSjømann = regelModell.getInntektskategorier().stream()
            .anyMatch(Inntektskategori::erArbeidstakerEllerSjømann);
        return erArbeidstakerEllerSjømann ? ja() : nei();
    }
}
