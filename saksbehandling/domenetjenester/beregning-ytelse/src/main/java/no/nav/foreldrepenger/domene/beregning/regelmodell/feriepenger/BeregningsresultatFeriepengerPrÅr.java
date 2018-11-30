package no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatAndel;

public class BeregningsresultatFeriepengerPrÅr {

    private LocalDate opptjeningÅr;
    private BigDecimal årsbeløp;

    @JsonBackReference
    private BeregningsresultatAndel beregningsresultatAndel;

    private BeregningsresultatFeriepengerPrÅr() {
    }

    public LocalDate getOpptjeningÅr() {
        return opptjeningÅr;
    }

    public BigDecimal getÅrsbeløp() {
        return årsbeløp;
    }

    public BeregningsresultatAndel getBeregningsresultatAndel() {
        return beregningsresultatAndel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsresultatFeriepengerPrÅr eksisterende) {
        return new Builder(eksisterende);
    }

    public static class Builder {

        private BeregningsresultatFeriepengerPrÅr kladd;

        public Builder() {
            kladd = new BeregningsresultatFeriepengerPrÅr();
        }

        public Builder(BeregningsresultatFeriepengerPrÅr kladd) {
            this.kladd = kladd;
        }

        public Builder medOpptjeningÅr(LocalDate opptjeningÅr) {
            kladd.opptjeningÅr = opptjeningÅr;
            return this;
        }

        public Builder medÅrsbeløp(BigDecimal årsbeløp) {
            kladd.årsbeløp = årsbeløp;
            return this;
        }

        public BeregningsresultatFeriepengerPrÅr build(BeregningsresultatAndel beregningsresultatAndel) {
            kladd.beregningsresultatAndel = beregningsresultatAndel;
            BeregningsresultatAndel.builder(beregningsresultatAndel).leggTilBeregningsresultatFeriepengerPrÅr(kladd);
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.opptjeningÅr, "opptjeningÅr");
            Objects.requireNonNull(kladd.årsbeløp, "årsbeløp");
            Objects.requireNonNull(kladd.beregningsresultatAndel, "beregningsresultatAndel");
        }
    }
}
