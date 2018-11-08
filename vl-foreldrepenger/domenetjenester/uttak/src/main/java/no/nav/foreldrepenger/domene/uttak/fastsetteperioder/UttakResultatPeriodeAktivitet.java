package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;

public class UttakResultatPeriodeAktivitet {

    private StønadskontoType trekkonto;
    private int trekkdager;
    private BigDecimal arbeidsprosent;
    private BigDecimal utbetalingsgrad;
    private String arbeidsforholdId;
    private String arbeidsforholdOrgnr;
    private UttakArbeidType uttakArbeidType;

    private UttakResultatPeriodeAktivitet() {
    }

    public StønadskontoType getTrekkonto() {
        return trekkonto;
    }

    public int getTrekkdager() {
        return trekkdager;
    }

    public BigDecimal getArbeidsprosent() {
        return arbeidsprosent;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public String getArbeidsforholdOrgnr() {
        return arbeidsforholdOrgnr;
    }

    public UttakArbeidType getUttakArbeidType() {
        return uttakArbeidType;
    }

    public void setUttakArbeidType(UttakArbeidType uttakArbeidType) {
        this.uttakArbeidType = uttakArbeidType;
    }

    public static class Builder {
        private UttakResultatPeriodeAktivitet kladd = new UttakResultatPeriodeAktivitet();

        public Builder medTrekkonto(StønadskontoType stønadskontoType) {
            kladd.trekkonto = stønadskontoType;
            return this;
        }

        public Builder medTrekkdager(int trekkdager) {
            kladd.trekkdager = trekkdager;
            return this;
        }

        public Builder medArbeidsprosent(BigDecimal arbeidsprosent) {
            kladd.arbeidsprosent = arbeidsprosent;
            return this;
        }

        public Builder medArbeidsforholdId(String arbeidsforholdId) {
            kladd.arbeidsforholdId = arbeidsforholdId;
            return this;
        }

        public Builder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
            kladd.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public Builder medArbeidsforholdOrgnr(String orgnr) {
            kladd.arbeidsforholdOrgnr = orgnr;
            return this;
        }

        public Builder medUttakArbeidType(UttakArbeidType uttakArbeidType) {
            kladd.uttakArbeidType = uttakArbeidType;
            return this;
        }

        public UttakResultatPeriodeAktivitet build() {
            Objects.requireNonNull(kladd.arbeidsprosent, "arbeidsprosent");
            Objects.requireNonNull(kladd.uttakArbeidType, "uttakArbeidType");
            return kladd;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UttakResultatPeriodeAktivitet that = (UttakResultatPeriodeAktivitet) o;
        return trekkdager == that.trekkdager &&
            likBortsettFraTrekkdager(that);
    }

    public boolean likBortsettFraTrekkdager(UttakResultatPeriodeAktivitet that) {
        return Objects.equals(trekkonto, that.trekkonto) &&
            Objects.equals(arbeidsprosent, that.arbeidsprosent) &&
            Objects.equals(utbetalingsgrad, that.utbetalingsgrad) &&
            Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
            Objects.equals(arbeidsforholdOrgnr, that.arbeidsforholdOrgnr);
    }

    @Override
    public int hashCode() {

        return Objects.hash(trekkonto, trekkdager, arbeidsprosent,
            utbetalingsgrad, arbeidsforholdId, arbeidsforholdOrgnr);
    }

    @Override
    public String toString() {
        return "UttakResultatPeriodeAktivitet{" +
            "arbeidsforholdId='" + arbeidsforholdId + '\'' +
            ", arbeidsforholdOrgnr='" + arbeidsforholdOrgnr + '\'' +
            ", uttakArbeidType=" + uttakArbeidType +
            '}';
    }
}
