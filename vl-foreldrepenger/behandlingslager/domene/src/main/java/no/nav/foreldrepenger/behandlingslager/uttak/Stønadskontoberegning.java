package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity
@Table(name = "STOENADSKONTOBEREGNING")
public class Stønadskontoberegning extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STOENADSKONTOBEREGNING")
    private Long id;

    @Lob
    @ChangeTracked
    @Column(name = "regel_input", nullable = false)
    private String regelInput;

    @Lob
    @ChangeTracked
    @Column(name = "regel_evaluering", nullable = false)
    private String regelEvaluering;

    @ChangeTracked
    @OneToMany(mappedBy = "stønadskontoberegning")
    private Set<Stønadskonto> stønadskontoer = new HashSet<>();

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public Set<Stønadskonto> getStønadskontoer() {
        return Collections.unmodifiableSet(stønadskontoer);
    }

    public static class Builder {
        private Stønadskontoberegning kladd;

        public Builder() {
            kladd = new Stønadskontoberegning();
        }

        public Builder medRegelInput(String regelInput) {
            kladd.regelInput = regelInput;
            return this;
        }

        public Builder medRegelEvaluering(String regelEvaluering) {
            kladd.regelEvaluering = regelEvaluering;
            return this;
        }

        public Builder medStønadskonto(Stønadskonto stønadskonto) {
            Objects.requireNonNull(stønadskonto);
            stønadskonto.setStønadskontoberegning(kladd);
            kladd.stønadskontoer.add(stønadskonto);
            return this;
        }

        public Stønadskontoberegning build() {
            return kladd;
        }
    }
}
