package no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;

public class BeregningsgrunnlagPeriode {
    @JsonManagedReference
    private List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = new ArrayList<>();
    private Periode bgPeriode;

    public BeregningsgrunnlagPeriode() {
    }

    public List<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagPrStatus(AktivitetStatus aktivitetStatus) {
        return beregningsgrunnlagPrStatus.stream()
                .filter(af -> aktivitetStatus.equals(af.getAktivitetStatus()))
                .collect(Collectors.toList());
    }

    public List<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagPrStatus() {
        return beregningsgrunnlagPrStatus;
    }

    void addBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
        Objects.requireNonNull(beregningsgrunnlagPrStatus, "beregningsgrunnlagPrStatus");
        Objects.requireNonNull(beregningsgrunnlagPrStatus.getAktivitetStatus(), "aktivitetStatus");
        this.beregningsgrunnlagPrStatus.add(beregningsgrunnlagPrStatus);
    }

    public Periode getBeregningsgrunnlagPeriode() {
        return bgPeriode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsgrunnlagPeriode beregningsgrunnlagPeriodeMal;

        public Builder() {
            beregningsgrunnlagPeriodeMal = new BeregningsgrunnlagPeriode();
        }

        public Builder medPeriode(Periode beregningsgrunnlagPeriode) {
            beregningsgrunnlagPeriodeMal.bgPeriode = beregningsgrunnlagPeriode;
            return this;
        }

        public Builder medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
            beregningsgrunnlagPeriodeMal.addBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatus);
            return this;
        }

        public BeregningsgrunnlagPeriode build() {
            verifyStateForBuild();
            return beregningsgrunnlagPeriodeMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagPeriodeMal.beregningsgrunnlagPrStatus, "beregningsgrunnlagPrStatus");
            Objects.requireNonNull(beregningsgrunnlagPeriodeMal.bgPeriode, "bgPeriode");
            Objects.requireNonNull(beregningsgrunnlagPeriodeMal.bgPeriode.getFom(), "bgPeriode.getFom()");
        }
    }
}
