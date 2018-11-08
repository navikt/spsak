package no.nav.foreldrepenger.beregning.regelmodell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BeregningsresultatFP {

    private List<BeregningsresultatPeriode> beregningsresultatPerioder = new ArrayList<>();
    private String regelInput;
    private String regelSporing;

    public String getRegelInput() {
        return regelInput;
    }

    public String getRegelSporing() {
        return regelSporing;
    }

    public List<BeregningsresultatPeriode> getBeregningsresultatPerioder() {
        return beregningsresultatPerioder;
    }

    public void addBeregningsresultatPeriode(BeregningsresultatPeriode brPeriode){
        Objects.requireNonNull(brPeriode, "beregningsresultatPeriode");
        if (!beregningsresultatPerioder.contains(brPeriode)) {
            beregningsresultatPerioder.add(brPeriode);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsresultatFP beregningsresultatFPMal;

        public Builder() {
            beregningsresultatFPMal = new BeregningsresultatFP();
        }

        public Builder medBeregningsresultatPerioder(List<BeregningsresultatPeriode> beregningsresultatPerioder){
            beregningsresultatFPMal.beregningsresultatPerioder.addAll(beregningsresultatPerioder);
            return this;
        }

        public Builder medRegelInput(String regelInput){
            beregningsresultatFPMal.regelInput = regelInput;
            return this;
        }

        public Builder medRegelSporing(String regelSporing){
            beregningsresultatFPMal.regelSporing = regelSporing;
            return this;
        }

        public BeregningsresultatFP build() {
            return beregningsresultatFPMal;
        }
    }
}
