package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregningsgrunnlagPeriode {
    @JsonManagedReference
    private List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = new ArrayList<>();
    private Periode bgPeriode;
    private List<PeriodeÅrsak> periodeÅrsaker = new ArrayList<>();
    @JsonBackReference
    private Beregningsgrunnlag beregningsgrunnlag;

    private BeregningsgrunnlagPeriode() {
    }

    public BeregningsgrunnlagPrStatus getBeregningsgrunnlagPrStatus(AktivitetStatus aktivitetStatus) {
        return beregningsgrunnlagPrStatus.stream()
            .filter(af -> aktivitetStatus.equals(af.getAktivitetStatus()))
            .findFirst()
            .orElse(null);
    }

    @JsonIgnore
    public Beregningsgrunnlag getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    void setBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    void addBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
        Objects.requireNonNull(beregningsgrunnlagPrStatus, "beregningsgrunnlagPrStatus");
        Objects.requireNonNull(beregningsgrunnlagPrStatus.getAktivitetStatus(), "aktivitetStatus");
        this.beregningsgrunnlagPrStatus.add(beregningsgrunnlagPrStatus);
    }

    public BigDecimal getBruttoPrÅr() {
        return beregningsgrunnlagPrStatus.stream()
            .map(BeregningsgrunnlagPrStatus::getBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAvkortetPrÅr() {
        return beregningsgrunnlagPrStatus.stream()
            .map(BeregningsgrunnlagPrStatus::getAvkortetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
    }

    public BigDecimal getRedusertPrÅr() {
        return beregningsgrunnlagPrStatus.stream()
            .map(BeregningsgrunnlagPrStatus::getRedusertPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
    }

    public BigDecimal getBruttoPrÅrInkludertNaturalytelser() {
        BigDecimal naturalytelser = getNaturalytelserBortfaltMinusTilkommetPrÅr();
        BigDecimal brutto = getBruttoPrÅr();
        return brutto.add(naturalytelser);
    }


    private BigDecimal getNaturalytelserBortfaltMinusTilkommetPrÅr() {
        return beregningsgrunnlagPrStatus.stream()
            .map(BeregningsgrunnlagPrStatus::samletNaturalytelseBortfaltMinusTilkommetPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Periode getBeregningsgrunnlagPeriode() {
        return bgPeriode;
    }

    public Collection<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagPrStatus() {
        return beregningsgrunnlagPrStatus;
    }

    public Inntektsgrunnlag getInntektsgrunnlag() {
        return beregningsgrunnlag.getInntektsgrunnlag();
    }

    public LocalDate getSkjæringstidspunkt() {
        return beregningsgrunnlag.getSkjæringstidspunkt();
    }

    public BigDecimal getGrunnbeløp() {
        return beregningsgrunnlag.getGrunnbeløp();
    }

    public BigDecimal getRedusertGrunnbeløp() {
        return beregningsgrunnlag.getRedusertGrunnbeløp();
    }

    public SammenligningsGrunnlag getSammenligningsGrunnlag() {
        return beregningsgrunnlag.getSammenligningsGrunnlag();
    }

    public Dekningsgrad getDekningsgrad() {
        return beregningsgrunnlag.getDekningsgrad();
    }

    public List<AktivitetStatusMedHjemmel> getAktivitetStatuser() {
        return beregningsgrunnlag.getAktivitetStatuser().stream()
            .sorted(Comparator.comparing(as -> as.getAktivitetStatus().getBeregningPrioritet()))
            .collect(Collectors.toList());
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return periodeÅrsaker;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPeriode eksisterendeBeregningsgrunnlagPeriode) {
        return new Builder(eksisterendeBeregningsgrunnlagPeriode);
    }

    public static class Builder {
        private BeregningsgrunnlagPeriode beregningsgrunnlagPeriodeMal;

        private Builder() {
            beregningsgrunnlagPeriodeMal = new BeregningsgrunnlagPeriode();
        }

        public Builder(BeregningsgrunnlagPeriode eksisterendeBeregningsgrunnlagPeriod) {
            beregningsgrunnlagPeriodeMal = eksisterendeBeregningsgrunnlagPeriod;
        }

        public Builder medPeriode(Periode beregningsgrunnlagPeriode) {
            beregningsgrunnlagPeriodeMal.bgPeriode = beregningsgrunnlagPeriode;
            return this;
        }

        public Builder leggTilPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            if (!(beregningsgrunnlagPeriodeMal.periodeÅrsaker instanceof ArrayList)) {
                beregningsgrunnlagPeriodeMal.periodeÅrsaker = new ArrayList<>(beregningsgrunnlagPeriodeMal.periodeÅrsaker);
            }
            if (!beregningsgrunnlagPeriodeMal.periodeÅrsaker.contains(periodeÅrsak)) {
                beregningsgrunnlagPeriodeMal.periodeÅrsaker.add(periodeÅrsak);
            }
            return this;
        }

        public Builder medPeriodeÅrsaker(List<PeriodeÅrsak> periodeÅrsaker) {
            beregningsgrunnlagPeriodeMal.periodeÅrsaker = periodeÅrsaker;
            return this;
        }

        public Builder leggTilPeriodeÅrsaker(List<PeriodeÅrsak> periodeÅrsaker) {
            periodeÅrsaker.forEach(this::leggTilPeriodeÅrsak);
            return this;
        }

        public Builder medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
            if (beregningsgrunnlagPrStatus.getAndelNr() != null && beregningsgrunnlagPeriodeMal.beregningsgrunnlagPrStatus.stream()
                .anyMatch(bps -> beregningsgrunnlagPrStatus.getAndelNr().equals(bps.getAndelNr()))) {
                throw new IllegalArgumentException("AndelNr er null eller finnes allerede: " + beregningsgrunnlagPrStatus.getAndelNr());
            }
            beregningsgrunnlagPeriodeMal.addBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatus);
            beregningsgrunnlagPrStatus.setBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeMal);
            return this;
        }

        public BeregningsgrunnlagPeriode build() {
            verifyStateForBuild();
            return beregningsgrunnlagPeriodeMal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagPeriodeMal.beregningsgrunnlagPrStatus, "beregningsgrunnlagPrStatus");
            Objects.requireNonNull(beregningsgrunnlagPeriodeMal.bgPeriode, "bgPeriode");
            Objects.requireNonNull(beregningsgrunnlagPeriodeMal.bgPeriode.getFom(), "bgPeriode.getFom()");
        }
    }
}
