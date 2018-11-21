package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell;

import java.util.ArrayList;
import java.util.List;

public class RegelResultat {

    private final ResultatBeregningType beregningsresultat;
    private final List<RegelMerknad> merknader = new ArrayList<>();
    private String regelSporing;

    public RegelResultat(ResultatBeregningType beregningsresultat, String regelSporing) {
        this.beregningsresultat = beregningsresultat;
        this.regelSporing = regelSporing;
    }

    public RegelResultat medRegelMerknad(RegelMerknad regelMerknad) {
        merknader.add(regelMerknad);
        return this;
    }

    public List<RegelMerknad> getMerknader() {
        return merknader;
    }

    public ResultatBeregningType getBeregningsresultat() {
        return beregningsresultat;
    }

    public String getRegelSporing() {
        return regelSporing;
    }
}
