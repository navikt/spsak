package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

public class BeregningsgrunnlagPrStatus {
    @JsonBackReference
    private BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;
    private AktivitetStatus aktivitetStatus;
    private Periode beregningsperiode;
    private List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = new ArrayList<>();

    private BigDecimal overstyrtPrÅr;
    private BigDecimal beregnetPrÅr;
    private BigDecimal bruttoPrÅr;
    private BigDecimal avkortetPrÅr;
    private BigDecimal redusertPrÅr;
    private BigDecimal gjennomsnittligPGI;
    private List<BigDecimal> pgiListe = new ArrayList<>();
    private BigDecimal årsbeløpFraTilstøtendeYtelse;
    private BigDecimal besteberegningPrÅr;
    private Boolean nyIArbeidslivet;
    private Long andelNr;
    private Inntektskategori inntektskategori;
    private boolean fastsattAvSaksbehandler = false;
    private boolean lagtTilAvSaksbehandler = false;

    private BeregningsgrunnlagPrStatus() {
    }

    @JsonIgnore
    public BeregningsgrunnlagPeriode getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Periode getBeregningsperiode() {
        return beregningsperiode;
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr != null ? avkortetPrÅr : arbeidsforhold.stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr != null ? redusertPrÅr : arbeidsforhold.stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getRedusertPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
    }

    public BigDecimal getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public boolean erArbeidstakerEllerFrilanser() {
        return AktivitetStatus.erArbeidstaker(aktivitetStatus) || AktivitetStatus.erFrilanser(aktivitetStatus);
    }

    public BigDecimal samletNaturalytelseBortfaltMinusTilkommetPrÅr() {
        BigDecimal sumBortfaltNaturalYtelse = arbeidsforhold.stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getNaturalytelseBortfaltPrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumTilkommetNaturalYtelse = arbeidsforhold.stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getNaturalytelseTilkommetPrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumBortfaltNaturalYtelse.subtract(sumTilkommetNaturalYtelse);
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr != null ? bruttoPrÅr : arbeidsforhold.stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getBruttoPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getBruttoInkludertNaturalytelsePrÅr() {
        BigDecimal brutto = getBruttoPrÅr();
        BigDecimal samletNaturalytelse = samletNaturalytelseBortfaltMinusTilkommetPrÅr();
        return brutto.add(samletNaturalytelse);
    }

    public List<BeregningsgrunnlagPrArbeidsforhold> getArbeidsforhold() {
        return arbeidsforhold;
    }

    public List<BeregningsgrunnlagPrArbeidsforhold> getArbeidsforholdIkkeFrilans() {
        return arbeidsforhold.stream().filter(af -> !af.erFrilanser()).collect(Collectors.toList());
    }

    public Optional<BeregningsgrunnlagPrArbeidsforhold> getFrilansArbeidsforhold() {
        return arbeidsforhold.stream().filter(BeregningsgrunnlagPrArbeidsforhold::erFrilanser).findAny();
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr != null ? beregnetPrÅr : arbeidsforhold.stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getBeregnetPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(null);
    }


    public BigDecimal getGjennomsnittligPGI() {
        return gjennomsnittligPGI;
    }

    public List<BigDecimal> getPgiListe() {
        return pgiListe;
    }

    public Long getAndelNr() {
        return andelNr;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public BigDecimal getÅrsbeløpFraTilstøtendeYtelse() {
        return årsbeløpFraTilstøtendeYtelse;
    }

    public Boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    public BigDecimal getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public boolean erFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public boolean erLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
        return new Builder(beregningsgrunnlagPrStatus);
    }

    public static class Builder {
        private BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatusMal;

        public Builder() {
            beregningsgrunnlagPrStatusMal = new BeregningsgrunnlagPrStatus();
        }

        public Builder(BeregningsgrunnlagPrStatus eksisterendeBGPrStatusMal) {
            beregningsgrunnlagPrStatusMal = eksisterendeBGPrStatusMal;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            beregningsgrunnlagPrStatusMal.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medBeregningsperiode(Periode beregningsperiode) {
            beregningsgrunnlagPrStatusMal.beregningsperiode = beregningsperiode;
            return this;
        }

        public Builder medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold) {
            beregningsgrunnlagPrStatusMal.arbeidsforhold.add(beregningsgrunnlagPrArbeidsforhold);
            return this;
        }

        public Builder medBeregnetPrÅr(BigDecimal beregnetPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.beregnetPrÅr = beregnetPrÅr;
            return medBruttoPrÅr(beregnetPrÅr);
        }

        public Builder medOverstyrtPrÅr(BigDecimal overstyrtPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.overstyrtPrÅr = overstyrtPrÅr;
            if (overstyrtPrÅr != null) {
                beregningsgrunnlagPrStatusMal.bruttoPrÅr = overstyrtPrÅr;
            }
            return this;
        }

        public Builder medBruttoPrÅr(BigDecimal bruttoPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.bruttoPrÅr = bruttoPrÅr;
            return this;
        }

        public Builder medAvkortetPrÅr(BigDecimal avkortetPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(BigDecimal redusertPrÅr) {
            sjekkIkkeArbeidstaker();
            beregningsgrunnlagPrStatusMal.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
            beregningsgrunnlagPrStatusMal.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
            beregningsgrunnlagPeriode.addBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatusMal);
            return this;
        }

        public Builder medÅrsbeløpFraTilstøtendeYtelse(BigDecimal årsbeløpFraTilstøtendeYtelse) {
            beregningsgrunnlagPrStatusMal.årsbeløpFraTilstøtendeYtelse = årsbeløpFraTilstøtendeYtelse;
            return this;
        }

        public Builder medErNyIArbeidslivet(Boolean nyIArbeidslivet) {
            beregningsgrunnlagPrStatusMal.nyIArbeidslivet = nyIArbeidslivet;
            return this;
        }

        public Builder medBesteberegningPrÅr(BigDecimal besteberegningPrÅr){
            beregningsgrunnlagPrStatusMal.besteberegningPrÅr = besteberegningPrÅr;
            return this;
        }

        public Builder medFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
            if (fastsattAvSaksbehandler != null) {
                beregningsgrunnlagPrStatusMal.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
            }
            return this;
        }

        public Builder medLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
            if (lagtTilAvSaksbehandler != null) {
                beregningsgrunnlagPrStatusMal.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
            }
            return this;
        }

