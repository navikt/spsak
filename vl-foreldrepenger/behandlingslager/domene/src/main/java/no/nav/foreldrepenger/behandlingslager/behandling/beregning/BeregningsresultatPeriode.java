package no.nav.foreldrepenger.behandlingslager.behandling.beregning;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "BeregningsresultatPeriode")
@Table(name = "BEREGNINGSRESULTAT_PERIODE")
public class BeregningsresultatPeriode extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BR_PERIODE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BEREGNINGSRESULTAT_FP_ID", nullable = false, updatable = false)
    @JsonBackReference
    private BeregningsresultatFP beregningsresultatFP;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsresultatPeriode", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<BeregningsresultatAndel> beregningsresultatAndelList = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "br_periode_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "br_periode_tom"))
    })
    private DatoIntervallEntitet periode;

    public Long getId() {
        return id;
    }

    public LocalDate getBeregningsresultatPeriodeFom() {
        return periode.getFomDato();
    }

    public LocalDate getBeregningsresultatPeriodeTom() {
        return periode.getTomDato();
    }

    public List<BeregningsresultatAndel> getBeregningsresultatAndelList() {
        return Collections.unmodifiableList(beregningsresultatAndelList);
    }

    public BeregningsresultatFP getBeregningsresultatFP() {
        return beregningsresultatFP;
    }

    public void addBeregningsresultatAndel(BeregningsresultatAndel beregningsresultatAndel) {
        Objects.requireNonNull(beregningsresultatAndel, "beregningsresultatAndel");
        if (!beregningsresultatAndelList.contains(beregningsresultatAndel)) {
            beregningsresultatAndelList.add(beregningsresultatAndel);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsresultatPeriode)) {
            return false;
        }
        BeregningsresultatPeriode other = (BeregningsresultatPeriode) obj;
        return Objects.equals(this.getBeregningsresultatPeriodeFom(), other.getBeregningsresultatPeriodeFom())
            && Objects.equals(this.getBeregningsresultatPeriodeTom(), other.getBeregningsresultatPeriodeTom())
        ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
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

        public Builder medBeregningsresultatAndeler(List<BeregningsresultatAndel> beregningsresultatAndelList){
            beregningsresultatPeriodeMal.beregningsresultatAndelList.addAll(beregningsresultatAndelList);
            return this;
        }

        public Builder medBeregningsresultatPeriodeFomOgTom(LocalDate beregningsresultatPeriodeFom, LocalDate beregningsresultatPeriodeTom) {
            beregningsresultatPeriodeMal.periode = DatoIntervallEntitet.fraOgMedTilOgMed(beregningsresultatPeriodeFom, beregningsresultatPeriodeTom);
            return this;
        }

        public BeregningsresultatPeriode build(BeregningsresultatFP beregningsresultatFP) {
            beregningsresultatPeriodeMal.beregningsresultatFP = beregningsresultatFP;
            verifyStateForBuild();
            beregningsresultatPeriodeMal.beregningsresultatFP.addBeregningsresultatPeriode(beregningsresultatPeriodeMal);
            return beregningsresultatPeriodeMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsresultatPeriodeMal.beregningsresultatAndelList, "beregningsresultatAndeler");
            Objects.requireNonNull(beregningsresultatPeriodeMal.beregningsresultatFP, "beregningsresultatFP");
            Objects.requireNonNull(beregningsresultatPeriodeMal.periode, "beregningsresultatPeriodePeriode");
            Objects.requireNonNull(beregningsresultatPeriodeMal.periode.getFomDato(), "beregningsresultaPeriodeFom");
            Objects.requireNonNull(beregningsresultatPeriodeMal.periode.getTomDato(), "beregningsresultaPeriodeTom");
        }
    }
}

