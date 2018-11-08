package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeregningsresultatPeriodeDto {
    private final LocalDate fom;
    private final LocalDate tom;
    private final int dagsats;
    private final BeregningsresultatPeriodeAndelDto[] andeler;

    private BeregningsresultatPeriodeDto(Builder builder) {
        fom = builder.fom;
        tom = builder.tom;
        dagsats = builder.dagsats;
        andeler = builder.andeler.stream().toArray(BeregningsresultatPeriodeAndelDto[]::new);
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public int getDagsats() {
        return dagsats;
    }

    public BeregningsresultatPeriodeAndelDto[] getAndeler() {
        return Arrays.copyOf(andeler, andeler.length);
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {
        private LocalDate fom;
        private LocalDate tom;
        private int dagsats;
        private List<BeregningsresultatPeriodeAndelDto> andeler;

        private Builder() {
            this.andeler = new ArrayList<>();
        }

        public Builder medFom(LocalDate fom) {
            this.fom = fom;
            return this;
        }

        public Builder medTom(LocalDate tom) {
            this.tom = tom;
            return this;
        }

        public Builder medDagsats(int dagsats) {
            this.dagsats = dagsats;
            return this;
        }

        public Builder medAndeler(List<BeregningsresultatPeriodeAndelDto> andeler) {
            this.andeler.addAll(andeler);
            return this;
        }

        public BeregningsresultatPeriodeDto create() {
            return new BeregningsresultatPeriodeDto(this);
        }
    }
}
