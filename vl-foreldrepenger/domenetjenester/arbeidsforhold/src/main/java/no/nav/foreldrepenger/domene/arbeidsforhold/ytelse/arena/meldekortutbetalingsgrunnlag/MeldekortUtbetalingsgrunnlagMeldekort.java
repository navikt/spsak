package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MeldekortUtbetalingsgrunnlagMeldekort {

    private LocalDate meldekortFom;
    private LocalDate meldekortTom;
    private BigDecimal dagsats;
    private BigDecimal beløp;
    private BigDecimal utbetalingsgrad;

    private MeldekortUtbetalingsgrunnlagMeldekort() { // NOSONAR
    }

    public LocalDate getMeldekortFom() {
        return meldekortFom;
    }

    public LocalDate getMeldekortTom() {
        return meldekortTom;
    }

    public BigDecimal getDagsats() {
        return dagsats;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }


    public static class MeldekortMeldekortBuilder {
        private final MeldekortUtbetalingsgrunnlagMeldekort meldekort;

        MeldekortMeldekortBuilder(MeldekortUtbetalingsgrunnlagMeldekort meldekort) {
            this.meldekort = meldekort;
        }

        public static MeldekortMeldekortBuilder ny() {
            return new MeldekortMeldekortBuilder(new MeldekortUtbetalingsgrunnlagMeldekort());
        }

        public MeldekortMeldekortBuilder medMeldekortFom(LocalDate dato) {
            this.meldekort.meldekortFom = dato;
            return this;
        }

        public MeldekortMeldekortBuilder medMeldekortTom(LocalDate dato) {
            this.meldekort.meldekortTom = dato;
            return this;
        }

        public MeldekortMeldekortBuilder medDagsats(BigDecimal dagsats) {
            this.meldekort.dagsats = dagsats;
            return this;
        }

        public MeldekortMeldekortBuilder medBeløp(BigDecimal beløp) {
            this.meldekort.beløp = beløp;
            return this;
        }

        public MeldekortMeldekortBuilder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
            this.meldekort.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public MeldekortUtbetalingsgrunnlagMeldekort build() {
            return this.meldekort;
        }

    }
}