        private void sjekkIkkeArbeidstaker() {
            if (beregningsgrunnlagPrStatusMal.aktivitetStatus == null || beregningsgrunnlagPrStatusMal.erArbeidstakerEllerFrilanser()) {
                throw new IllegalArgumentException("Kan ikke overstyre aggregert verdi for status ATFL");
            }
        }

        public Builder medArbeidsforhold(List<Arbeidsforhold> arbeidsforhold) {
            if (arbeidsforhold != null) {
                int andelNr = 1;
                for (Arbeidsforhold af : arbeidsforhold) {
                    beregningsgrunnlagPrStatusMal.arbeidsforhold.add(BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(af).medAndelNr(andelNr++).build());
                }
            }
            return this;
        }

        public Builder medArbeidsforhold(List<Arbeidsforhold> arbeidsforhold, List<BigDecimal> refusjonskravPrÅr, LocalDate skjæringstidspunkt) {
            if (arbeidsforhold != null) {
                if (!refusjonskravPrÅr.isEmpty() && arbeidsforhold.size() != refusjonskravPrÅr.size()) {
                    throw new IllegalArgumentException("Lengde på arbeidsforhold og refusjonskravPrÅr må vere like");
                }
                Periode beregningsperiode = Periode.of(skjæringstidspunkt.minusMonths(3).withDayOfMonth(1), skjæringstidspunkt.withDayOfMonth(1).minusDays(1));
                int andelNr = 1;
                for (int i = 0; i < arbeidsforhold.size(); i++) {
                    beregningsgrunnlagPrStatusMal.arbeidsforhold.add(BeregningsgrunnlagPrArbeidsforhold.builder()
                        .medArbeidsforhold(arbeidsforhold.get(i))
                        .medAndelNr(andelNr++)
                        .medRefusjonskravPrÅr(refusjonskravPrÅr.isEmpty() ? null : refusjonskravPrÅr.get(i))
                        .medBeregningsperiode(beregningsperiode)
                        .build());
                }
            }
            return this;
        }

        public Builder medGjennomsnittligPGI(BigDecimal gjennomsnittligPGI) {
            beregningsgrunnlagPrStatusMal.gjennomsnittligPGI = gjennomsnittligPGI;
            return this;
        }

        public Builder medAndelNr(Long andelNr) {
            beregningsgrunnlagPrStatusMal.andelNr = andelNr;
            return this;
        }

        public Builder medPGI(List<BigDecimal> pgiListe) {
            Objects.requireNonNull(beregningsgrunnlagPrStatusMal.aktivitetStatus, "pgiListe");
            if (pgiListe.isEmpty() || pgiListe.size() == 3) {
                beregningsgrunnlagPrStatusMal.pgiListe = pgiListe;
            } else {
                throw new IllegalArgumentException("Liste over PGI må være tom eller inneholde eksakt 3 årsverdier");
            }
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            beregningsgrunnlagPrStatusMal.inntektskategori = inntektskategori;
            return this;
        }

        public BeregningsgrunnlagPrStatus build() {
            verifyStateForBuild();
            return beregningsgrunnlagPrStatusMal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagPrStatusMal.aktivitetStatus, "aktivitetStatus");
            if (AktivitetStatus.ATFL.equals(beregningsgrunnlagPrStatusMal.aktivitetStatus)) {
                if (beregningsgrunnlagPrStatusMal.andelNr != null) {
                    throw new IllegalArgumentException("Andelsnr kan ikke angis for andel med status " + beregningsgrunnlagPrStatusMal.aktivitetStatus);
                }
            } else {
                Objects.requireNonNull(beregningsgrunnlagPrStatusMal.andelNr, "andelNr");
            }
        }
    }
}
