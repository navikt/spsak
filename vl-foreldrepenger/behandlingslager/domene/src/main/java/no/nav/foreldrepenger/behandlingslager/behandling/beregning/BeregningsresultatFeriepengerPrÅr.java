package no.nav.foreldrepenger.behandlingslager.behandling.beregning;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "BeregningsresultatFeriepengerPrÅr")
@Table(name = "br_feriepenger_pr_aar")
public class BeregningsresultatFeriepengerPrÅr extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BR_FERIEPENGER_PR_AAR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "br_feriepenger_id", nullable = false, updatable = false)
    private BeregningsresultatFeriepenger beregningsresultatFeriepenger;

    @ManyToOne(optional = false)
    @JoinColumn(name = "beregningsresultat_andel_id", nullable = false, updatable = false)
    private BeregningsresultatAndel beregningsresultatAndel;

    @Column(name = "opptjeningsaar", nullable = false)
    private LocalDate opptjeningsår;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "aarsbeloep", nullable = false)))
    @ChangeTracked
    private Beløp årsbeløp;

    public Long getId() {
        return id;
    }

    public BeregningsresultatFeriepenger getBeregningsresultatFeriepenger() {
        return beregningsresultatFeriepenger;
    }

    public BeregningsresultatAndel getBeregningsresultatAndel() {
        return beregningsresultatAndel;
    }

    public LocalDate getOpptjeningsår() {
        return opptjeningsår;
    }

    public Beløp getÅrsbeløp() {
        return årsbeløp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsresultatFeriepengerPrÅr)) {
            return false;
        }
        BeregningsresultatFeriepengerPrÅr other = (BeregningsresultatFeriepengerPrÅr) obj;
        return Objects.equals(this.getId(), other.getId())
            && Objects.equals(this.getOpptjeningsår(), other.getOpptjeningsår())
            && Objects.equals(this.getÅrsbeløp(), other.getÅrsbeløp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, opptjeningsår, årsbeløp);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsresultatFeriepengerPrÅr beregningsresultatFeriepengerPrÅrMal;

        public Builder() {
            beregningsresultatFeriepengerPrÅrMal = new BeregningsresultatFeriepengerPrÅr();
        }

        public Builder medOpptjeningsår(LocalDate opptjeningsår) {
            beregningsresultatFeriepengerPrÅrMal.opptjeningsår = opptjeningsår;
            return this;
        }

        public Builder medÅrsbeløp(Long årsbeløp) {
            beregningsresultatFeriepengerPrÅrMal.årsbeløp = new Beløp(BigDecimal.valueOf(årsbeløp));
            return this;
        }

        public BeregningsresultatFeriepengerPrÅr build(BeregningsresultatFeriepenger beregningsresultatFeriepenger, BeregningsresultatAndel beregningsresultatAndel) {
            beregningsresultatFeriepengerPrÅrMal.beregningsresultatFeriepenger = beregningsresultatFeriepenger;
            BeregningsresultatFeriepenger.builder(beregningsresultatFeriepenger).leggTilBeregningsresultatFeriepengerPrÅr(beregningsresultatFeriepengerPrÅrMal);
            beregningsresultatFeriepengerPrÅrMal.beregningsresultatAndel = beregningsresultatAndel;
            BeregningsresultatAndel.builder(beregningsresultatAndel).leggTilBeregningsresultatFeriepengerPrÅr(beregningsresultatFeriepengerPrÅrMal);
            verifyStateForBuild();
            return beregningsresultatFeriepengerPrÅrMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsresultatFeriepengerPrÅrMal.beregningsresultatFeriepenger, "beregningsresultatFeriepenger");
            Objects.requireNonNull(beregningsresultatFeriepengerPrÅrMal.beregningsresultatAndel, "beregningsresultatAndel");
            Objects.requireNonNull(beregningsresultatFeriepengerPrÅrMal.opptjeningsår, "opptjeningsår");
            Objects.requireNonNull(beregningsresultatFeriepengerPrÅrMal.årsbeløp, "årsbeløp");
        }
    }
}
