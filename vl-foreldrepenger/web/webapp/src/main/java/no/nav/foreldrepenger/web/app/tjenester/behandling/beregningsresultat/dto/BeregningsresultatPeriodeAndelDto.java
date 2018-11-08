package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;

public class BeregningsresultatPeriodeAndelDto {
    private final String arbeidsgiverNavn;
    private final String arbeidsgiverOrgnr;
    private final Integer refusjon;
    private final Integer tilSoker;
    private final UttakDto uttak;
    private final BigDecimal utbetalingsgrad;
    private final LocalDate sisteUtbetalingsdato;
    private final AktivitetStatus aktivitetStatus;
    private final String arbeidsforholdId;
    private final OpptjeningAktivitetType arbeidsforholdType;

    private BeregningsresultatPeriodeAndelDto(Builder builder) {
        this.arbeidsgiverNavn = builder.arbeidsgiverNavn;
        this.arbeidsgiverOrgnr = builder.arbeidsgiverOrgnr;
        this.refusjon = builder.refusjon;
        this.tilSoker = builder.tilSøker;
        this.uttak = builder.uttak;
        this.utbetalingsgrad = builder.utbetalingsgrad;
        this.sisteUtbetalingsdato = builder.sisteUtbetalingsdato;
        this.aktivitetStatus = builder.aktivitetStatus;
        this.arbeidsforholdId = builder.arbeidsforholdId;
        this.arbeidsforholdType = builder.arbeidsforholdType;
    }

    public String getArbeidsgiverNavn() {
        return arbeidsgiverNavn;
    }

    public String getArbeidsgiverOrgnr() {
        return arbeidsgiverOrgnr;
    }

    public Integer getRefusjon() {
        return refusjon;
    }

    public Integer getTilSoker() {
        return tilSoker;
    }

    public UttakDto getUttak() {
        return uttak;
    }

    public BigDecimal getUtbetalingsgrad() { return utbetalingsgrad; }

    public LocalDate getSisteUtbetalingsdato() { return sisteUtbetalingsdato; }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public String getArbeidsforholdId() { return arbeidsforholdId; }

    public OpptjeningAktivitetType getArbeidsforholdType() { return arbeidsforholdType; }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {
        private String arbeidsgiverNavn;
        private String arbeidsgiverOrgnr;
        private Integer refusjon;
        private Integer tilSøker;
        private BigDecimal utbetalingsgrad;
        private UttakDto uttak;
        private LocalDate sisteUtbetalingsdato;
        private AktivitetStatus aktivitetStatus;
        private String arbeidsforholdId;
        private OpptjeningAktivitetType arbeidsforholdType;

        private Builder() {
        }

        public Builder medArbeidsgiverOrgnr(String arbeidsgiverOrgnr) {
            this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
            return this;
        }

        public Builder medArbeidsgiverNavn(String arbeidsgiverNavn) {
            this.arbeidsgiverNavn= arbeidsgiverNavn;
            return this;
        }

        public Builder medRefusjon(Integer refusjon) {
            this.refusjon = refusjon;
            return this;
        }

        public Builder medTilSøker(Integer tilSøker) {
            this.tilSøker = tilSøker;
            return this;
        }

        public Builder medUtbetalingsgrad(BigDecimal utbetalingsgrad ) {
            this.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public Builder medSisteUtbetalingsdato(LocalDate sisteUtbetalingsdato ) {
            this.sisteUtbetalingsdato = sisteUtbetalingsdato;
            return this;
        }

        public Builder medAktivitetstatus(AktivitetStatus aktivitetStatus) {
            this.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medArbeidsforholdId(String arbeidsforholdId) {
            this.arbeidsforholdId = arbeidsforholdId;
            return this;
        }

        public Builder medArbeidsforholdType(OpptjeningAktivitetType arbeidsforholdType) {
            this.arbeidsforholdType = arbeidsforholdType;
            return this;
        }

        public Builder medUttak(UttakDto uttak) {
            this.uttak = uttak;
            return this;
        }

        public BeregningsresultatPeriodeAndelDto create() {
            return new BeregningsresultatPeriodeAndelDto(this);
        }
    }
}
