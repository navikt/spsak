package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;

public class Periodeinntekt {
    private Periode periode;
    private BigDecimal inntekt;
    private Inntektskilde inntektskilde;
    private Arbeidsforhold arbeidsgiver;
    private BigDecimal utbetalingsgrad;
    private List<NaturalYtelse> naturalYtelser = new ArrayList<>();

    public BigDecimal getInntekt() {
        return inntekt;
    }

    public boolean inneholder(LocalDate dato) {
        return !(dato.isBefore(getFom()) || dato.isAfter(getTom()));
    }

    public boolean erFraår(int år) {
        return år == getFom().getYear();
    }

    public Inntektskilde getInntektskilde() {
        return inntektskilde;
    }

    public Optional<Arbeidsforhold> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public LocalDate getFom() {
        return periode.getFom();
    }

    public LocalDate getTom() {
        return periode.getTom();
    }


    public Optional<BigDecimal> getUtbetalingsgrad() {
        return Optional.ofNullable(utbetalingsgrad);
    }

    public List<NaturalYtelse> getNaturalYtelser() {
        return Collections.unmodifiableList(naturalYtelser);
    }

    public boolean fraInntektsmelding() {
        return Inntektskilde.INNTEKTSMELDING.equals(inntektskilde);
    }

    public boolean erInnenforPeriode(Periode periode) {
        return !getFom().isBefore(periode.getFom()) && !getTom().isAfter(periode.getTom());
    }

    //Eneste tillate oppretting av en periodeinntekt da feltene skal være effektivt final (uten å være det for builderens skyld)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Periodeinntekt kladd;

        private Builder() {
            kladd = new Periodeinntekt();
        }

        public Builder medInntektskilde(Inntektskilde inntektskilde) {
            kladd.inntektskilde = inntektskilde;
            return this;
        }

        public Builder medPeriode(Periode periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder medMåned(LocalDate dato) {
            kladd.periode = Periode.of(dato.withDayOfMonth(1), dato.withDayOfMonth(1).plusMonths(1).minusDays(1));
            return this;
        }

        public Builder medArbeidsgiver(Arbeidsforhold arbeidsgiver) {
            kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medInntekt(BigDecimal inntekt) {
            kladd.inntekt = inntekt;
            return this;
        }

        public Builder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
            kladd.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public Builder medNaturalYtelser(List<NaturalYtelse> naturalYtelser) {
            if (naturalYtelser != null) {
                kladd.naturalYtelser.addAll(naturalYtelser);
            }
            return this;
        }

        public Periodeinntekt build() {
            verifyStateForBuild();
            if (!kladd.getNaturalYtelser().isEmpty() && !kladd.fraInntektsmelding()) {
                throw new IllegalArgumentException("Naturalytelse kan bare angis med kilde Inntektsmelding");
            }
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.inntekt, "Inntekt");
            Objects.requireNonNull(kladd.inntektskilde, "Inntektskilde");
            Objects.requireNonNull(kladd.periode, "Periode");
            Objects.requireNonNull(kladd.getFom(), "Fom");
            Objects.requireNonNull(kladd.getTom(), "Tom");
        }

    }
}
