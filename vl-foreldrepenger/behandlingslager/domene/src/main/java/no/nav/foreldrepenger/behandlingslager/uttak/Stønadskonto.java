package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity
@Table(name = "STOENADSKONTO")
public class Stønadskonto extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STOENADSKONTO")
    private Long id;

    @ChangeTracked
    @Column(name = "max_dager", nullable = false)
    private Integer maxDager;

    @ChangeTracked
    @ManyToOne
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "stoenadskontotype", referencedColumnName = "kode")),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + StønadskontoType.DISCRIMINATOR
                + "'")) })
    private StønadskontoType stønadskontoType;

    @ManyToOne
    @JoinColumn(name = "stoenadskontoberegning_id", nullable = false)
    private Stønadskontoberegning stønadskontoberegning;

    Stønadskonto() {
        // For hibernate
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(stønadskontoType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof Stønadskonto)) {
            return false;
        }
        Stønadskonto other = (Stønadskonto) obj;
        return Objects.equals(stønadskontoType, other.stønadskontoType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stønadskontoType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<type=" + stønadskontoType + ", maxDager=" + maxDager + ">";

    }

    public int getMaxDager() {
        return maxDager;
    }

    public StønadskontoType getStønadskontoType() {
        return stønadskontoType;
    }

    public Stønadskontoberegning getStønadskontoberegning() {
        return stønadskontoberegning;
    }

    void setStønadskontoberegning(Stønadskontoberegning stønadskontoberegning) {
        this.stønadskontoberegning = stønadskontoberegning;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Stønadskonto kladd = new Stønadskonto();

        public Builder medMaxDager(int maxDager) {
            kladd.maxDager = maxDager;
            return this;
        }

        public Builder medStønadskontoType(StønadskontoType stønadskontoType) {
            kladd.stønadskontoType = stønadskontoType;
            return this;
        }

        public Stønadskonto build() {
            return kladd;
        }
    }
}
