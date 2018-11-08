package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten;

import java.math.BigDecimal;
import java.time.YearMonth;

public class Månedsinntekt {

    private BigDecimal beløp;
    private YearMonth måned;
    private String arbeidsgiver;
    private String arbeidsforholdRef;
    private String ytelseKode;
    private String pensjonKode;
    private boolean ytelse;

    private Månedsinntekt(BigDecimal beløp, YearMonth måned, String arbeidsgiver, String arbeidsforholdRef) {
        this.beløp = beløp;
        this.måned = måned;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.ytelse = false;
    }

    public String getYtelseKode() {
        return ytelseKode;
    }

    public String getPensjonKode() {
        return pensjonKode;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public YearMonth getMåned() {
        return måned;
    }

    public String getArbeidsgiver() {
        return arbeidsgiver;
    }

    public String getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public boolean isYtelse() {
        return ytelse;
    }

    public static class Builder {
        private BigDecimal beløp;
        private YearMonth måned;
        private String utbetaler;
        private String arbeidsforholdRef;
        private String ytelseKode;
        private String pensjonKode;
        private boolean ytelse;

        public Builder medBeløp(BigDecimal beløp) {
            this.beløp = beløp;
            return this;
        }

        public Builder medMåned(YearMonth måned) {
            this.måned = måned;
            return this;
        }

        public Builder medArbeidsgiver(String arbeidsgiver) {
            this.utbetaler = arbeidsgiver;
            return this;
        }

        public Builder medPensjonEllerTrygdKode(String kode) {
            this.pensjonKode = kode;
            return this;
        }

        public Builder medYtelseKode(String kode) {
            this.ytelseKode = kode;
            return this;
        }

        public Builder medArbeidsforholdRef(String arbeidsforholdRef) {
            this.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medYtelse(boolean ytelse) {
            this.ytelse = ytelse;
            return this;
        }

        public Månedsinntekt build() {
            final Månedsinntekt månedsinntekt = new Månedsinntekt(beløp, måned, utbetaler, arbeidsforholdRef);
            månedsinntekt.ytelse = ytelse;
            månedsinntekt.pensjonKode = pensjonKode;
            månedsinntekt.ytelseKode = ytelseKode;
            return månedsinntekt;
        }
    }
}
