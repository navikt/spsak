package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;

public class SammenligningsGrunnlag {
    private Periode sammenligningsperiode;
    private BigDecimal rapportertPrÅr;
    private BigDecimal avvikProsent = BigDecimal.ZERO;

    private SammenligningsGrunnlag() {
        //Tom konstruktør
    }

    public Periode getSammenligningsperiode() {
        return sammenligningsperiode;
    }

    public BigDecimal getRapportertPrÅr() {
        return rapportertPrÅr;
    }

    public Long getAvvikPromille() {
        return avvikProsent.scaleByPowerOfTen(1).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }

    public BigDecimal getAvvikProsent() {
        return avvikProsent;
    }

    public void setAvvikProsent(BigDecimal avvikProsent) {
        this.avvikProsent = avvikProsent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SammenligningsGrunnlag mal;

        private Builder() {
            mal = new SammenligningsGrunnlag();
        }

        public Builder medSammenligningsperiode(Periode periode) {
            mal.sammenligningsperiode = periode;
            return this;
        }

        public Builder medRapportertPrÅr(BigDecimal rapportertPrÅr) {
            mal.rapportertPrÅr = rapportertPrÅr;
            return this;
        }

        public Builder medAvvikProsentFraPromille(long avvikPromille) {
            mal.avvikProsent = BigDecimal.valueOf(avvikPromille).scaleByPowerOfTen(-1);
            return this;
        }

        public Builder medAvvikProsent(BigDecimal avvikProsent) {
            mal.avvikProsent = avvikProsent;
            return this;
        }

        public SammenligningsGrunnlag build() {
            return mal;
        }
    }
}
