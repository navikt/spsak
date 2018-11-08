package no.nav.foreldrepenger.beregning.regelmodell;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.fpsak.tidsserie.LocalDateInterval;

public class BeregningsresultatPeriode {
    @JsonBackReference
    private BeregningsresultatFP beregningsresultatFP;
    private List<BeregningsresultatAndel> beregningsresultatAndelList = new ArrayList<>();
    private LocalDateInterval periode;

    public LocalDate getFom() {
        return periode.getFomDato();
    }

    public LocalDate getTom() {
        return periode.getTomDato();
    }

    public LocalDateInterval getPeriode() {
        return periode;
    }

    public List<BeregningsresultatAndel> getBeregningsresultatAndelList() {
        return beregningsresultatAndelList;
    }

    public BeregningsresultatFP getBeregningsresultatFP() {
        return beregningsresultatFP;
    }

    public boolean inneholder(LocalDate dato) {
        return periode.encloses(dato);
    }

    public void addBeregningsresultatAndel(BeregningsresultatAndel beregningsresultatAndel) {
        Objects.requireNonNull(beregningsresultatAndel, "beregningsresultatAndel");
        if (!beregningsresultatAndelList.contains(beregningsresultatAndel)) {
            beregningsresultatAndelList.add(beregningsresultatAndel);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsresultatPeriode eksisterendeBeregningsresultatPeriode) {
        return new Builder(eksisterendeBeregningsresultatPeriode);
    }

    public static class Builder {
        private BeregningsresultatPeriode beregningsresultatPeriodeMal;

        public Builder() {
            beregningsresultatPeriodeMal = new BeregningsresultatPeriode();
        }

        public Builder(BeregningsresultatPeriode eksisterendeBeregningsresultatPeriode) {
            beregningsresultatPeriodeMal = eksisterendeBeregningsresultatPeriode;
        }

        public Builder medBeregningsresultatAndeler(List<BeregningsresultatAndel> beregningsresultatAndelList) {
            beregningsresultatPeriodeMal.beregningsresultatAndelList.addAll(beregningsresultatAndelList);
            return this;
        }

        public Builder medBeregningsresultatAndel(BeregningsresultatAndel beregningsresultatAndel) {
            beregningsresultatPeriodeMal.beregningsresultatAndelList.add(beregningsresultatAndel);
            return this;
        }

        public Builder medPeriode(LocalDateInterval periode) {
            beregningsresultatPeriodeMal.periode = periode;
            return this;
        }

        public BeregningsresultatPeriode build(BeregningsresultatFP beregningsresultatFP) {
            beregningsresultatPeriodeMal.beregningsresultatFP = beregningsresultatFP;
            verifyStateForBuild();
            beregningsresultatPeriodeMal.beregningsresultatFP.addBeregningsresultatPeriode(beregningsresultatPeriodeMal);
            return beregningsresultatPeriodeMal;
        }

        public BeregningsresultatPeriode build() {
            verifyStateForBuild();
            return beregningsresultatPeriodeMal;
        }

        void verifyStateForBuild() {
            Objects.requireNonNull(beregningsresultatPeriodeMal.beregningsresultatAndelList, "beregningsresultatAndeler");
            Objects.requireNonNull(beregningsresultatPeriodeMal.periode, "periode");
        }
    }
}

