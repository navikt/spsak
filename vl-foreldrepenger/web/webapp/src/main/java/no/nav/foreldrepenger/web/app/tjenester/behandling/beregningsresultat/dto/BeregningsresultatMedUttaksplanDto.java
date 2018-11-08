package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeregningsresultatMedUttaksplanDto {
    private final boolean sokerErMor;
    private LocalDate opphoersdato;
    private final BeregningsresultatPeriodeDto[] perioder;

    private BeregningsresultatMedUttaksplanDto(Builder builder) {
        this.sokerErMor = builder.sokerErMor;
        this.opphoersdato = builder.opphoersdato;
        this.perioder = builder.perioder.stream().toArray(BeregningsresultatPeriodeDto[]::new);
    }

    public boolean getSokerErMor() {
        return sokerErMor;
    }

    public LocalDate getOpphoersdato() {
        return opphoersdato;
    }

    public BeregningsresultatPeriodeDto[] getPerioder() {
        return Arrays.copyOf(perioder, perioder.length);
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {
        private boolean sokerErMor;
        private LocalDate opphoersdato;
        private List<BeregningsresultatPeriodeDto> perioder;

        private Builder() {
            perioder = new ArrayList<>();
        }

        public Builder medSokerErMor(boolean sokerErMor) {
            this.sokerErMor = sokerErMor;
            return this;
        }

        public Builder medOpphoersdato(LocalDate opphoersdato) {
            this.opphoersdato = opphoersdato;
            return this;
        }

        public Builder medPerioder(List<BeregningsresultatPeriodeDto> perioder) {
            this.perioder = perioder;
            return this;
        }

        public BeregningsresultatMedUttaksplanDto create() {
            return new BeregningsresultatMedUttaksplanDto(this);
        }
    }
}
