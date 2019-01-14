package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.Grunnbeløp;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;

public class Beregningsgrunnlag {
    private final List<AktivitetStatusMedHjemmel> aktivitetStatuser = new ArrayList<>();
    private LocalDate skjæringstidspunkt;
    private Inntektsgrunnlag inntektsgrunnlag;
    @JsonManagedReference
    private final List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = new ArrayList<>();
    private SammenligningsGrunnlag sammenligningsGrunnlag;
    private Dekningsgrad dekningsgrad = Dekningsgrad.DEKNINGSGRAD_100;
    private BigDecimal grunnbeløp;
    private BigDecimal redusertGrunnbeløp;
    private List<Grunnbeløp> grunnbeløpSatser = new ArrayList<>();
    private boolean arbeidskategoriInaktiv;
    private boolean sykepengerPåSkjæringstidspunkt;

    private Beregningsgrunnlag() {
    }

    public boolean harSykepengerPåSkjæringstidpunkt() {
        return sykepengerPåSkjæringstidspunkt;
    }

    public boolean erArbeidskategoriInaktiv() {
        return arbeidskategoriInaktiv;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public Inntektsgrunnlag getInntektsgrunnlag() {
        return inntektsgrunnlag;
    }

    public List<AktivitetStatusMedHjemmel> getAktivitetStatuser() {
        return Collections.unmodifiableList(aktivitetStatuser);
    }

    public SammenligningsGrunnlag getSammenligningsGrunnlag() {
        return sammenligningsGrunnlag;
    }

    public List<BeregningsgrunnlagPeriode> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder.stream()
            .sorted(Comparator.comparing(bg -> bg.getBeregningsgrunnlagPeriode().getFom()))
            .collect(Collectors.toUnmodifiableList());
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public BigDecimal getGrunnbeløp() {
        return grunnbeløp;
    }

    public AktivitetStatusMedHjemmel getAktivitetStatus(AktivitetStatus aktivitetStatus) {
        return aktivitetStatuser.stream().filter(as -> as.inneholder(aktivitetStatus)).findAny()
                .orElseThrow(() -> new IllegalStateException("Beregningsgrunnlaget mangler regel for status " + aktivitetStatus.getBeskrivelse()));
    }

    public long verdiAvG(LocalDate dato) {
        Optional<Grunnbeløp> optional = grunnbeløpSatser.stream()
            .filter(g -> !dato.isBefore(g.getFom()) && !dato.isAfter(g.getTom()))
            .findFirst();

        if (optional.isPresent()) {
            return optional.get().getGVerdi();
        } else {
            throw new IllegalArgumentException("Kjenner ikke G-verdi for året " + dato.getYear());
        }
    }

    public long snittverdiAvG(int år) {
        Optional<Grunnbeløp> optional = grunnbeløpSatser.stream().filter(g -> g.getFom().getYear() == år).findFirst();
        if (optional.isPresent()) {
            return optional.get().getGSnitt();
        } else {
            throw new IllegalArgumentException("Kjenner ikke GSnitt-verdi for året " + år);
        }
    }

    public BigDecimal getRedusertGrunnbeløp() {
        return redusertGrunnbeløp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Beregningsgrunnlag beregningsgrunnlag) {
        return new Builder(beregningsgrunnlag);
    }

    public static class Builder {
        private Beregningsgrunnlag beregningsgrunnlagMal;

        public Builder() {
            beregningsgrunnlagMal = new Beregningsgrunnlag();
        }

        public Builder(Beregningsgrunnlag beregningsgrunnlag) {
            beregningsgrunnlagMal = beregningsgrunnlag;
        }

        public Builder medSykepengerPåSkjæringstidspunkt(boolean sykepengerPåSkjæringstidspunkt) {
            beregningsgrunnlagMal.sykepengerPåSkjæringstidspunkt = sykepengerPåSkjæringstidspunkt;
            return this;
        }

        public Builder medArbeidskategoriInaktiv(boolean arbeidskategoriInaktiv) {
            beregningsgrunnlagMal.arbeidskategoriInaktiv = arbeidskategoriInaktiv;
            return this;
        }

        public Builder medInntektsgrunnlag(Inntektsgrunnlag inntektsgrunnlag) {
            beregningsgrunnlagMal.inntektsgrunnlag = inntektsgrunnlag;
            return this;
        }

        public Builder medSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
            beregningsgrunnlagMal.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Builder medAktivitetStatuser(List<AktivitetStatusMedHjemmel> aktivitetStatusList) {
            beregningsgrunnlagMal.aktivitetStatuser.addAll(aktivitetStatusList);
            return this;
        }

        public Builder medSammenligningsgrunnlag(SammenligningsGrunnlag sammenligningsGrunnlag) {
            beregningsgrunnlagMal.sammenligningsGrunnlag = sammenligningsGrunnlag;
            return this;
        }

        public Builder medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
            beregningsgrunnlagMal.beregningsgrunnlagPerioder.add(beregningsgrunnlagPeriode);
            beregningsgrunnlagPeriode.setBeregningsgrunnlag(beregningsgrunnlagMal);
            return this;
        }

        public Builder medBeregningsgrunnlagPerioder(List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder) {
            beregningsgrunnlagMal.beregningsgrunnlagPerioder.addAll(beregningsgrunnlagPerioder);
            beregningsgrunnlagPerioder.forEach(bgPeriode -> bgPeriode.setBeregningsgrunnlag(beregningsgrunnlagMal));
            return this;
        }

        public Builder medDekningsgrad(Dekningsgrad dekningsgrad) {
            beregningsgrunnlagMal.dekningsgrad = dekningsgrad;
            return this;
        }

        public Builder medGrunnbeløp(BigDecimal grunnbeløp) {
            beregningsgrunnlagMal.grunnbeløp = grunnbeløp;
            return this;
        }

        public Builder medRedusertGrunnbeløp(BigDecimal redusertGrunnbeløp) {
            beregningsgrunnlagMal.redusertGrunnbeløp = redusertGrunnbeløp;
            return this;
        }

        public Builder medGrunnbeløpSatser(List<Grunnbeløp> grunnbeløpSatser) {
            beregningsgrunnlagMal.grunnbeløpSatser.clear();
            beregningsgrunnlagMal.grunnbeløpSatser.addAll(grunnbeløpSatser);
            return this;
        }

        public Beregningsgrunnlag build() {
            verifyStateForBuild();
            return beregningsgrunnlagMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagMal.inntektsgrunnlag, "inntektsgrunnlag");
            Objects.requireNonNull(beregningsgrunnlagMal.skjæringstidspunkt, "skjæringstidspunkt");
            Objects.requireNonNull(beregningsgrunnlagMal.aktivitetStatuser, "aktivitetStatuser");
            if (beregningsgrunnlagMal.beregningsgrunnlagPerioder.isEmpty()) {
                throw new IllegalStateException("Beregningsgrunnlaget må inneholde minst 1 periode");
            }
            if (beregningsgrunnlagMal.aktivitetStatuser.isEmpty()) {
                throw new IllegalStateException("Beregningsgrunnlaget må inneholde minst 1 status");
            }
            if (beregningsgrunnlagMal.grunnbeløpSatser.isEmpty()) {
                throw new IllegalStateException("Beregningsgrunnlaget må inneholde grunnbeløpsatser");
            }
        }
    }
}
