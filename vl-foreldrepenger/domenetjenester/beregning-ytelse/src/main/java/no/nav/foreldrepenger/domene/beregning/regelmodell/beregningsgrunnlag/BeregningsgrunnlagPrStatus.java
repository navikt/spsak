package no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BeregningsgrunnlagPrStatus {
    private AktivitetStatus aktivitetStatus;
    private List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = new ArrayList<>();
    private BigDecimal redusertBrukersAndelPrÅr;
    private Inntektskategori inntektskategori;

    BeregningsgrunnlagPrStatus() {
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public List<BeregningsgrunnlagPrArbeidsforhold> getArbeidsforhold() {
        return arbeidsforhold;
    }

    public BigDecimal getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatusMal;

        public Builder() {
            beregningsgrunnlagPrStatusMal = new BeregningsgrunnlagPrStatus();
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            beregningsgrunnlagPrStatusMal.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold) {
            beregningsgrunnlagPrStatusMal.arbeidsforhold.add(beregningsgrunnlagPrArbeidsforhold);
            return this;
        }

        public Builder medRedusertBrukersAndelPrÅr(BigDecimal redusertBrukersAndelPrÅr) {
            beregningsgrunnlagPrStatusMal.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            beregningsgrunnlagPrStatusMal.inntektskategori = inntektskategori;
            return this;
        }

        public BeregningsgrunnlagPrStatus build() {
            verifyStateForBuild();
            return beregningsgrunnlagPrStatusMal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagPrStatusMal.aktivitetStatus, "aktivitetStatus");
        }

        public Builder medArbeidsforhold(List<Arbeidsforhold> arbeidsforhold) {
            if (arbeidsforhold != null) {
                arbeidsforhold.forEach(af -> beregningsgrunnlagPrStatusMal.arbeidsforhold.add(BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(af).build()));
            }
            return this;
        }
    }
}
