package no.nav.foreldrepenger.domene.beregning.regelmodell;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.domene.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerPrÅr;

public class BeregningsresultatAndel {

    @JsonBackReference
    private BeregningsresultatPeriode beregningsresultatPeriode;
    private Boolean brukerErMottaker;
    private Arbeidsforhold arbeidsforhold;
    private Long dagsats;
    private BigDecimal stillingsprosent = BigDecimal.valueOf(100);
    private BigDecimal utbetalingsgrad = BigDecimal.valueOf(100);
    private Long dagsatsFraBg;
    private AktivitetStatus aktivitetStatus;
    private Inntektskategori inntektskategori;
    private List<BeregningsresultatFeriepengerPrÅr> beregningsresultatFeriepengerPrÅrListe = new ArrayList<>();

    private BeregningsresultatAndel() {
    }

    public BeregningsresultatPeriode getBeregningsresultatPeriode() {
        return beregningsresultatPeriode;
    }

    public Boolean erBrukerMottaker() {
        return brukerErMottaker;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public Long getDagsatsFraBg() {
        return dagsatsFraBg;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public List<BeregningsresultatFeriepengerPrÅr> getBeregningsresultatFeriepengerPrÅrListe() {
        return beregningsresultatFeriepengerPrÅrListe;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    @Override
    public String toString() {
        return "BeregningsresultatAndel{" +
            "aktivitetStatus='" + aktivitetStatus.name() + '\'' +
            ", orgnr=" + (arbeidsforhold != null ? arbeidsforhold.getOrgnr() : null) +
            ", arbeidsforholdId=" + (arbeidsforhold != null ? arbeidsforhold.getArbeidsforholdId() : null) +
            ", erBrukerMottaker=" + erBrukerMottaker() +
            '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsresultatAndel eksisterendeBeregningsresultatAndel) {
        return new Builder(eksisterendeBeregningsresultatAndel);
    }

    public static class Builder {
        private BeregningsresultatAndel beregningsresultatAndelMal;

        public Builder() {
            beregningsresultatAndelMal = new BeregningsresultatAndel();
        }

        public Builder(BeregningsresultatAndel eksisterendeBeregningsresultatAndel) {
            beregningsresultatAndelMal = eksisterendeBeregningsresultatAndel;
        }

        public Builder medBrukerErMottaker(Boolean brukerErMottaker) {
            beregningsresultatAndelMal.brukerErMottaker = brukerErMottaker;
            return this;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            beregningsresultatAndelMal.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder medDagsats(Long dagsats) {
            beregningsresultatAndelMal.dagsats = dagsats;
            return this;
        }

        public Builder medStillingsprosent(BigDecimal stillingsprosent) {
            beregningsresultatAndelMal.stillingsprosent = stillingsprosent;
            return this;
        }

        public Builder medUtbetalingssgrad(BigDecimal utbetalingsgrad) {
            beregningsresultatAndelMal.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public Builder medDagsatsFraBg(Long dagsatsFraBg) {
            beregningsresultatAndelMal.dagsatsFraBg = dagsatsFraBg;
            return this;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            beregningsresultatAndelMal.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            beregningsresultatAndelMal.inntektskategori = inntektskategori;
            return this;
        }

        public Builder leggTilBeregningsresultatFeriepengerPrÅr(BeregningsresultatFeriepengerPrÅr beregningsresultatFeriepengerPrÅr) {
            beregningsresultatAndelMal.beregningsresultatFeriepengerPrÅrListe.add(beregningsresultatFeriepengerPrÅr);
            return this;
        }

        public BeregningsresultatAndel build(BeregningsresultatPeriode beregningsresultatPeriode) {
            beregningsresultatAndelMal.beregningsresultatPeriode = beregningsresultatPeriode;
            verifyStateForBuild();
            beregningsresultatAndelMal.getBeregningsresultatPeriode()
                .addBeregningsresultatAndel(beregningsresultatAndelMal);
            return beregningsresultatAndelMal;
        }

        void verifyStateForBuild() {
            Objects.requireNonNull(beregningsresultatAndelMal.beregningsresultatPeriode, "beregningsresultatPeriode");
            Objects.requireNonNull(beregningsresultatAndelMal.brukerErMottaker, "brukerErMottaker");
            Objects.requireNonNull(beregningsresultatAndelMal.dagsats, "dagsats");
            Objects.requireNonNull(beregningsresultatAndelMal.dagsatsFraBg, "dagsatsFraBg");
            Objects.requireNonNull(beregningsresultatAndelMal.utbetalingsgrad, "utbetalingsgrad");
        }
    }
}
