package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;

public class UttakResultatPeriodeAktivitetDto {

    private StønadskontoType stønadskontoType;
    private Integer trekkdager;
    private BigDecimal prosentArbeid;
    private String arbeidsforholdId;
    private String arbeidsforholdNavn;
    private String arbeidsforholdOrgnr;
    private BigDecimal utbetalingsgrad;
    private UttakArbeidType uttakArbeidType;
    private boolean gradering;

    private UttakResultatPeriodeAktivitetDto() {
    }

    public StønadskontoType getStønadskontoType() {
        return stønadskontoType;
    }

    public Integer getTrekkdager() {
        return trekkdager;
    }

    public BigDecimal getProsentArbeid() {
        return prosentArbeid;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public String getArbeidsforholdNavn() {
        return arbeidsforholdNavn;
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

    @JsonIgnore
    boolean isGradering() {
        return gradering;
    }

    public static class Builder {

        private UttakResultatPeriodeAktivitetDto kladd = new UttakResultatPeriodeAktivitetDto();

        public Builder medStønadskontoType(StønadskontoType stønadskontoType) {
            kladd.stønadskontoType = stønadskontoType;
            return this;
        }

        public Builder medTrekkdager(Integer trekkdager) {
            kladd.trekkdager = trekkdager;
            return this;
        }

        public Builder medProsentArbeid(BigDecimal prosentArbeid) {
            kladd.prosentArbeid = prosentArbeid;
            return this;
        }

        public Builder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
            kladd.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public Builder medArbeidsforhold(String arbeidsforholdId, String arbeidsforholdOrgnr, String arbeidsforholdNavn) {
            kladd.arbeidsforholdId = arbeidsforholdId;
            kladd.arbeidsforholdOrgnr = arbeidsforholdOrgnr;
            kladd.arbeidsforholdNavn = arbeidsforholdNavn;
            return this;
        }

        public Builder medGradering(boolean gradering) {
            kladd.gradering = gradering;
            return this;
        }

        public Builder medUttakArbeidType(UttakArbeidType uttakArbeidType) {
            kladd.uttakArbeidType = uttakArbeidType;
            return this;
        }

        public UttakResultatPeriodeAktivitetDto build() {
            return kladd;
        }
    }
}
