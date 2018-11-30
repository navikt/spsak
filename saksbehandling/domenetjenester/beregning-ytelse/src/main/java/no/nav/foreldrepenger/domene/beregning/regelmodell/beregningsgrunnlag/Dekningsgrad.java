package no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag;

public enum Dekningsgrad {
    DEKNINGSGRAD_80(0.8), DEKNINGSGRAD_100(1.0);

    private double verdi;

    Dekningsgrad(double verdi) {
        this.verdi = verdi;
    }

    public double getVerdi() { return verdi; }
}
