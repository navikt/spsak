package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;


public class TilstøtendeYtelseAndel {
    private BigDecimal beløp;
    private String orgNr;
    private InntektPeriodeType hyppighet;
    private LocalDate arbeidsforholdFom;
    private LocalDate arbeidsforholdTom;
    private Inntektskategori inntektskategori;
    private AktivitetStatus aktivitetStatus;

    public BigDecimal getBeløp() {
        return beløp;
    }

    public Optional<String> getOrgNr() {
        return Optional.ofNullable(orgNr);
    }

    public InntektPeriodeType getHyppighet() {
        return hyppighet;
    }

    public LocalDate getArbeidsforholdFom() {
        return arbeidsforholdFom;
    }

    public LocalDate getArbeidsforholdTom() {
        return arbeidsforholdTom;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TilstøtendeYtelseAndel kladd;

        private Builder() {
            kladd = new TilstøtendeYtelseAndel();
        }

        public Builder medBeløp(BigDecimal beløp) {
            kladd.beløp = beløp;
            return this;
        }

        public Builder medOrgNr(String orgNr) {
            kladd.orgNr = orgNr;
            return this;
        }

        public Builder medHyppighet(InntektPeriodeType inntektPeriodeType) {
            kladd.hyppighet = inntektPeriodeType;
            return this;
        }

        public Builder medArbeidsforholdFom(LocalDate arbeidsforholdFom) {
            kladd.arbeidsforholdFom = arbeidsforholdFom;
            return this;
        }

        public Builder medArbeidsforholdTom(LocalDate arbeidsforholdTom) {
            kladd.arbeidsforholdTom = arbeidsforholdTom;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            kladd.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public TilstøtendeYtelseAndel build() {
            return kladd;
        }
    }
}
