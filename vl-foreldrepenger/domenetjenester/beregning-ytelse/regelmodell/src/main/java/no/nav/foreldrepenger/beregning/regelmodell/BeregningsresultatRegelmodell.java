package no.nav.foreldrepenger.beregning.regelmodell;

import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregningsresultatRegelmodell {
    private Beregningsgrunnlag beregningsgrunnlag;
    private UttakResultat uttakResultat;

    public BeregningsresultatRegelmodell(Beregningsgrunnlag beregningsgrunnlag, UttakResultat uttakResultat) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.uttakResultat = uttakResultat;
    }

    public Beregningsgrunnlag getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public UttakResultat getUttakResultat() {
        return uttakResultat;
    }
}
