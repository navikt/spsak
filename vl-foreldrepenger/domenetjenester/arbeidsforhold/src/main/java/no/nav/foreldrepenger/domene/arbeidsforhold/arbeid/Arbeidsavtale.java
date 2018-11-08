package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Arbeidsavtale {
    private LocalDate arbeidsavtaleFom;
    private LocalDate arbeidsavtaleTom;
    private BigDecimal stillingsprosent;
    private BigDecimal beregnetAntallTimerPrUke;
    private BigDecimal avtaltArbeidstimerPerUke;
    private LocalDate sisteLønnsendringsdato;
    private boolean erAnsettelsesPerioden;

    private Arbeidsavtale(LocalDate arbeidsavtaleFom, LocalDate arbeidsavtaleTom, BigDecimal stillingsprosent, BigDecimal beregnetAntallTimerPrUke, BigDecimal avtaltArbeidstimerPerUke, LocalDate sisteLønnsendringsdato, boolean erAnsettelsesPerioden) {
        this.arbeidsavtaleFom = arbeidsavtaleFom;
        this.arbeidsavtaleTom = arbeidsavtaleTom;
        this.stillingsprosent = stillingsprosent;
        this.beregnetAntallTimerPrUke = beregnetAntallTimerPrUke;
        this.avtaltArbeidstimerPerUke = avtaltArbeidstimerPerUke;
        this.sisteLønnsendringsdato = sisteLønnsendringsdato;
        this.erAnsettelsesPerioden = erAnsettelsesPerioden;
    }

    public LocalDate getArbeidsavtaleFom() {
        return arbeidsavtaleFom;
    }

    public LocalDate getArbeidsavtaleTom() {
        return arbeidsavtaleTom;
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    public BigDecimal getBeregnetAntallTimerPrUke() {
        return beregnetAntallTimerPrUke;
    }

    public BigDecimal getAvtaltArbeidstimerPerUke() {
        return avtaltArbeidstimerPerUke;
    }

    public LocalDate getSisteLønnsendringsdato() {
       return sisteLønnsendringsdato;
    }

    public boolean getErAnsettelsesPerioden() {
        return erAnsettelsesPerioden;
    }

    public static class Builder {
        private boolean erAnsettelsesPerioden = false;
        private LocalDate arbeidsavtaleFom;
        private LocalDate arbeidsavtaleTom;
        private LocalDate sisteLønnsendringsdato;
        private BigDecimal stillingsprosent;
        private BigDecimal beregnetAntallTimerPrUke;
        private BigDecimal avtaltArbeidstimerPerUke;

        public Builder medArbeidsavtaleFom(LocalDate arbeidsavtaleFom) {
            this.arbeidsavtaleFom = arbeidsavtaleFom;
            return this;
        }

        public Builder medArbeidsavtaleTom(LocalDate arbeidsavtaleTom) {
            this.arbeidsavtaleTom = arbeidsavtaleTom;
            return this;
        }

        public Builder medSisteLønnsendringsdato(LocalDate sisteLønnsendringsdato) {
            this.sisteLønnsendringsdato = sisteLønnsendringsdato;
            return this;
        }

        public Builder medStillingsprosent(BigDecimal stillingsprosent) {
            this.stillingsprosent = stillingsprosent;
            return this;
        }

        public Builder medBeregnetAntallTimerPrUke(BigDecimal beregnetAntallTimerPrUke) {
            this.beregnetAntallTimerPrUke = beregnetAntallTimerPrUke;
            return this;
        }

        public Builder medAvtaltArbeidstimerPerUke(BigDecimal avtaltArbeidstimerPerUke) {
            this.avtaltArbeidstimerPerUke = avtaltArbeidstimerPerUke;
            return this;
        }

        public Builder erAnsettelsesPerioden() {
            this.erAnsettelsesPerioden = true;
            return this;
        }

        public Arbeidsavtale build() {
            return new Arbeidsavtale(arbeidsavtaleFom, arbeidsavtaleTom, stillingsprosent, beregnetAntallTimerPrUke, avtaltArbeidstimerPerUke, sisteLønnsendringsdato, erAnsettelsesPerioden);
        }
    }
}
