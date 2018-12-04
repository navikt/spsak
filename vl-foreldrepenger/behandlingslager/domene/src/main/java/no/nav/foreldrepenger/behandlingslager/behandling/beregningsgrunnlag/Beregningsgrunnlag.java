package no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.domene.typer.Beløp;

@Entity(name = "Beregningsgrunnlag")
@Table(name = "BEREGNINGSGRUNNLAG")
public class Beregningsgrunnlag extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEREGNINGSGRUNNLAG")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long versjon;

    @Column(name = "skjaringstidspunkt", nullable = false)
    private LocalDate skjæringstidspunkt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    private List<BeregningsgrunnlagAktivitetStatus> aktivitetStatuser = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    private List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = new ArrayList<>();

    @OneToOne(mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    private Sammenligningsgrunnlag sammenligningsgrunnlag;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regellogg_skjaringstidspunkt")
    private String regelloggSkjæringstidspunkt;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regellogg_brukers_status")
    private String regelloggBrukersStatus;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regelinput_skjaringstidspunkt")
    private String regelInputSkjæringstidspunkt;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regelinput_brukers_status")
    private String regelInputBrukersStatus;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regelinput_tilstoetende_ytelse")
    private String regelInputTilstøtendeYtelse;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regellogg_tilstoetende_ytelse")
    private String regelloggTilstøtendeYtelse;

    @Column(name = "dekningsgrad", nullable = false, columnDefinition = "NUMERIC")
    private Long dekningsgrad;

    @Column(name = "opprinnelig_skjaringstidspunkt", nullable = false)
    private LocalDate opprinneligSkjæringstidspunkt;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "grunnbeloep", nullable = false)))
    @ChangeTracked
    private Beløp grunnbeløp;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "redusert_grunnbeloep", nullable = false)))
    @ChangeTracked
    private Beløp redusertGrunnbeløp;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gjeldende_bg_kofakber_id")
    private Beregningsgrunnlag gjeldendeBeregningsgrunnlag;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsgrunnlag", cascade = CascadeType.PERSIST)
    private List<BeregningsgrunnlagFaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<BeregningsgrunnlagAktivitetStatus> getAktivitetStatuser() {
        return Collections.unmodifiableList(aktivitetStatuser);
    }

    public List<BeregningsgrunnlagPeriode> getBeregningsgrunnlagPerioder() {
        return Collections.unmodifiableList(beregningsgrunnlagPerioder
            .stream()
            .sorted(Comparator.comparing(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPeriodeFom))
            .collect(Collectors.toList()));
    }

    public Sammenligningsgrunnlag getSammenligningsgrunnlag() {
        return sammenligningsgrunnlag;
    }

    public Long getDekningsgrad() {
        return dekningsgrad;
    }

    public LocalDate getOpprinneligSkjæringstidspunkt() {
        return opprinneligSkjæringstidspunkt;
    }

    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }

    public Beløp getRedusertGrunnbeløp() {
        return redusertGrunnbeløp;
    }

    public void leggTilBeregningsgrunnlagAktivitetStatus(BeregningsgrunnlagAktivitetStatus bgAktivitetStatus) {
        Objects.requireNonNull(bgAktivitetStatus, "beregningsgrunnlagAktivitetStatus");
        aktivitetStatuser.remove(bgAktivitetStatus); // NOSONAR
        aktivitetStatuser.add(bgAktivitetStatus);
    }

    public void leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode bgPeriode) {
        Objects.requireNonNull(bgPeriode, "beregningsgrunnlagPeriode");
        if (!beregningsgrunnlagPerioder.contains(bgPeriode)) {//NOSONAR
            beregningsgrunnlagPerioder.add(bgPeriode);
        }
    }

    public String getRegelloggSkjæringstidspunkt() {
        return regelloggSkjæringstidspunkt;
    }

    public String getRegelloggBrukersStatus() {
        return regelloggBrukersStatus;
    }

    public String getRegelInputSkjæringstidspunkt() {
        return regelInputSkjæringstidspunkt;
    }

    public String getRegelInputBrukersStatus() {
        return regelInputBrukersStatus;
    }

    public String getRegelInputTilstøtendeYtelse() {
        return regelInputTilstøtendeYtelse;
    }

    public String getRegelloggTilstøtendeYtelse() {
        return regelloggTilstøtendeYtelse;
    }

    public Hjemmel getHjemmel() {
        if (aktivitetStatuser.isEmpty()) {
            return Hjemmel.UDEFINERT;
        }
        if (aktivitetStatuser.size() == 1) {
            return aktivitetStatuser.get(0).getHjemmel();
        }
        Optional<BeregningsgrunnlagAktivitetStatus> dagpenger = aktivitetStatuser.stream()
                .filter(as -> Hjemmel.F_14_7_8_49.equals(as.getHjemmel()))
                .findFirst();
        if (dagpenger.isPresent()) {
            return dagpenger.get().getHjemmel();
        }
        Optional<BeregningsgrunnlagAktivitetStatus> gjelder = aktivitetStatuser.stream()
                .filter(as -> !Hjemmel.F_14_7.equals(as.getHjemmel()))
                .findFirst();
        return gjelder.isPresent() ? gjelder.get().getHjemmel() : Hjemmel.F_14_7;
    }

    public Optional<Beregningsgrunnlag> getGjeldendeBeregningsgrunnlag() {
        return Optional.ofNullable(gjeldendeBeregningsgrunnlag);
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return Collections.unmodifiableList(faktaOmBeregningTilfeller
            .stream()
            .map(BeregningsgrunnlagFaktaOmBeregningTilfelle::getFaktaOmBeregningTilfelle)
            .collect(Collectors.toList()));
    }

    public Beregningsgrunnlag dypKopi() {
        Beregningsgrunnlag kopi = Kopimaskin.deepCopy(this);
        builder(kopi).medGjeldendeBeregningsgrunnlag(this.gjeldendeBeregningsgrunnlag).build();
        return kopi;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Beregningsgrunnlag)) {
            return false;
        }
        Beregningsgrunnlag other = (Beregningsgrunnlag) obj;
        return Objects.equals(this.getSkjæringstidspunkt(), other.getSkjæringstidspunkt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skjæringstidspunkt, dekningsgrad, opprinneligSkjæringstidspunkt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            "id=" + id + ", " //$NON-NLS-2$ //$NON-NLS-3$
            + "skjæringstidspunkt=" + skjæringstidspunkt + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "dekningsgrad=" + dekningsgrad + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "opprinneligSkjæringstidspunkt=" + opprinneligSkjæringstidspunkt + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "grunnbeløp=" + grunnbeløp + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "redusertGrunnbeløp=" + redusertGrunnbeløp + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Beregningsgrunnlag original) {
        return new Builder(original);
    }

    public static class Builder {
        private Beregningsgrunnlag beregningsgrunnlagMal;

        private Builder() {
            beregningsgrunnlagMal = new Beregningsgrunnlag();
        }

        private Builder(Beregningsgrunnlag original) {
            beregningsgrunnlagMal = original;
        }

        public Builder medSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
            beregningsgrunnlagMal.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Builder medDekningsgrad(Long dekningsgrad) {
            beregningsgrunnlagMal.dekningsgrad = dekningsgrad;
            return this;
        }

        public Builder medOpprinneligSkjæringstidspunkt(LocalDate opprinneligSkjæringstidspunkt) {
            beregningsgrunnlagMal.opprinneligSkjæringstidspunkt = opprinneligSkjæringstidspunkt;
            return this;
        }

        public Builder medGrunnbeløp(BigDecimal grunnbeløp) {
            beregningsgrunnlagMal.grunnbeløp = new Beløp(grunnbeløp);
            return this;
        }

        public Builder medGrunnbeløp(Beløp grunnbeløp) {
            beregningsgrunnlagMal.grunnbeløp = grunnbeløp;
            return this;
        }

        public Builder medRedusertGrunnbeløp(BigDecimal redusertGrunnbeløp) {
            beregningsgrunnlagMal.redusertGrunnbeløp = new Beløp(redusertGrunnbeløp);
            return this;
        }

        public Builder medRedusertGrunnbeløp(Beløp redusertGrunnbeløp) {
            beregningsgrunnlagMal.redusertGrunnbeløp = redusertGrunnbeløp;
            return this;
        }

        public Builder leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.Builder aktivitetStatusBuilder) {
            aktivitetStatusBuilder.build(beregningsgrunnlagMal);
            return this;
        }

        public Builder leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder) {
            beregningsgrunnlagPeriodeBuilder.build(beregningsgrunnlagMal);
            return this;
        }

        public Builder leggTilFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
            faktaOmBeregningTilfeller.forEach(this::leggTilFaktaOmBeregningTilfeller);
            return this;
        }

        private void leggTilFaktaOmBeregningTilfeller(FaktaOmBeregningTilfelle tilfelle) {
            BeregningsgrunnlagFaktaOmBeregningTilfelle b = BeregningsgrunnlagFaktaOmBeregningTilfelle.builder().medFaktaOmBeregningTilfelle(tilfelle).build(beregningsgrunnlagMal);
            this.beregningsgrunnlagMal.faktaOmBeregningTilfeller.add(b);
        }

        public Builder medSammenligningsgrunnlag(Sammenligningsgrunnlag sammenligningsgrunnlag) {
            beregningsgrunnlagMal.sammenligningsgrunnlag = sammenligningsgrunnlag;
            return this;
        }

        public Builder medRegelloggSkjæringstidspunkt(String regelInputSkjæringstidspunkt, String regelloggSkjæringstidspunkt) {
            beregningsgrunnlagMal.regelInputSkjæringstidspunkt = regelInputSkjæringstidspunkt;
            beregningsgrunnlagMal.regelloggSkjæringstidspunkt = regelloggSkjæringstidspunkt;
            return this;
        }

        public Builder medRegelloggBrukersStatus(String regelInputBrukersStatus, String regelloggBrukersStatus) {
            beregningsgrunnlagMal.regelInputBrukersStatus = regelInputBrukersStatus;
            beregningsgrunnlagMal.regelloggBrukersStatus = regelloggBrukersStatus;
            return this;
        }

        public Builder medRegelloggTilstøtendeYtelse(String regelInputTilstøtendeYtelse, String regelloggTilstøtendeYtelse) {
            beregningsgrunnlagMal.regelInputTilstøtendeYtelse = regelInputTilstøtendeYtelse;
            beregningsgrunnlagMal.regelloggTilstøtendeYtelse = regelloggTilstøtendeYtelse;
            return this;
        }

        public Builder medGjeldendeBeregningsgrunnlag(Beregningsgrunnlag gjeldendeBeregningsgrunnlag) {
            beregningsgrunnlagMal.gjeldendeBeregningsgrunnlag = gjeldendeBeregningsgrunnlag;
            return this;
        }

        public Beregningsgrunnlag build() {
            verifyStateForBuild();
            return beregningsgrunnlagMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagMal.skjæringstidspunkt, "skjæringstidspunkt");
        }
    }
}
