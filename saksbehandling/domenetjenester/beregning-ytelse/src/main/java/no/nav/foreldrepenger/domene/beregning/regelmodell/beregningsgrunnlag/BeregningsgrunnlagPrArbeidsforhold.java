package no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class BeregningsgrunnlagPrArbeidsforhold {
    private Arbeidsforhold arbeidsforhold;
    private BigDecimal redusertRefusjonPrÅr;
    private BigDecimal redusertBrukersAndelPrÅr;
    private Long dagsatsBruker;
    private Long dagsatsArbeidsgiver;
    private Inntektskategori inntektskategori;

    BeregningsgrunnlagPrArbeidsforhold() {
    }

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public Long getDagsatsBruker() {
        return dagsatsBruker;
    }

    public Long getDagsatsArbeidsgiver() {
        return dagsatsArbeidsgiver;
    }

    public BigDecimal getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
    }

    public BigDecimal getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsgrunnlagPrArbeidsforhold mal;

        public Builder() {
            mal = new BeregningsgrunnlagPrArbeidsforhold();
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            mal.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder medRedusertRefusjonPrÅr(BigDecimal redusertRefusjonPrÅr) {
            mal.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
            mal.dagsatsArbeidsgiver = redusertRefusjonPrÅr == null ? null : avrundTilDagsats(redusertRefusjonPrÅr);
            return this;
        }

        public Builder medRedusertBrukersAndelPrÅr(BigDecimal redusertBrukersAndelPrÅr) {
            mal.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
            mal.dagsatsBruker = redusertBrukersAndelPrÅr == null ? null : avrundTilDagsats(redusertBrukersAndelPrÅr);
            return this;
        }

        public Builder medDagsatsBruker(Long dagsatsBruker) {
            mal.dagsatsBruker = dagsatsBruker;
            return this;
        }

        public Builder medDagsatsArbeidsgiver(Long dagsatsArbeidsgiver) {
            mal.dagsatsArbeidsgiver = dagsatsArbeidsgiver;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            mal.inntektskategori = inntektskategori;
            return this;
        }

        private long avrundTilDagsats(BigDecimal verdi) {
            return verdi.divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
        }

        public BeregningsgrunnlagPrArbeidsforhold build() {
            verifyStateForBuild();
            return mal;
        }

        void verifyStateForBuild() {
            Objects.requireNonNull(mal.arbeidsforhold, "arbeidsforhold");
        }
    }
}
