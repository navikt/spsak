package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;
import no.nav.vedtak.util.InputValideringRegex;

public class UttakResultatPeriodeAktivitetLagreDto {

    @NotNull
    @ValidKodeverk
    private StønadskontoType stønadskontoType;

    @NotNull
    @Min(0)
    @Max(1000)
    private int trekkdager;

    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @Size(max = 200)
    private String arbeidsforholdOrgnr;

    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @Size(max = 200)
    private String arbeidsforholdId;

    @Min(0)
    @Max(100)
    @Digits(integer = 3, fraction = 2)
    private BigDecimal utbetalingsgrad;

    @ValidKodeverk
    private UttakArbeidType uttakArbeidType;

    UttakResultatPeriodeAktivitetLagreDto() { //NOSONAR
        //for jackson
    }

    public StønadskontoType getStønadskontoType() {
        return stønadskontoType;
    }

    public int getTrekkdager() {
        return trekkdager;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public String getArbeidsforholdOrgnr() {
        return arbeidsforholdOrgnr;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public UttakArbeidType getUttakArbeidType() {
        return uttakArbeidType;
    }

    public static class Builder {

        private UttakResultatPeriodeAktivitetLagreDto kladd = new UttakResultatPeriodeAktivitetLagreDto();

        public Builder medStønadskontoType(StønadskontoType stønadskontoType) {
            kladd.stønadskontoType = stønadskontoType;
            return this;
        }

        public Builder medTrekkdager(Integer trekkdager) {
            kladd.trekkdager = trekkdager;
            return this;
        }

        public Builder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
            kladd.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public Builder medArbeidsforholdOrgnr(String arbeidsforholdOrgnr) {
            kladd.arbeidsforholdOrgnr = arbeidsforholdOrgnr;
            return this;
        }

        public Builder medArbeidsforholdId(String arbeidsforholdId) {
            kladd.arbeidsforholdId = arbeidsforholdId;
            return this;
        }

        public Builder medUttakArbeidType(UttakArbeidType uttakArbeidType) {
            kladd.uttakArbeidType = uttakArbeidType;
            return this;
        }

        public UttakResultatPeriodeAktivitetLagreDto build() {
            return kladd;
        }
    }
}
