package no.nav.foreldrepenger.beregning.regelmodell;

public class BeregningsresultatRegelmodellMellomregning {
    private final BeregningsresultatRegelmodell input;
    private final BeregningsresultatFP output;

    public BeregningsresultatRegelmodellMellomregning(BeregningsresultatRegelmodell input, BeregningsresultatFP output) {
        this.input = input;
        this.output = output;
    }

    public BeregningsresultatRegelmodell getInput() {
        return input;
    }

    public BeregningsresultatFP getOutput() {
        return output;
    }
}
