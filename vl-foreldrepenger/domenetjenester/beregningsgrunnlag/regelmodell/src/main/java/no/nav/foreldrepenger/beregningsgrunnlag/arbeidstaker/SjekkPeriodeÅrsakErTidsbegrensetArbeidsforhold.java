package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkPeriodeÅrsakErTidsbegrensetArbeidsforhold extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 27.4";
    static final String BESKRIVELSE = "Har bruker et tidsbegrenset arbeidsforhold?";

    SjekkPeriodeÅrsakErTidsbegrensetArbeidsforhold() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream().anyMatch(this::harAvsluttetArbeidsforhold) ?
            ja() : nei();
    }

    private boolean harAvsluttetArbeidsforhold(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getPeriodeÅrsaker().contains(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
    }
}
