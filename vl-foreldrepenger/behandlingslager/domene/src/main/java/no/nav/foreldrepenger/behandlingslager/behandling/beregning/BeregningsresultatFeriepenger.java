package no.nav.foreldrepenger.behandlingslager.behandling.beregning;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "BeregningsresultatFeriepenger")
@Table(name = "br_feriepenger")
public class BeregningsresultatFeriepenger extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BR_FERIEPENGER")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BEREGNINGSRESULTAT_FP_ID", nullable = false, updatable = false)
    private BeregningsresultatFP beregningsresultatFP;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsresultatFeriepenger", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<BeregningsresultatFeriepengerPrÅr> beregningsresultatFeriepengerPrÅrListe = new ArrayList<>();

    @Column(name = "feriepenger_periode_fom")
    private LocalDate feriepengerPeriodeFom;

    @Column(name = "feriepenger_periode_tom")
    private LocalDate feriepengerPeriodeTom;

    @Lob
    @Column(name = "feriepenger_regel_input", nullable = false)
    private String feriepengerRegelInput;

    @Lob
    @Column(name = "feriepenger_regel_sporing", nullable = false)
    private String feriepengerRegelSporing;

    public Long getId() {
        return id;
    }

    public LocalDate getFeriepengerPeriodeFom() {
        return feriepengerPeriodeFom;
    }

    public LocalDate getFeriepengerPeriodeTom() {
        return feriepengerPeriodeTom;
    }

    public String getFeriepengerRegelInput() {
        return feriepengerRegelInput;
    }

    public String getFeriepengerRegelSporing() {
        return feriepengerRegelSporing;
    }

    public List<BeregningsresultatFeriepengerPrÅr> getBeregningsresultatFeriepengerPrÅrListe() {
        return Collections.unmodifiableList(beregningsresultatFeriepengerPrÅrListe);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BeregningsresultatFeriepenger)) {
            return false;
        }
        BeregningsresultatFeriepenger other = (BeregningsresultatFeriepenger) obj;
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsresultatFeriepenger beregningsresultatFeriepenger) {
        return new Builder(beregningsresultatFeriepenger);
    }

    public static class Builder {
        private BeregningsresultatFeriepenger beregningsresultatFPMal;

        public Builder() {
            beregningsresultatFPMal = new BeregningsresultatFeriepenger();
        }

        public Builder(BeregningsresultatFeriepenger beregningsresultatFeriepenger) {
            beregningsresultatFPMal = beregningsresultatFeriepenger;
        }

        public Builder leggTilBeregningsresultatFeriepengerPrÅr(BeregningsresultatFeriepengerPrÅr beregningsresultatFeriepengerPrÅrMal) {
            beregningsresultatFPMal.beregningsresultatFeriepengerPrÅrListe.add(beregningsresultatFeriepengerPrÅrMal);
            return this;
        }

        public Builder medFeriepengerRegelInput(String regelInput) {
            beregningsresultatFPMal.feriepengerRegelInput = regelInput;
            return this;
        }

        public Builder medFeriepengerRegelSporing(String regelSporing) {
            beregningsresultatFPMal.feriepengerRegelSporing = regelSporing;
            return this;
        }

        public Builder medFeriepengerPeriodeFom(LocalDate feriepengerPeriodeFom) {
            beregningsresultatFPMal.feriepengerPeriodeFom = feriepengerPeriodeFom;
            return this;
        }

        public Builder medFeriepengerPeriodeTom(LocalDate feriepengerPeriodeTom) {
            beregningsresultatFPMal.feriepengerPeriodeTom = feriepengerPeriodeTom;
            return this;
        }

        public BeregningsresultatFeriepenger build(BeregningsresultatFP beregningsresultatFP) {
            beregningsresultatFPMal.beregningsresultatFP = beregningsresultatFP;
            BeregningsresultatFP.builder(beregningsresultatFP).medBeregningsresultatFeriepenger(beregningsresultatFPMal);
            verifyStateForBuild();
            return beregningsresultatFPMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsresultatFPMal.beregningsresultatFP, "beregningsresultatFP");
            Objects.requireNonNull(beregningsresultatFPMal.beregningsresultatFeriepengerPrÅrListe, "beregningsresultatFeriepengerPrÅrListe");
            Objects.requireNonNull(beregningsresultatFPMal.feriepengerRegelInput, "feriepengerRegelInput");
            Objects.requireNonNull(beregningsresultatFPMal.feriepengerRegelSporing, "feriepengerRegelSporing");
        }
    }
}
