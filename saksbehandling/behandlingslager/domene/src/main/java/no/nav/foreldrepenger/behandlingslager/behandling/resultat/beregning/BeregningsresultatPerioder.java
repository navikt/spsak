package no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "Beregningsresultat")
@Table(name = "BR_BEREGNINGSRESULTAT")
public class BeregningsresultatPerioder extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEREGNINGSRESULTAT_FP")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsresultat", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<BeregningsresultatPeriode> beregningsresultatPerioder = new ArrayList<>();

    /**
     * Er egentlig OneToOne, men må mappes slik da JPA/Hibernate ikke støtter OneToOne på annet enn shared PK.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "beregningsresultat", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Set<BeregningsresultatFeriepenger> beregningsresultatFeriepenger = new HashSet<>(1);

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regel_input", nullable = false)
    private String regelInput;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "regel_sporing", nullable = false)
    private String regelSporing;

    @Column(name = "endringsdato")
    private LocalDate endringsdato;

    public Long getId() {
        return id;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public String getRegelSporing() {
        return regelSporing;
    }

    public Optional<LocalDate> getEndringsdato(){
        return Optional.ofNullable(endringsdato);
    }

    public List<BeregningsresultatPeriode> getBeregningsresultatPerioder() {
        return Collections.unmodifiableList(beregningsresultatPerioder);
    }

    public void addBeregningsresultatPeriode(BeregningsresultatPeriode brPeriode){
        Objects.requireNonNull(brPeriode, "beregningsresultatPeriode");
        if (!beregningsresultatPerioder.contains(brPeriode)) {
            beregningsresultatPerioder.add(brPeriode);
        }
    }

    public Optional<BeregningsresultatFeriepenger> getBeregningsresultatFeriepenger() {
        if (this.beregningsresultatFeriepenger.size() > 1) {
            throw new IllegalStateException("Utviklerfeil: Det finnes flere BeregningsresultatFeriepenger");
        }
        return beregningsresultatFeriepenger.isEmpty() ? Optional.empty() : Optional.of(beregningsresultatFeriepenger.iterator().next());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsresultatPerioder)) {
            return false;
        }
        BeregningsresultatPerioder other = (BeregningsresultatPerioder) obj;
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsresultatPerioder beregningsresultat) {
        return new Builder(beregningsresultat);
    }

    public static class Builder {
        private BeregningsresultatPerioder beregningsresultatMal;

        public Builder() {
            this.beregningsresultatMal = new BeregningsresultatPerioder();
        }

        public Builder(BeregningsresultatPerioder beregningsresultat) {
            this.beregningsresultatMal = beregningsresultat;
        }

        public Builder medRegelInput(String regelInput){
            beregningsresultatMal.regelInput = regelInput;
            return this;
        }

        public Builder medRegelSporing(String regelSporing){
            beregningsresultatMal.regelSporing = regelSporing;
            return this;
        }

        public Builder medBeregningsresultatFeriepenger(BeregningsresultatFeriepenger beregningsresultatFeriepenger) {
            beregningsresultatMal.beregningsresultatFeriepenger.clear();
            beregningsresultatMal.beregningsresultatFeriepenger.add(beregningsresultatFeriepenger);
            return this;
        }

        public Builder medEndringsdato(LocalDate endringsdato){
            beregningsresultatMal.endringsdato = endringsdato;
            return this;
        }

        public BeregningsresultatPerioder build() {
            verifyStateForBuild();
            return beregningsresultatMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsresultatMal.beregningsresultatPerioder, "beregningsresultatPerioder");
            Objects.requireNonNull(beregningsresultatMal.regelInput, "regelInput");
            Objects.requireNonNull(beregningsresultatMal.regelSporing, "regelSporing");
        }
    }
}
