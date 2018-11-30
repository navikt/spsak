package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.InntektPeriodeType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.ReferanseType;

public class BeregningsgrunnlagAndelTilstøtendeYtelse {
    private String orgnr;
    private String aktørId;
    private ReferanseType referanseType;
    private String arbeidsforholdId;
    private BigDecimal beregnetPrÅr;
    private Inntektskategori inntektskategori;
    private LocalDate arbeidsperiodeFom;
    private LocalDate arbeidsperiodeTom;
    private InntektPeriodeType hyppighet;
    private BigDecimal beløp;
    private AktivitetStatus aktivitetStatus;
    private boolean fraTilstøtendeYtelse;
    private BigDecimal refusjonskrav;

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public String getOrgnr() {
        return orgnr;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public InntektPeriodeType getHyppighet() {
        return hyppighet;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public LocalDate getArbeidsperiodeFom() {
        return arbeidsperiodeFom;
    }

    public LocalDate getArbeidsperiodeTom() {
        return arbeidsperiodeTom;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public boolean erFraTilstøtendeYtelse() {
        return fraTilstøtendeYtelse;
    }

    public Optional<BigDecimal> getRefusjonskrav() {
        return Optional.ofNullable(refusjonskrav);
    }

    public String getAktørId() {
        return aktørId;
    }

    public ReferanseType getReferanseType() {
        return referanseType;
    }

    public String getIdentifikator() {
        if (ReferanseType.AKTØR_ID.equals(referanseType)) {
            return aktørId;
        } else if (ReferanseType.ORG_NR.equals(referanseType)) {
            return orgnr;
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagAndelTilstøtendeYtelse original) {
        return new Builder(original);
    }

    public static class Builder {
        private BeregningsgrunnlagAndelTilstøtendeYtelse kladd;

        private Builder() {
            kladd = new BeregningsgrunnlagAndelTilstøtendeYtelse();
        }

        public Builder(BeregningsgrunnlagAndelTilstøtendeYtelse original) {
            kladd = original;
        }

        public Builder medOrgnr(String orgnr) {
            kladd.orgnr = orgnr;
            kladd.referanseType = ReferanseType.ORG_NR;
            return this;
        }

        public Builder medAktørId(String aktørId) {
            kladd.aktørId = aktørId;
            kladd.referanseType = ReferanseType.AKTØR_ID;
            return this;
        }


        public Builder medArbeidsforholdId(String arbeidsforholdId) {
            kladd.arbeidsforholdId = arbeidsforholdId;
            return this;
        }

        public Builder medBeregnetPrÅr(BigDecimal beregnetPrÅr) {
            kladd.beregnetPrÅr = beregnetPrÅr;
            return this;
        }

        public Builder medArbeidsperiodeFom(LocalDate arbeidsperiodeFom) {
            kladd.arbeidsperiodeFom = arbeidsperiodeFom;
            return this;
        }

        public Builder medArbeidsperiodeTom(LocalDate arbeidsperiodeTom) {
            kladd.arbeidsperiodeTom = arbeidsperiodeTom;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            kladd.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medBeløp(BigDecimal beløp) {
            kladd.beløp = beløp;
            return this;
        }

        public Builder medHyppighet(InntektPeriodeType hyppighet) {
            kladd.hyppighet = hyppighet;
            return this;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }


        public Builder medRefusjonskrav(BigDecimal refusjonskrav) {
            kladd.refusjonskrav = refusjonskrav;
            return this;
        }

        public Builder medFraTilstøtendeYtelse() {
            kladd.fraTilstøtendeYtelse = true;
            return this;
        }

        public BeregningsgrunnlagAndelTilstøtendeYtelse build() {
            verifiserStateForBuild();
            return kladd;
        }

        private void verifiserStateForBuild() {
            Objects.requireNonNull(kladd.inntektskategori);
        }
    }
}
